package tuPrologCapability;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoMoreSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Theory;
import oo2apl.agent.Context;
/**
 * This context exposes tuProlog engines to an agent. Multiple engines can be 
 * used at once. 
 * 
 * @author Bas Testerink
 */
public final class TuPrologContext implements Context {
	/** The currently active engines */
	private final Map<UUID, Prolog> prologEngines;
	
	public TuPrologContext(){
		this.prologEngines = new HashMap<>();
	}
	
	/** Create a new prolog engine by consulting a file. The returned ID is to be used when posing queries.  */
	public final UUID newPrologEngine(final String pathToPrologFile) throws InvalidTheoryException, FileNotFoundException, IOException{
		Prolog engine = new Prolog();
		engine.setTheory(new Theory(new FileInputStream(pathToPrologFile))); 
		UUID id = UUID.randomUUID();
		while(this.prologEngines.get(id) != null)
			id = UUID.randomUUID();
		this.prologEngines.put(id, engine);
		return id;
	}
	
	/** Execute a given query for a given engine and return true iff there is an answer. */
	public final boolean queryIfTrue(final UUID engineID, final String query) throws MalformedGoalException { 
		return queryFirstResult(engineID, query).isSuccess();
	}
	
	/** Obtain for a given engine and query the first result that satisfied the query. It is not possible to 
	 * request the other answers afterwards, so if all answers are required then use getAllQueryResults.  */
	public final SolveInfo queryFirstResult(final UUID engineID, final String query) throws MalformedGoalException {
		Prolog engine = this.prologEngines.get(engineID);
		return engine.solve(query);
	}
	
	/** Get a list of results for a given engine and query. To convert results to JSON strings you can use 
	 * TuPrologCapability.resultToJSONString */
	public final List<SolveInfo> queryAllResults(final UUID engineID, final String query) throws MalformedGoalException {
		Prolog engine = this.prologEngines.get(engineID);
		List<SolveInfo> results = new ArrayList<>();
		SolveInfo result = engine.solve(query);
		if(result.isSuccess()) results.add(result);
		while(engine.hasOpenAlternatives()){
			try {
				result = engine.solveNext();
				if(result.isSuccess()) results.add(result);
			} catch (NoMoreSolutionException e) { 
				// done
			}
		}
		return results;
	}
	
	/** Save the given engine to a given file. */
	public final void saveToFile(final UUID engineID, final String pathToFile) throws FileNotFoundException, IOException {
		Prolog engine = this.prologEngines.get(engineID);
		FileOutputStream stream = new FileOutputStream(pathToFile);
		stream.write(engine.getTheory().toString().getBytes());
		stream.flush();
		stream.close();
	}
}
