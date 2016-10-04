package argumentationAIFCapability.argumentbuilders;

import java.util.Optional;

import argumentationAIFCapability.AIFArgumentBuilder;
/**
 * The popular opinion argument is used to appeal to common sense or to attend someone to common 
 * knowledge. For example "it is commonly known that raining makes the street wet" can be the premise 
 * where the conclusion becomes (defeasibly) "raining makes the street wet". 
 * 
 * @author Bas Testerink
 */
public final class PopularOpinionBuilder extends AIFArgumentBuilder {
	// Components of this argument
	private Optional<String> 	generalAcceptance = Optional.empty(),	// "Agent A is in a position to know p" 
								knowledgePosition = Optional.empty(),	// "p"
								sNode = Optional.empty(); 				// Node that ties the premise and conclusion together

	/** Set the general acceptance premise of this argument by providing its description. This will cause the creation of a new I-Node with 
	 * the given description, of which the ID becomes the ID of the premise. Example: "It is commonly known that p". */
	public final PopularOpinionBuilder setGeneralAcceptanceByDescription(final String description) throws AIFArgumentBuilderException{
		if(this.generalAcceptance.isPresent()) throw new AIFArgumentBuilderException();
		this.generalAcceptance = super.iNodeText(Optional.empty(), description);
		return this;
	}

	/** Set the general acceptance premise of this argument by using an ID. */
	public final PopularOpinionBuilder setGeneralAcceptanceByID(final String id) throws AIFArgumentBuilderException{
		if(this.generalAcceptance.isPresent()) throw new AIFArgumentBuilderException();
		this.generalAcceptance = Optional.of(id);
		return this;
	} 

	/** Set the knowledge position of this argument by providing its description. This will cause the creation of a new I-Node with 
	 * the given description, of which the ID becomes the conclusion. Example: "p" (if p was asserted as common knowledge). */
	public final PopularOpinionBuilder setKnowledgePositionByDescription(final String description) throws AIFArgumentBuilderException{
		if(this.knowledgePosition.isPresent()) throw new AIFArgumentBuilderException();
		this.knowledgePosition = super.iNodeText(Optional.empty(), description);
		return this;
	}

	/** Set the knowledge position of this argument by using an ID. */
	public final PopularOpinionBuilder setKnowledgePositionByID(final String id) throws AIFArgumentBuilderException{
		if(this.knowledgePosition.isPresent()) throw new AIFArgumentBuilderException();
		this.knowledgePosition = Optional.of(id);
		return this;
	}

	/** Returns the AIF triples of this argument. If any values are not set (such as premises, conclusions, etc.) then 
	 * they will be generated automatically. You cannot set the values afterwards, only obtain them with the getters. */
	protected final StringBuffer convertToAIFTriples(){
		// Generate the IDs of the nodes in the AIF graph
		this.sNode = Optional.of(getID(Optional.empty()));
		this.generalAcceptance = this.generalAcceptance.isPresent() ? this.generalAcceptance : Optional.of(getID(Optional.empty())); 
		this.knowledgePosition = this.knowledgePosition.isPresent() ? this.knowledgePosition : Optional.of(getID(Optional.empty()));

		// Make the triples 
		return (new StringBuffer())
				.append("aif:").append(this.sNode.get()).append(" aif:s-inference-node-fulfil aif:popular-opinion-scheme.\r\n")
				.append("aif:").append(this.generalAcceptance.get()).append(" aif:i-fulfil aif:general-acceptance-description.\r\n")
				.append("aif:").append(this.knowledgePosition.get()).append(" aif:i-fulfil aif:knowledge-position-description.\r\n") 
				.append("aif:").append(this.sNode.get()).append(" aif:premise aif:").append(this.generalAcceptance.get()).append(".\r\n") 
				.append("aif:").append(this.sNode.get()).append(" aif:conclusion aif:").append(this.knowledgePosition.get()).append(".\r\n");  
	} 

	// Getters
	public final String getGeneralAcceptance(){ return this.generalAcceptance.get(); } 
	public final String getKnowledgePosition(){ return this.knowledgePosition.get(); }
	public final String getSNode(){ return this.sNode.get(); }
}
