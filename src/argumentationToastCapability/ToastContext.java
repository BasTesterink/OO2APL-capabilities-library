package argumentationToastCapability;

import java.io.BufferedReader;
import java.io.File; 
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID; 
import oo2apl.agent.Context;
/**
 * The TOAST context stores argumentation theories that can be evaluated with TOAST. It exposes some API calls to 
 * save and read theories to and from files. The context should still be updated to allow argumentation queries 
 * with the exposed API. 
 * 
 * @author Bas Testerink
 */
public final class ToastContext implements Context {
	/** Argumentation theories */
	private final Map<UUID, ArgumentationTheory> theories; 
	/** URL of TOAST */
	private final String toastWebInterface; 
	
	public ToastContext(final String toastURL){
		this.toastWebInterface = toastURL;
		this.theories = new HashMap<>(); 
	}
	
	/** Create a new prolog engine by consulting a file. The returned ID is to be used when posing queries.  */
	public final UUID newArgumentationTheory() {
		ArgumentationTheory theory = new ArgumentationTheory(this.toastWebInterface);
		UUID id = UUID.randomUUID();
		while(this.theories.get(id) != null)
			id = UUID.randomUUID();
		this.theories.put(id, theory);
		return id;
	}
	
	/** Load argumentation theory specification from file. */
	public final UUID newArgumentationTheoryFromFile(final String pathToFile) throws FileNotFoundException, IOException {
		// Make a new theory
		UUID id = newArgumentationTheory();
		ArgumentationTheory theory = this.theories.get(id);
		
		// Read the file
		BufferedReader br = new BufferedReader(new FileReader(new File(pathToFile)));
		StringBuffer buffer = new StringBuffer();
		String line = "";
		while((line = br.readLine()) != null)
			buffer.append(line);
		br.close();
		
		// Add the specification to the theory
		theory.parseTOASTInput(buffer.toString());
		return id;
	}
	
	/** Save argumentation theory specification to file. */
	public final void saveToFile(final UUID theoryID, final String pathToFile) throws FileNotFoundException, IOException {
		ArgumentationTheory theory = this.theories.get(theoryID);
		FileOutputStream stream = new FileOutputStream(pathToFile);
		stream.write(theory.toTOASTInput().getBytes());
		stream.flush();
		stream.close();
	}

	/** Obtain a theory so it can be modified. */
	public final ArgumentationTheory getArgumentationTheory(final UUID id){
		return this.theories.get(id);
	}
	
	/** Remove a theory. */
	public final ArgumentationTheory removeArgumentationTheory(final UUID id){
		return this.theories.remove(id);
	} 
}
