package auctionCapability.planschemes;

import oo2apl.agent.AgentContextInterface;
import oo2apl.agent.PlanToAgentInterface;
import oo2apl.agent.Trigger;
import auctionCapability.BuyerContext;
import auctionCapability.triggers.AuctionEnded;
import oo2apl.plan.builtin.FunctionalPlanSchemeInterface;
import oo2apl.plan.builtin.SubPlanInterface;

/** 
 * A default plan scheme for handling the personal win of an auction. It will update the current demands in the buyer context. This 
 * scheme is not by default included in the TraderCapability, hence if you want to use it, add it to the message plan schemes. 
 * @author Bas Testerink
 *
 */
public final class HandleAuctionEndedPlanScheme implements FunctionalPlanSchemeInterface { 
	public SubPlanInterface getPlan(final Trigger trigger, final AgentContextInterface contextInterface) {
		if(trigger instanceof AuctionEnded<?>){ 
			AuctionEnded<?> end = (AuctionEnded<?>) trigger;
			return (PlanToAgentInterface planInterface) -> {
				BuyerContext context = planInterface.getContext(BuyerContext.class);
				// Update the demands
				context.updateDemands(end);
			};
		} else return SubPlanInterface.UNINSTANTIATED;
	}
}