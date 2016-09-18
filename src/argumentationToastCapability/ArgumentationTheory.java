package argumentationToastCapability;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

import org.json.JSONObject;
 

/**
 * An argumentation theory is an ASPIC concept. This class serves as a container for the 
 * specification of a theory which can be used as input for for instance TOAST. 
 * 
 * @author Bas Testerink
 */
public final class ArgumentationTheory { 
	private static enum LinkPreference {LAST, WEAKEST};
	private static enum Semantics {GROUNDED, PREFERRED, SEMI_STABLE, STABLE};
	/** The result of the last TOAST call  */
	private JSONObject toastResponse;
	/** Whether the theory was updated since the last TOAST call */
	private boolean upToDate = false;
	/** URL of TOAST */
	private final String toastWebInterface; 
	// All the different storages
	private final List<String> axioms, premises, assumptions;
	private final List<Entry<String, String>> propositionalPreferences, rulePreferences, contrarinessPairs;
	private final Map<String, String> strictRules, defeasibleRules;
	private LinkPreference linkPreference;
	private Semantics semantics;
	private boolean closeUnderTransposition;
	

	/** By default weakest link preference is used. By default grounded semantics are used. 
	 * Also closure under transposition is turned off by default.*/
	protected ArgumentationTheory(final String toastWebInterface){
		// Initialize all the data storages
		this.axioms = new LinkedList<>();
		this.premises = new LinkedList<>();
		this.assumptions = new LinkedList<>();
		this.propositionalPreferences = new LinkedList<>();
		this.rulePreferences = new LinkedList<>();
		this.contrarinessPairs = new LinkedList<>(); 
		this.strictRules = new HashMap<>();
		this.defeasibleRules = new HashMap<>();
		this.toastWebInterface = toastWebInterface;
		this.linkPreference = LinkPreference.WEAKEST;
		this.closeUnderTransposition = false;
		this.semantics = Semantics.GROUNDED;
	}
	
