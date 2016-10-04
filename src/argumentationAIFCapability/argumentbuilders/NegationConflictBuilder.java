package argumentationAIFCapability.argumentbuilders;

import java.util.Optional;

import argumentationAIFCapability.AIFArgumentBuilder;
/**
 * This argument captures classical negation. If "p" and "not p" are premises, then this argument can be 
 * used to point out that they cannot both be true at the same time.
 * 
 * @author Bas Testerink
 */
public final class NegationConflictBuilder extends AIFArgumentBuilder {
	// Components of this argument
	private Optional<String> 	positiveProposition = Optional.empty(),	// "p" 
								negativeProposition = Optional.empty(),	// "not p"
								sNode = Optional.empty(); 				// Node that ties the conflicted and conflicting element together

	/** Set the positive proposition of this argument by providing its description. This will cause the creation of a new I-Node with 
	 * the given description, of which the ID becomes the ID of the premise. Example: "p". */
	public final NegationConflictBuilder setPositivePropositionByDescription(final String description) throws AIFArgumentBuilderException{
		if(this.positiveProposition.isPresent()) throw new AIFArgumentBuilderException();
		this.positiveProposition = super.iNodeText(Optional.empty(), description);
		return this;
	}

	/** Set the positive proposition of this argument by using an ID. */
	public final NegationConflictBuilder setPositivePropositionByID(final String id) throws AIFArgumentBuilderException{
		if(this.positiveProposition.isPresent()) throw new AIFArgumentBuilderException();
		this.positiveProposition = Optional.of(id);
		return this;
	} 

	/** Set the negative proposition of this argument by providing its description. This will cause the creation of a new I-Node with 
	 * the given description, of which the ID becomes the conclusion. Example: "not p". */
	public final NegationConflictBuilder setNegativePropositionByDescription(final String description) throws AIFArgumentBuilderException{
		if(this.negativeProposition.isPresent()) throw new AIFArgumentBuilderException();
		this.negativeProposition = super.iNodeText(Optional.empty(), description);
		return this;
	}

	/** Set the negative proposition of this argument by using an ID. */
	public final NegationConflictBuilder setNegativePropositionByID(final String id) throws AIFArgumentBuilderException{
		if(this.negativeProposition.isPresent()) throw new AIFArgumentBuilderException();
		this.negativeProposition = Optional.of(id);
		return this;
	}

	/** Returns the AIF triples of this argument. If any values are not set (such as premises, conclusions, etc.) then 
	 * they will be generated automatically. You cannot set the values afterwards, only obtain them with the getters. */
	protected final StringBuffer convertToAIFTriples(){
		// Generate the IDs of the nodes in the AIF graph
		this.sNode = Optional.of(getID(Optional.empty()));
		this.positiveProposition = this.positiveProposition.isPresent() ? this.positiveProposition : Optional.of(getID(Optional.empty())); 
		this.negativeProposition = this.negativeProposition.isPresent() ? this.negativeProposition : Optional.of(getID(Optional.empty()));

		// Make the triples 
		return (new StringBuffer())
				.append("aif:").append(this.sNode.get()).append(" aif:s-conflict-node-fulfil aif:negation-conflict-scheme.\r\n")
				.append("aif:").append(this.positiveProposition.get()).append(" aif:i-fulfil aif:positive-proposition-description.\r\n")
				.append("aif:").append(this.negativeProposition.get()).append(" aif:i-fulfil aif:negative-proposition-description.\r\n") 
				.append("aif:").append(this.sNode.get()).append(" aif:conflictedElement aif:").append(this.positiveProposition.get()).append(".\r\n") 
				.append("aif:").append(this.sNode.get()).append(" aif:conflictingElement aif:").append(this.negativeProposition.get()).append(".\r\n");       
	} 

	// Getters
	public final String getPositiveProposition(){ return this.positiveProposition.get(); } 
	public final String getNegativeProposition(){ return this.negativeProposition.get(); }
	public final String getSNode(){ return this.sNode.get(); }
}
