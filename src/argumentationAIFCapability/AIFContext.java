package argumentationAIFCapability;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID; 

import org.json.JSONObject;

import argumentationToastCapability.ArgumentationTheory;
import argumentationToastCapability.ToastContext;
import fusekiOntologyCapability.FusekiContext;
import oo2apl.agent.Context;

/**
 * 
 * This context manages AIF graphs and allows ASPIC evaluation of them using 
 * TOAST. It also manages the dialogues in which the agent is currently participating. 
 * 
 * @author Bas Testerink
 */
public final class AIFContext implements Context {
	private final FusekiContext fuseki;
	private final ToastContext toast;
	private final String pathToAIFFilesDirectory; 
	private final String selectPremisesQuery, selectStrictRulesQuery, selectDefeasibleRulesQuery, selectContrarinessQuery,
						 clearPositionQuery;

	public AIFContext(final FusekiContext fuseki, final ToastContext toast, final String pathToAIFFilesDirectory){
		this.fuseki = fuseki;
		this.toast = toast;
		this.pathToAIFFilesDirectory = pathToAIFFilesDirectory; 
		this.selectPremisesQuery = readFile(pathToAIFFilesDirectory+"selectPremises.sparql");
		this.selectStrictRulesQuery = readFile(pathToAIFFilesDirectory+"selectStrictRules.sparql");
		this.selectDefeasibleRulesQuery = readFile(pathToAIFFilesDirectory+"selectDefeasibleRules.sparql");
		this.selectContrarinessQuery = readFile(pathToAIFFilesDirectory+"selectContrarinessRules.sparql"); 
		this.clearPositionQuery = readFile(pathToAIFFilesDirectory+"clearPosition.sparql"); 
	}

	/**
	 * An AIF position is an AIF graph that an agent can use as a form of knowledge representation. 
	 * Under the hood it is an RDF dataset that is backed by the AIF ontology and hosted on a 
	 * Fuseki server. The AIF has no inherent semantics, but we may use for instance ASPIC semantics 
	 * to give meaning to a position. 
	 * 
	 * The word 'Position' is chosen since an agent may use a Position as the position that the agent 
	 * takes with respect to some argumentative process that does not correspond to its beliefs or 
	 * knowledge. E.g. an agent may represent another agent by taking a position for that agent and 
	 * argue in its best interests. 
	 * 
	 * This method creates a position with the specified name and inserts immediately the initial 
	 * arguments that are provided. The reasoner and AIF ontology are loaded in the dataset and the 
	 * agent may use the Fuseki capability to query the dataset too. */
	public final void makePosition(final String name, final AIFArgumentBuilder... initialArguments){
		// TODO: check if name is an existing dataset in fuseki
		// TODO: check if fuseki is running
		try {
			// TODO: Update when the fuseki capability has been updated so that these methods only return when they are finished.
			// Setup the dataset
			this.fuseki.addDataSet(name, false, "PREFIX aif: <uu:dialoguebuilder/nlpmodule/aif#>\r\n");
			Thread.sleep(100);
			this.fuseki.loadOntology(name, this.pathToAIFFilesDirectory+"AIF.ttl");
			Thread.sleep(100);
			this.fuseki.enableReasoner(name, false);
			Thread.sleep(10000);

			// Load the initial arguments
			addArgumentsToPosition(name, initialArguments);
		} catch (IOException e) { 
			e.printStackTrace();
		} catch (InterruptedException e) { 
			e.printStackTrace();
		}
	}
	
