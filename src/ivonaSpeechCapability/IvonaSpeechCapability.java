package ivonaSpeechCapability;

import oo2apl.agent.AgentBuilder;

/**
 * The Ivona speech capability allows the agent to speak out loud by using the Ivona cloud service.
 * As of the moment of writing it is possible to get a limited but free beta development account at Ivona. 
 * Note that Ivona is not open source. See https://www.ivona.com/us/for-business/speech-cloud/ for more info on 
 * how to get credentials. 
 * 
 * @author Bas Testerink
 */
public final class IvonaSpeechCapability extends AgentBuilder {
	
	/** Creates a new speech capability that can be used by agents. For a PlanToAgentInterface planInt you can use 
	 * planInt.getContext(IvonaContext.class).speak("Hello world.") for instance to speak. By default the voice is 
	 * set to English. 
	 *  @param pathToBuffer Path to the folder where the utterances are buffered. e.g. /home/ivonabuffer/ 
	 *  @param pathToCredentials Path to the Ivona credentials file. */
	public IvonaSpeechCapability(final String pathToBuffer, final String pathToCredentials){
		super.addContext(new IvonaContext(pathToBuffer, pathToCredentials)); 
	} 
}
