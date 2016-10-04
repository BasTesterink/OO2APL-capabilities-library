package argumentationAIFCapability.argumentbuilders;

import java.util.Optional;

import argumentationAIFCapability.AIFArgumentBuilder;
/**
 * This argument captures the intuition that if someone is in a position to know "p", and 
 * that person says "p" is (not) true, then "p" is (defeasibly) (not) true.
 * 
 * @author Bas Testerink
 */
public final class PositionToKnowBuilder extends AIFArgumentBuilder {
	// Components of this argument
	private Optional<String> 	positionToHaveKnowledge = Optional.empty(),	// "Agent A is in a position to know p"
								knowledgeAssertion = Optional.empty(),		// "Agent A asserts that p is the case"
								knowledgePosition = Optional.empty(),		// "p"
								sNode = Optional.empty(); 					// Node that ties the premises and conclusion together

	/** Set the position to have knowledge premise of this argument by providing its description. This will cause the creation of a new I-Node with 
	 * the given description, of which the ID becomes the ID of the premise. Example: "Agent A is in a position to know p". */
	public final PositionToKnowBuilder setPositionToHaveKnowledgeByDescription(final String description) throws AIFArgumentBuilderException{
		if(this.positionToHaveKnowledge.isPresent()) throw new AIFArgumentBuilderException();
		this.positionToHaveKnowledge = super.iNodeText(Optional.empty(), description);
		return this;
	}

	/** Set the position to have knowledge premise of this argument by using an ID. */
	public final PositionToKnowBuilder setPositionToHaveKnowledgeByID(final String id) throws AIFArgumentBuilderException{
		if(this.positionToHaveKnowledge.isPresent()) throw new AIFArgumentBuilderException();
		this.positionToHaveKnowledge = Optional.of(id);
		return this;
	}

	/** Set the knowledge assertion premise of this argument by providing its description. This will cause the creation of a new I-Node with 
	 * the given description, of which the ID becomes the ID of the premise. Example: "Agent A asserts that p is the case"*/
	public final PositionToKnowBuilder setKnowledgeAssertionByDescription(final String description) throws AIFArgumentBuilderException{
		if(this.knowledgeAssertion.isPresent()) throw new AIFArgumentBuilderException();
		this.knowledgeAssertion = super.iNodeText(Optional.empty(), description);
		return this;
	}

	/** Set the knowledge assertion premise of this argument by using an ID. */
	public final PositionToKnowBuilder setKnowledgeAssertionByID(final String id) throws AIFArgumentBuilderException{
		if(this.knowledgeAssertion.isPresent()) throw new AIFArgumentBuilderException();
		this.knowledgeAssertion = Optional.of(id);
		return this;
	}

	/** Set the knowledge position of this argument by providing its description. This will cause the creation of a new I-Node with 
	 * the given description, of which the ID becomes the conclusion. Example: "p" (if p was asserted by the agent that is in the position to know about p). */
	public final PositionToKnowBuilder setKnowledgePositionByDescription(final String description) throws AIFArgumentBuilderException{
		if(this.knowledgePosition.isPresent()) throw new AIFArgumentBuilderException();
		this.knowledgePosition = super.iNodeText(Optional.empty(), description);
		return this;
	}

	/** Set the knowledge position of this argument by using an ID. */
	public final PositionToKnowBuilder setKnowledgePositionByID(final String id) throws AIFArgumentBuilderException{
		if(this.knowledgePosition.isPresent()) throw new AIFArgumentBuilderException();
		this.knowledgePosition = Optional.of(id);
		return this;
	}

	/** Returns the AIF triples of this argument. If any values are not set (such as premises, conclusions, etc.) then 
	 * they will be generated automatically. You cannot set the values afterwards, only obtain them with the getters. */
	protected final StringBuffer convertToAIFTriples(){
		// Generate the IDs of the nodes in the AIF graph
		this.sNode = Optional.of(getID(Optional.empty()));
		this.positionToHaveKnowledge = this.positionToHaveKnowledge.isPresent() ? this.positionToHaveKnowledge : Optional.of(getID(Optional.empty()));
		this.knowledgeAssertion = this.knowledgeAssertion.isPresent() ? this.knowledgeAssertion : Optional.of(getID(Optional.empty()));
		this.knowledgePosition = this.knowledgePosition.isPresent() ? this.knowledgePosition : Optional.of(getID(Optional.empty()));

		// Make the triples 
		return (new StringBuffer())
				.append("aif:").append(this.sNode.get()).append(" aif:s-inference-node-fulfil aif:position-to-know-scheme.\r\n")
				.append("aif:").append(this.positionToHaveKnowledge.get()).append(" aif:i-fulfil aif:position-to-have-knowledge-description.\r\n")
				.append("aif:").append(this.knowledgeAssertion.get()).append(" aif:i-fulfil aif:knowledge-assertion-description.\r\n")
				.append("aif:").append(this.knowledgePosition.get()).append(" aif:i-fulfil aif:knowledge-position-description.\r\n")
				.append("aif:").append(this.sNode.get()).append(" aif:premise aif:").append(this.positionToHaveKnowledge.get()).append(".\r\n")
				.append("aif:").append(this.sNode.get()).append(" aif:premise aif:").append(this.knowledgeAssertion.get()).append(".\r\n")
				.append("aif:").append(this.sNode.get()).append(" aif:conclusion aif:").append(this.knowledgePosition.get()).append(".\r\n");  
	} 

	// Getters
	public final String getPositionToHaveKnowledgePremise(){ return this.positionToHaveKnowledge.get(); }
	public final String getKnowledgeAssertionPremise(){ return this.knowledgeAssertion.get(); }
	public final String getKnowledgePosition(){ return this.knowledgePosition.get(); }
	public final String getSNode(){ return this.sNode.get(); }
}
