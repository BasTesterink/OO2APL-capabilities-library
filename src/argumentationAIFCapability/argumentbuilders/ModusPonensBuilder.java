package argumentationAIFCapability.argumentbuilders;

import java.util.Optional;

import argumentationAIFCapability.AIFArgumentBuilder;
/**
 * Modus ponens is a classical logical (strict) inference rule. The basic argument is that if 
 * "p" and "if p then q" are true, then "q" must be true too.
 * 
 * @author Bas Testerink
 */
public final class ModusPonensBuilder extends AIFArgumentBuilder {
	// Components of this argument
	private Optional<String> minorPremise = Optional.empty(),	// "p"
							 majorPremise = Optional.empty(),	// "p->q"
							 conclusion = Optional.empty(),		// "q"
							 sNode = Optional.empty(); 			// Node that ties the premises and conclusion together

	/** Set the minor premise of this argument by providing its description. This will cause the creation of a new I-Node with 
	 * the given description, of which the ID becomes the ID of the premise. Example: "p". */
	public final ModusPonensBuilder setMinorPremiseByDescription(final String description) throws AIFArgumentBuilderException{
		if(this.minorPremise.isPresent()) throw new AIFArgumentBuilderException();
		this.minorPremise = super.iNodeText(Optional.empty(), description);
		return this;
	}
	
	/** Set the minor premise of this argument by using an ID. */
	public final ModusPonensBuilder setMinorPremiseByID(final String id) throws AIFArgumentBuilderException{
		if(this.minorPremise.isPresent()) throw new AIFArgumentBuilderException();
		this.minorPremise = Optional.of(id);
		return this;
	}

	/** Set the major premise of this argument by providing its description. This will cause the creation of a new I-Node with 
	 * the given description, of which the ID becomes the ID of the premise. Example: "p->q"*/
	public final ModusPonensBuilder setMajorPremiseByDescription(final String description) throws AIFArgumentBuilderException{
		if(this.majorPremise.isPresent()) throw new AIFArgumentBuilderException();
		this.majorPremise = super.iNodeText(Optional.empty(), description);
		return this;
	}
	
	/** Set the major premise of this argument by using an ID. */
	public final ModusPonensBuilder setMajorPremiseByID(final String id) throws AIFArgumentBuilderException{
		if(this.majorPremise.isPresent()) throw new AIFArgumentBuilderException();
		this.majorPremise = Optional.of(id);
		return this;
	}
	
	/** Set the conclusion of this argument by providing its description. This will cause the creation of a new I-Node with 
	 * the given description, of which the ID becomes the conclusion. Example: "q" (if minor and major premises are for instance "p" and "p->q"). */
	public final ModusPonensBuilder setConclusionByDescription(final String description) throws AIFArgumentBuilderException{
		if(this.conclusion.isPresent()) throw new AIFArgumentBuilderException();
		this.conclusion = super.iNodeText(Optional.empty(), description);
		return this;
	}
	
	/** Set the conclusion of this argument by using an ID. */
	public final ModusPonensBuilder setConclusionByID(final String id) throws AIFArgumentBuilderException{
		if(this.conclusion.isPresent()) throw new AIFArgumentBuilderException();
		this.conclusion = Optional.of(id);
		return this;
	}
	
	/** Returns the AIF triples of this argument. If any values are not set (such as premises, conclusions, etc.) then 
	 * they will be generated automatically. You cannot set the values afterwards, only obtain them with the getters. */
	protected final StringBuffer convertToAIFTriples(){
		// Generate the IDs of the nodes in the AIF graph
		this.sNode = Optional.of(getID(Optional.empty()));
		this.minorPremise = this.minorPremise.isPresent() ? this.minorPremise : Optional.of(getID(Optional.empty()));
		this.majorPremise = this.majorPremise.isPresent() ? this.majorPremise : Optional.of(getID(Optional.empty()));
		this.conclusion = this.conclusion.isPresent() ? this.conclusion : Optional.of(getID(Optional.empty()));
		
		// Make the triples 
		return (new StringBuffer())
		.append("aif:").append(this.sNode.get()).append(" aif:s-inference-node-fulfil aif:modus-ponens-scheme.\r\n")
		.append("aif:").append(this.minorPremise.get()).append(" aif:i-fulfil aif:minor-premise-description.\r\n")
		.append("aif:").append(this.majorPremise.get()).append(" aif:i-fulfil aif:major-premise-description.\r\n")
		.append("aif:").append(this.conclusion.get()).append(" aif:i-fulfil aif:modus-ponens-conclusion-description.\r\n")
		.append("aif:").append(this.sNode.get()).append(" aif:premise aif:").append(this.minorPremise.get()).append(".\r\n")
		.append("aif:").append(this.sNode.get()).append(" aif:premise aif:").append(this.majorPremise.get()).append(".\r\n")
		.append("aif:").append(this.sNode.get()).append(" aif:conclusion aif:").append(this.conclusion.get()).append(".\r\n");  
	} 
	
	// Getters
	public final String getMinorPremise(){ return this.minorPremise.get(); }
	public final String getMajorPremise(){ return this.majorPremise.get(); }
	public final String getConclusion(){ return this.conclusion.get(); }
	public final String getSNode(){ return this.sNode.get(); }
}
