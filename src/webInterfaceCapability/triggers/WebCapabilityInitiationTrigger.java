package webInterfaceCapability.triggers;

import oo2apl.agent.ExternalProcessToAgentInterface;
import oo2apl.agent.Trigger;
/**
 * Trigger that is used to initialize the web capability.
 * @author Bas Testerink
 */
public final class WebCapabilityInitiationTrigger implements Trigger {
	private final int port;
	private final ExternalProcessToAgentInterface publisher;
	
	public WebCapabilityInitiationTrigger(final int port, final ExternalProcessToAgentInterface publisher){
		this.port = port;
		this.publisher = publisher;
	}
	
	public final int getPort(){ return this.port; }
	public final ExternalProcessToAgentInterface getPublisher(){ return this.publisher; }
}