	/** Get the JSONObject that TOAST returns. */
	public final JSONObject getTOASTOutput(){
		// First check whether to update the TOAST output
		updateWithTOASTWebserviceIfNecessary();
		// Return a clone (note, an actual clone method is not available in the JSON library, hence the String parsing)
		return new JSONObject(this.toastResponse.toString());
	}

	
	/** Checks whether TOAST was used to evaluate this theory since the last time that this theory was updated. If not, 
	 * then TOAST is called through its web interface. */
	private final void updateWithTOASTWebserviceIfNecessary(){
		if(!this.upToDate){  
			try {
				// Make the connection
				HttpURLConnection connection = (HttpURLConnection) (new URL(this.toastWebInterface)).openConnection();

				// We will send (through POST) the theory in JSON format and receive the evaluation in JSON format as well
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				connection.setRequestProperty("Accept", "application/json");
				connection.setRequestMethod("POST"); 

				// Write this theory to the output stream/TOAST interface
				OutputStream os = connection.getOutputStream();
				os.write(this.toTOASTInput().getBytes("UTF-8"));
				os.flush();

				// Obtain the result
				StringBuilder result = new StringBuilder();   
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
					String line = null;  
					while ((line = br.readLine()) != null) {  
						result.append(line + "\n");  
					}
					br.close();  
					this.toastResponse = new JSONObject(result.toString());
				} else {
					// TODO: thow exception 
					System.out.println("Cannot evaluate with TOAST. ");
				}  
			} catch(Exception e){
				e.printStackTrace();
			}
			
			// Theory is now up do date (note: don't concurrently update the theory whilst evaluating)
			this.upToDate = true;
		}
	}
	
	/** Transforms the theory in a TOAST-readable JSON string. This is also the format that the capability uses 
	 * to store theories in files. */
	protected final String toTOASTInput(){
		StringBuffer result = new StringBuffer();
		result.append('{');
		result.append("\"axioms\": \""); 
		this.axioms.forEach((String axiom) -> {result.append(axiom).append(';');});
		result.append("\",\r\n");
		result.append("\"premises\": \""); 
		this.premises.forEach((String axiom) -> {result.append(axiom).append(';');});
		result.append("\",\r\n");
		result.append("\"assumptions\": \""); 
		this.assumptions.forEach((String axiom) -> {result.append(axiom).append(';');});
		result.append("\",\r\n");
		result.append("\"kbPrefs\": \""); 
		this.propositionalPreferences.forEach((Entry<String, String> pair) -> {result.append(pair.getKey()).append('<').append(pair.getValue()).append(';');});
		result.append("\",\r\n");
		result.append("\"rules\": \""); 
		this.strictRules.keySet().forEach((String label) -> {result.append(" [").append(label).append("] ").append(this.strictRules.get(label)).append(';');});
		this.defeasibleRules.keySet().forEach((String label) -> {result.append(" [").append(label).append("] ").append(this.defeasibleRules.get(label)).append(';');});
		result.append("\",\r\n");
		result.append("\"rulePrefs\": \""); 
		this.rulePreferences.forEach((Entry<String, String> pair) -> {result.append('[').append(pair.getKey()).append("]<[").append(pair.getValue()).append("];");});
		result.append("\",\r\n");
		result.append("\"contrariness\": \""); 
		this.contrarinessPairs.forEach((Entry<String, String> pair) -> {result.append(pair.getKey()).append('^').append(pair.getValue()).append(';');});
		result.append("\",\r\n");
		result.append("\"link\":"); // TODO: the webpage doesn't show if these are the correct "strings"
		if(this.linkPreference == LinkPreference.WEAKEST) result.append("\"weakest\"");
		else if(this.linkPreference == LinkPreference.LAST) result.append("\"last\"");
		result.append(",\r\n");
		result.append("\"semantics\":"); // TODO: the webpage doesn't show if these are the correct "strings"
		if(this.semantics == Semantics.GROUNDED) result.append("\"grounded\"");
		else if(this.semantics == Semantics.PREFERRED) result.append("\"preferred\"");
		else if(this.semantics == Semantics.STABLE) result.append("\"stable\"");
		else if(this.semantics == Semantics.SEMI_STABLE) result.append("\"semi-stable\"");
		result.append(",\r\n");
		result.append("\"transposition\":"); 
		result.append(this.closeUnderTransposition);
		result.append('}');
		return result.toString();
	}
	
	/** Aux. method to convert a rule to a string representation. */
	private final String makeRuleString(final String consequent, final String arrow, final String... conditions){
		StringBuffer rule = new StringBuffer();
		for(int i = 0; i < conditions.length; i++){
			rule.append(conditions[i]);
			if(conditions.length != i+1) rule.append(',');
		}
		return rule.append(arrow).append(consequent).toString();
	}
	
	/** Parse the JSON string that is used to communicate with TOAST and add all interpreted 
	 * premises, rules, etc. to this theory.  */
	protected final void parseTOASTInput(final String input){
		// Parsing first to a JSON object in order to make life a bit easier
		JSONObject json = new JSONObject(input);
		// Parse the various data
		Arrays.asList(json.getString("axioms").split(";")).forEach((String axiom)->{addAxiom(axiom);});
		Arrays.asList(json.getString("premises").split(";")).forEach((String premise)->{addPremise(premise);});
		Arrays.asList(json.getString("assumptions").split(";")).forEach((String assumption)->{addAssumption(assumption);});
		Arrays.asList(json.getString("kbPrefs").split(";")).forEach((String pair)->{addPropositionalPreference(
				pair.substring(0,pair.indexOf('<')), 
				pair.substring(pair.indexOf('<')+1));});
		Arrays.asList(json.getString("rulePrefs").split(";")).forEach((String pair)->{addRulePreference(
				pair.substring(1,pair.indexOf('<')-1), 
				pair.substring(pair.indexOf('<')+2, pair.lastIndexOf(']')));});
		Arrays.asList(json.getString("contrariness").split(";")).forEach((String pair)->{addContrarinessPair(
				pair.substring(0,pair.indexOf('^')), 
				pair.substring(pair.indexOf('^')+1));});
		Arrays.asList(json.getString("rules").split(";")).forEach((String rule)->{
			if(rule.contains("->"))
				putStrictRule(
						rule.substring(rule.indexOf('[')+1, rule.indexOf(']')),  				// Label
						rule.substring(rule.indexOf('>')+1), 									// Consequent
						rule.substring(rule.indexOf(']')+2, rule.indexOf("->")).split(","));	// Conditions
			else if(rule.contains("=>"))
				putDefeasibleRule(
						rule.substring(rule.indexOf('[')+1, rule.indexOf(']')),  				// Label
						rule.substring(rule.indexOf('>')+1), 									// Consequent
						rule.substring(rule.indexOf(']')+2, rule.indexOf("=>")).split(","));	// Conditions
		});
		String link = json.getString("link");
		if(link.equals("weakest")) setLinkPreference(LinkPreference.WEAKEST);
		else if(link.equals("last")) setLinkPreference(LinkPreference.LAST);
		String semantics = json.getString("semantics");
		if(semantics.equals("grounded")) setSemantics(Semantics.GROUNDED);
		else if(semantics.equals("preferred")) setSemantics(Semantics.PREFERRED);
		else if(semantics.equals("semi-stable")) setSemantics(Semantics.SEMI_STABLE);
		else if(semantics.equals("stable")) setSemantics(Semantics.STABLE);
		this.setCloseUnderTransposition(Boolean.parseBoolean(json.getString("transposition")));
	}
	
	///////////////////////////////
	//////// DATA UPDATES /////////
	///////////////////////////////
	// TODO: add comments to explain each concept
	
	public final ArgumentationTheory addAxiom(final String axiom){ this.axioms.add(axiom); this.upToDate = false; return this;} 
	
	public final ArgumentationTheory removeAxiom(final String axiom){ this.axioms.remove(axiom); this.upToDate = false; return this; }

	public final ArgumentationTheory addPremise(final String premise){ this.premises.add(premise); this.upToDate = false; return this; }
	
	public final ArgumentationTheory removePremise(final String premise){ this.premises.remove(premise); this.upToDate = false; return this; }
	
	public final ArgumentationTheory addAssumption(final String assumption){ this.assumptions.add(assumption); this.upToDate = false; return this; }
	
	public final ArgumentationTheory removeAssumption(final String assumption){ this.assumptions.remove(assumption); this.upToDate = false; return this; }
	
	public final ArgumentationTheory addPropositionalPreference(final String preferredProposition, final String dispreferredProposition){ this.propositionalPreferences.add(new SimpleEntry<>(preferredProposition, dispreferredProposition)); this.upToDate = false; return this; }
	
	public final ArgumentationTheory removePropositionalPreference(final String preferredProposition, final String dispreferredProposition){ this.propositionalPreferences.remove(new SimpleEntry<>(preferredProposition, dispreferredProposition)); this.upToDate = false; return this; }

	public final ArgumentationTheory putStrictRule(final String ruleLabel, final String consequent, final String... conditions){ this.strictRules.put(ruleLabel, makeRuleString(consequent, "->", conditions)); this.upToDate = false; return this; }
		
	public final ArgumentationTheory removeStrictRule(final String ruleLabel){ this.strictRules.remove(ruleLabel); this.upToDate = false; return this; }
	
	public final ArgumentationTheory putDefeasibleRule(final String ruleLabel, final String consequent, final String... conditions){ this.defeasibleRules.put(ruleLabel, makeRuleString(consequent, "=>", conditions)); this.upToDate = false; return this; }
	
	public final ArgumentationTheory removeDefeasibleRule(final String ruleLabel){ this.defeasibleRules.remove(ruleLabel); this.upToDate = false; return this; }
	
	public final ArgumentationTheory addRulePreference(final String preferredRuleLabel, final String dispreferredRuleLabel){ this.rulePreferences.add(new SimpleEntry<>(preferredRuleLabel, dispreferredRuleLabel)); this.upToDate = false; return this; }
	
	public final ArgumentationTheory removeRulePreference(final String preferredRuleLabel, final String dispreferredRuleLabel){ this.rulePreferences.remove(new SimpleEntry<>(preferredRuleLabel, dispreferredRuleLabel)); this.upToDate = false; return this; }

	public final ArgumentationTheory addContrarinessPair(final String contraryProposition, final String proposition){ this.contrarinessPairs.add(new SimpleEntry<>(contraryProposition, proposition));  this.upToDate = false; return this; }

	public final ArgumentationTheory removeContrarinessPair(final String contraryProposition, final String proposition){ this.contrarinessPairs.remove(new SimpleEntry<>(contraryProposition, proposition));  this.upToDate = false; return this; }
	
	public final ArgumentationTheory addContradictoryPair(final String propositionA, final String propositionB){ this.contrarinessPairs.add(new SimpleEntry<>(propositionA, propositionB)); this.contrarinessPairs.add(new SimpleEntry<>(propositionB, propositionA)); this.upToDate = false; return this; }

	public final ArgumentationTheory removeContradictoryPair(final String propositionA, final String propositionB){ this.contrarinessPairs.remove(new SimpleEntry<>(propositionA, propositionB)); this.contrarinessPairs.remove(new SimpleEntry<>(propositionB, propositionA)); this.upToDate = false; return this; }
	
	public final ArgumentationTheory setLinkPreference(final LinkPreference preference){ this.linkPreference = preference; this.upToDate = false; return this; }
	
	public final ArgumentationTheory setCloseUnderTransposition(final boolean closeUnderTransposition){ this.closeUnderTransposition = closeUnderTransposition; this.upToDate = false; return this; }
	
	public final ArgumentationTheory setSemantics(final Semantics semantics){ this.semantics = semantics; this.upToDate = false; return this; }
	
	// TODO:
	// A nice query API should still be designed that fits all argumentation needs.

//	public final boolean isSupportedProposition(final String proposition){
//		// First check whether to update the TOAST output
//		updateWithTOASTWebserviceIfNecessary();
//
//		// Check whether the proposition is supported
//		return this.toastResponse.getJSONArray("acceptableConclusions")
//				.getJSONArray(0).toList().stream()
//				.filter((Object o)->{return ((String) o).toString().equals(proposition);})
//				.findFirst().isPresent();
//	}
//	
//	public final String[] supportedPropositions(final String proposition){
//		// First check whether to update the TOAST output
//		updateWithTOASTWebserviceIfNecessary();
//
//		// Obtain and return all supported propositions
//		List<String> result = new ArrayList<>();
//		this.toastResponse.getJSONArray("acceptableConclusions").getJSONArray(0).forEach(
//				(Object o) -> { result.add((String)o); }
//		);
//		return result.toArray(new String[result.size()]);
//	}
}
