package auctionCapability.planschemes;

import oo2apl.agent.AgentContextInterface;
import oo2apl.agent.PlanToAgentInterface;
import oo2apl.agent.Trigger;
import auctionCapability.BuyerContext;
import auctionCapability.triggers.AuctionAnnouncement;
import auctionCapability.triggers.ParticipantResponse;
import oo2apl.plan.builtin.FunctionalPlanSchemeInterface;
import oo2apl.plan.builtin.SubPlanInterface;
/**
 * This plan scheme handles for participants of an auction the announcement of the auction. 
 * 
 * @author Bas Testerink
 *
 */
public final class HandleAuctionAnnouncement implements FunctionalPlanSchemeInterface { 
	public SubPlanInterface getPlan(final Trigger trigger, final AgentContextInterface contextInterface) {
		if(trigger instanceof AuctionAnnouncement<?>){
			AuctionAnnouncement<?> announcement = (AuctionAnnouncement<?>) trigger;
			return (PlanToAgentInterface planInterface) -> {
				// Get the context for decision making
				BuyerContext context = planInterface.getContext(BuyerContext.class);
				
				// Get the (initial) bid
				ParticipantResponse participantResponse = context.registerAuction(announcement, planInterface.getAgentID()); 
				
				// Send the bid
				planInterface.sendMessage(announcement.getAuctioneer(), participantResponse);
			};
		}
		return SubPlanInterface.UNINSTANTIATED; 
	}
}