package argumentationAIFCapability.argumentbuilders;

import java.util.Optional;

import argumentationAIFCapability.AIFArgumentBuilder;
/** This builder can be used if you want to create a new INode. 
 * Note that arguments also support the addition of their premises and conclusion through their 
 * descriptions. These will automatically make new INodes which you can obtain with the getters from 
 * the argument builders.  
 * */
public final class PropositionBuilder extends AIFArgumentBuilder {
	// Components of this argument
	private Optional<String> proposition = Optional.empty(); // "p"

	/** Set the proposition by providing its description. This will cause the creation of a new I-Node with 
	 * the given description, of which the ID becomes the ID of the premise. Example: "p". */
	public final PropositionBuilder setPropositionByDescription(final String description) throws AIFArgumentBuilderException{
		if(this.proposition.isPresent()) throw new AIFArgumentBuilderException();
		this.proposition = super.iNodeText(Optional.empty(), description);
		return this;
	} 

	/** Returns the AIF triples of this argument. If any values are not set (such as premises, conclusions, etc.) then 
	 * they will be generated automatically. You cannot set the values afterwards, only obtain them with the getters. */
	protected final StringBuffer convertToAIFTriples() { 
		// If no proposition description is specified then a new one is created
		this.proposition = this.proposition.isPresent() ? this.proposition : Optional.of(getID(Optional.empty()));
		return (new StringBuffer())
				.append("aif:").append(this.proposition.get()).append(" a aif:I-Node.\r\n");
	}
	
	// Getter
	public final String getProposition(){ return this.proposition.get(); }
}