	/** Add arguments to an existing position; note that this requires that the position is hosted on fuseki. */
	public final void addArgumentsToPosition(final String name, final AIFArgumentBuilder... arguments){ 
		// TODO: check if name is an existing dataset in fuseki
		// TODO: check if fuseki is runnings
		try {
			this.fuseki.useRDFBase(name, "PREFIX aif: <uu:dialoguebuilder/nlpmodule/aif#>\r\n");
			// Add the arguments
			if(arguments.length > 0){
				StringBuffer update = new StringBuffer("INSERT DATA {\r\n");
				Arrays.asList(arguments).forEach((AIFArgumentBuilder argument) -> {update.append(argument.getAIFTriples().toString());});   
				this.fuseki.update(name, update.append("\r\n}\r\n").toString()); 
			} 
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/** Clear a position by removing all the arguments. This is faster than making a new position, since that requires a reboot of fuseki. */
	public final void clearPosition(final String name){
		try {
			this.fuseki.update(name, this.clearPositionQuery);
		} catch (IOException e) { 
			e.printStackTrace();
		}
	}

	/** At the moment this method returns the TOAST analysis of a position with the default settings of TOAST. */
	public final String getPositionSemanticalAnalysis(final String name){ 
		return getArgumentationTheory(name).getTOASTOutput().toString();
	} 

	/** Convert the AIF graph to an ASPIC specification. */
	private final ArgumentationTheory getArgumentationTheory(final String name){
		// Make a new theory
		UUID id = this.toast.newArgumentationTheory();
		ArgumentationTheory theory = toast.getArgumentationTheory(id);
		try{
			// Obtain the various elements that make up an ASPIC theory
			JSONObject premises = this.fuseki.query(name, this.selectPremisesQuery);
			JSONObject strictRules = this.fuseki.query(name, this.selectStrictRulesQuery);
			JSONObject defeasibleRules = this.fuseki.query(name, this.selectDefeasibleRulesQuery);
			JSONObject contrarinessRules = this.fuseki.query(name, this.selectContrarinessQuery);
			
			// Add the premises to the theory
			premises.getJSONObject("results").getJSONArray("bindings").forEach((Object object)->{ 
				theory.addPremise(((JSONObject) object).getJSONObject("premise").getString("value"));
			});

			// Add the strict/defeasible rules to the theory
			putInferenceRules(theory, strictRules, true);
			putInferenceRules(theory, defeasibleRules, false); 

			// Add the contrariness pairs to the theory
			contrarinessRules.getJSONObject("results").getJSONArray("bindings").forEach((Object object)->{
				JSONObject json = (JSONObject) object;
				theory.addContrarinessPair(json.getJSONObject("p").getString("value"),json.getJSONObject("q").getString("value"));
			});
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
		// We only use this theory once, so delete it from the TOAST context
		this.toast.removeArgumentationTheory(id);
		return theory;
	}
	
	/** Aux method to add inference rules to an argumentation theory from a SPARQL query result. */
	private final void putInferenceRules(final ArgumentationTheory theory, final JSONObject rulesObj, final boolean strict){
		final Map<String, List<String>> rules = new HashMap<>();
		rulesObj.getJSONObject("results").getJSONArray("bindings").forEach((Object object)->{ 
			JSONObject json = (JSONObject) object;
			String label = json.getJSONObject("label").getString("value");
			if(rules.get(label) == null){
				rules.put(label, new ArrayList<>());
				rules.get(label).add(json.getJSONObject("conclusion").getString("value"));
			}
			rules.get(label).add(json.getJSONObject("premiseInAntecedent").getString("value"));
		});
		for(String label : rules.keySet()){
			List<String> list = rules.get(label);
			if(strict) theory.putStrictRule(label, list.remove(0), (String[]) list.toArray(new String[list.size()]));
			else theory.putDefeasibleRule(label, list.remove(0), (String[]) list.toArray(new String[list.size()]));
		}
	}

	/** Read a text file and return its contents. */
	private final String readFile(final String pathToFile){
		StringBuffer result = new StringBuffer();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToFile));
			String line = "";
			while((line = bufferedReader.readLine()) != null)
				result.append(line).append("\r\n");
			bufferedReader.close();
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
		} catch (IOException e) { 
			e.printStackTrace();
		}
		return result.toString();
	}
}
