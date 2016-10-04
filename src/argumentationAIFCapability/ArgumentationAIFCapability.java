package argumentationAIFCapability;

import argumentationAIFCapability.argumentbuilders.ModusPonensBuilder;
import argumentationAIFCapability.argumentbuilders.NegationConflictBuilder;
import argumentationAIFCapability.argumentbuilders.PopularOpinionBuilder;
import argumentationAIFCapability.argumentbuilders.PositionToKnowBuilder;
import argumentationAIFCapability.argumentbuilders.PropositionBuilder;
import argumentationToastCapability.ToastContext;
import fusekiOntologyCapability.FusekiContext;
import oo2apl.agent.AgentBuilder;

/**
 * 
 * The AIF capability supports the storage of arguments using the argument interchange format (AIF).
 * It does so by using a Fuseki server to store argument graphs and use Toast to evaluate arguments. 
 * The capability also provides plan schemes to handle argumentation dialogues. To trigger a dialogue 
 * the agent can adopt an internal trigger that specifies with whom and what protocol the dialogue 
 * should be executed, the start position (which is also an argumentation graph) that specifies the 
 * position of the agent in the dialogue, a goal predicate on the dialogue position to indicate the 
 * private aim of the dialogue and finally a strategy to use during the dialogue. When the dialogue is 
 * terminated the agent will adopt a new internal trigger to report the result (resulting dialogue 
 * position, the status of the goal, and a history describing the events during the dialogue). 
 * 
 * @author Bas Testerink
 */

public class ArgumentationAIFCapability extends AgentBuilder {
	/**
	 * Note: currently the capability only supports the specification and evaluation of a position. Example usage:
	 * <code>
	 * 
	 * PositionToKnowBuilder positionToKnow = (new PositionToKnowBuilder())
				.setPositionToHaveKnowledgeByDescription("Agent A knows whether p or not p")
				.setKnowledgeAssertionByDescription("Agent A says p")
				.setKnowledgePositionByDescription("p");
	
		ModusPonensBuilder modusPonens1 = (new ModusPonensBuilder())
				.setMinorPremiseByID(positionToKnow.getKnowledgePosition())
				.setMajorPremiseByDescription("p->q")
				.setConclusionByDescription("q");
	
		PopularOpinionBuilder popularOpinion = (new PopularOpinionBuilder())
				.setGeneralAcceptanceByDescription("It is commonly the case that r")
				.setKnowledgePositionByDescription("r");
	
		ModusPonensBuilder modusPonens2 = (new ModusPonensBuilder())
				.setMinorPremiseByID(popularOpinion.getKnowledgePosition())
				.setMajorPremiseByDescription("r -> not (p->q)")
				.setConclusionByDescription("not (p->q)");
	
		NegationConflictBuilder conflict = (new NegationConflictBuilder())
				.setPositivePropositionByID(modusPonens1.getMajorPremise())
				.setNegativePropositionByID(modusPonens2.getConclusion());
		
		PropositionBuilder proposition = (new PropositionBuilder())
				.setPropositionByDescription("a proposition"); 
				
		AIFContext aif = planInterface.getContext(AIFContext.class);
		aif.makePosition("myExample", positionToKnow, modusPonens1, popularOpinion, modusPonens2, conflict, proposition);
		
		System.out.println(aif.getPositionSemanticalAnalysis("myExample"));
	 * </code>
	 */
	public ArgumentationAIFCapability(final FusekiContext fuseki, final ToastContext toast, final String pathToAIFFilesDirectory){
		super.addContext(new AIFContext(fuseki, toast, pathToAIFFilesDirectory));
	}
}
