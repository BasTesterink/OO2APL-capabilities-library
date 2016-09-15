package auctionCapability.planschemes;

import oo2apl.agent.AgentContextInterface;
import oo2apl.agent.AgentID;
import oo2apl.agent.PlanToAgentInterface;
import oo2apl.agent.Trigger;
import auctionCapability.AuctioneerContext;
import auctionCapability.triggers.AuctionAnnouncement;
import auctionCapability.triggers.AuctionResult;
import auctionCapability.triggers.ParticipantResponse;
import auctionCapability.triggers.AuctionResult.ResultType;
import oo2apl.plan.builtin.FunctionalPlanSchemeInterface;
import oo2apl.plan.builtin.SubPlanInterface;

/**
 * This plan scheme deals with the received bids from participants. 
 * 
 * @author Bas Testerink
 */
public final class HandleParticipantResponseScheme implements FunctionalPlanSchemeInterface { 
	public SubPlanInterface getPlan(final Trigger trigger, final AgentContextInterface contextInterface) {
		if(trigger instanceof ParticipantResponse){ 
			ParticipantResponse participantResponse = (ParticipantResponse) trigger;
			return (PlanToAgentInterface planInterface) -> {
				// Get the context for decision making
				AuctioneerContext context = planInterface.getContext(AuctioneerContext.class);
	 
				// Make the auction data
				AuctionResult<?> result = context.handleParticipantResponse(participantResponse);
				
				// Notify the auctioneer of the current status if the round was finished
				if(result != AuctionResult.WAITING){ 
					planInterface.addInternalTrigger(result);
				}
				
				// If an auction is finished, then remove its data
				if(result.getType() == ResultType.FINISHED){
					context.clearData(participantResponse.getAuctionID());
				} else if(result.getType() == ResultType.NEWROUND){
					// English and Dutch auctions can have multiple rounds
					// In case of an English or Dutch auction the current price is published
					AuctionAnnouncement<?> announcement = new AuctionAnnouncement<>(
							participantResponse.getAuctionID(), 
							planInterface.getAgentID(), 
							result.getTrigger().getType(), 
							result.getTrigger().getObjectForSale(), 
							result.getPrice(),
							result.getQuantityAvailable(),
							result.getDecrement());
					for(AgentID participant : result.getTrigger().getParticipants()){ 
						planInterface.sendMessage(participant, announcement);
					}
					
				} // else there are still bids to be received, so do nothing
			};
		} else return SubPlanInterface.UNINSTANTIATED;
	}  
}
