package argumentationAIFCapability;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
/**
 * Argumentbuilders help with the programmatic specification of arguments. The idea is to implement for each 
 * scheme a builder (currently not all schemes are supported). You can also specify your own schemes. 
 * 
 * A scheme builder ensures that each component of an argument has an I-node or S-node and you can obtain them 
 * from the builder to reuse in the specification of other arguments. Finally, a builder can be converted into 
 * AIF RDF triples that can be inserted in a position.
 * 
 * @author Bas Testerink
 */
public abstract class AIFArgumentBuilder { 
	protected static final AtomicLong counter = new AtomicLong(0);  // Used for generating ids 
	private final StringBuffer accumulator;							// Used for accumulating added I-Nodes
	
	public AIFArgumentBuilder(){
		this.accumulator = new StringBuffer();
	}
	
	/** Get the ID if it is present or otherwise generate a new ID.  */
	protected final String getID(final Optional<String> argument){
		return argument.isPresent() ? argument.get() : ("n"+(counter.incrementAndGet()));
	}
	
	/** Add an I-node description. This will generate a new I-node if necessary. */
	protected final Optional<String> iNodeText(final Optional<String> iNodeIdentifier, final String description){
		String iNodeIdentifierStr = getID(iNodeIdentifier);
		this.accumulator.append("aif:").append(iNodeIdentifierStr).append(" aif:iNodeText \"").append(description).append("\".\r\n");
		return Optional.of(iNodeIdentifierStr);
	}
	
	/** Protected visible method so that builders can implement their conversion to AIF triples. */
	protected abstract StringBuffer convertToAIFTriples();
	
	/** Package visible method so that the context can access the the AIF triples. */
	final StringBuffer getAIFTriples(){ 
		// Add any asserted iNode texts and return
		return convertToAIFTriples().append(this.accumulator);
	}
}
