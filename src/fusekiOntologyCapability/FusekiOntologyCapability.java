package fusekiOntologyCapability;

import oo2apl.agent.AgentBuilder;
/**
 * 
 * This is a capability that allows an agent to control a Fuseki2 server, store and remove 
 * datasets from it, and query/update datasets. This is ideal for semantic web based applications 
 * that rely on ontological reasoning. 
 * 
 * @author Bas Testerink
 *
 */
public final class FusekiOntologyCapability extends AgentBuilder {
	/** It is assumed for now that Fuseki will be hosted locally on the provided port, and that Fuseki2 
	 * is locally installed in the provided directory. The context enables some basic functionalities 
	 * such as starting/stopping the server, adding/removing datasets and querying/updating datasets.  
	 * Note: this is an early version. Most methods still have to be revised to accord for consecutive 
	 * method calls that go to quick for the Fuseki server.*/
	public FusekiOntologyCapability(final int port, final String fusekiDirectory, final String pathToConfigTemplatesDirectory){
		super.addContext(new FusekiContext(port, fusekiDirectory, pathToConfigTemplatesDirectory));
	}
}
