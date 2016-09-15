package webInterfaceCapability;

import java.net.URI;

import oo2apl.agent.Trigger;
/** 
 * Interface for converters that can convert a URI with a request into a trigger to be adopted internally.
 * @author Bas Testerink
 */
public interface HTTPTriggerConverter {
	/** Return value if conversion did not succeed. */
	public static final Trigger NOT_CONVERTED = new Trigger(){}; 
	/** Try to convert the input to a trigger. If this fails, then return {@link HTTPTriggerConverter.NOT_CONVERTED}*/
	public Trigger convert(URI uri, String request); 
}
