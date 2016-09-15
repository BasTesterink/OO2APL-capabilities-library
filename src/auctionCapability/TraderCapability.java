package auctionCapability;

import java.util.List;
import java.util.function.Predicate;

import auctionCapability.BuyerContext.DutchStrategy;
import auctionCapability.BuyerContext.EnglishStrategy;
import auctionCapability.BuyerContext.EvaluationFunction;
import auctionCapability.BuyerContext.VickreyStrategy;
import auctionCapability.planschemes.HandleAuctionAnnouncement;
import auctionCapability.planschemes.HandleParticipantResponseScheme;
import auctionCapability.planschemes.OrganizeAuctionScheme;
import auctionCapability.triggers.AuctionEnded;
import auctionCapability.triggers.AuctionResult;
import auctionCapability.triggers.OrganizeAuction;
import auctionCapability.triggers.AuctionResult.ResultType;
import auctionCapability.triggers.OrganizeAuction.AuctionType;
import oo2apl.agent.AgentBuilder;
import oo2apl.agent.AgentID;
import oo2apl.agent.PlanToAgentInterface;
import oo2apl.agent.Trigger;
import oo2apl.plan.builtin.DecoupledPlanBodyInterface;

/**
 * An agent builder that can be included during agent creation so that the agent gains the capability 
 * to organize and participate in auctions. If x is your main agent builder, then use x.include(new TraderCapability);
 * 
 * Usage:
 * 
 * Currently the capability supports Vickrey, English and Dutch auctions where an arbitrary (but positive) quantity of a given unit/item/object is on sale. 
 * Upon including this capability the agent can organize auctions or participate in auctions. To organize an auction one has to adopt an internal trigger 
 * which is an instantiation of OrganizeAuction. This will cause the agent to message the participants and handle their responses. If the auction enters a 
 * new round, or is finished, then the auctioneer will receive an internal trigger from the type AuctionResult. 
 * To organize an auction it is possible to use the TradeCapability.organizeXXXXAuction(planInterface,...) methods from inside a plan.
 * 
 * As a participant, this capability can be used to set the bidding strategies of the agent and the items that it wants. Both can be done at 
 * the moment of the agent creation and from inside a plan using TradeCapability.addXXXXStrategy(...) and addDemand. 
 * 
 * The library provides a trigger to be send by the auctioneer to the participants called AuctionEnd. This is not done automatically, so if 
 * you want to send auction ends then provide this as part of a plan to handle auction results. In a similar vein, it is not preprogrammed how 
 * buyers react to the end of an auction. There is, however a preprogrammed method in the BuyerContext called updateDemands which will update the 
 * demands of the agent based on the result of the action. A shortcut for calling this method is TraderCapability.updateDemands(...), which is 
 * recommended to be executed for any plan that handles the results of auctions on the participant side. 
 *  
 * Important implementation notes:
 * 	- An agent only has one strategy per auction type, i.e., it is not supported that an agent can use different strategies simultaneously
 *  - The participants of the auction need to be known before the auction starts. It is not supported that agents join whilst the auction is ongoing.
 *  - Currently demands are tied to classes of tradeable goods. In the future this might be updated. 
 *  - Manually removing a buyer's demands during auctions can result in unexpected behavior and should be prevented. 
 *  
 * @author Bas Testerink
 *
 */
public final class TraderCapability extends AgentBuilder {
	// Context for bookkeeping auctions
	protected final AuctioneerContext auctioneerContext;
	// Context for bookkeeping demands and bidding strategies
	protected final BuyerContext buyerContext;
	
	public TraderCapability(){
		this.auctioneerContext = new AuctioneerContext();
		this.buyerContext = new BuyerContext(); 
		super.addContext(this.auctioneerContext);
		super.addContext(this.buyerContext);
		super.addInternalTriggerPlanScheme(new OrganizeAuctionScheme());
		super.addMessagePlanScheme(new HandleAuctionAnnouncement());
		super.addMessagePlanScheme(new HandleParticipantResponseScheme());
	}

	/** Add an evaluation function so that objects of a certain class can be 
	 * evaluated by the agent for their worth. */
	public final <T> TraderCapability addDemand(final Class<T> klass, final EvaluationFunction<T> function, final int desiredQuantity){
		this.buyerContext.putDemand(klass, new Demand<T>(function, desiredQuantity)); 
		return this;
	}
	
	/** Add an evaluation function so that objects of a certain class can be 
	 * evaluated by the agent for their worth. */
	public final static <T> void addDemand(final PlanToAgentInterface planInterface, final Class<T> klass, final EvaluationFunction<T> function, final int desiredQuantity){
		getBuyerContext(planInterface).putDemand(klass, new Demand<T>(function, desiredQuantity)); 
	} 
	
	/** Override the standard Vickrey strategy. Note: the standard strategy of always bidding the evaluation of an 
	 * object is in fact the optimal strategy. */
	public final <T> TraderCapability setVickreyStrategy(final VickreyStrategy strategy){
		this.buyerContext.setVickreyStrategy(strategy);
		return this;
	}

	/** Override the standard Vickrey strategy. Note: the standard strategy of always bidding the evaluation of an 
	 * object is in fact the optimal strategy. */
	public static final <T> void setVickreyStrategy(final PlanToAgentInterface planInterface, final VickreyStrategy strategy){
		getBuyerContext(planInterface).setVickreyStrategy(strategy); 
	}
	
	/** Override the standard English bidding strategy. */
	public final <T> TraderCapability setEnglishStrategy(final EnglishStrategy strategy){
		this.buyerContext.setEnglishStrategy(strategy);
		return this;
	}
	
	/** Override the standard English bidding strategy. */
	public final <T> void setEnglishStrategy(final PlanToAgentInterface planInterface, final EnglishStrategy strategy){
		getBuyerContext(planInterface).setEnglishStrategy(strategy); 
	}
	
	/** Override the standard Dutch auction bidding strategy. */
	public final <T> TraderCapability setDutchStrategy(final DutchStrategy strategy){
		this.buyerContext.setDutchStrategy(strategy);
		return this;
	}
	
	/** Override the standard Dutch auction bidding strategy. */
	public final <T> void setDutchStrategy(final PlanToAgentInterface planInterface, final DutchStrategy strategy){
		getBuyerContext(planInterface).setDutchStrategy(strategy); 
	}
 
	private final static BuyerContext getBuyerContext(final PlanToAgentInterface planInterface){
		BuyerContext context = planInterface.getContext(BuyerContext.class);
		// TODO: check if context was available, otherwise throw exception that capability was not included
		return context;
	}

	/** An API call to make it easy from within a plan execution method to organize and handle the result of an auction. NOTE: it is 
	 * required that during the agent creation process the agent has gained all the contexts and schemes from the peer-2-peer auction 
	 * package. To ensure this, you can use an AgentBuilder that absorbs a new TraderCapability instance to create the agent.  */
	public static final <T> void organizeAuction(final PlanToAgentInterface planInterface, final AuctionType auctionType,
			final T objectForSale, final List<AgentID> participants, final double minimalPrice, final double maximalPrice, final double decrementPerRound, 
			final int nrOfWinners, final DecoupledPlanBodyInterface<AuctionResult<T>> planForRoundUpdate,
			final DecoupledPlanBodyInterface<AuctionResult<T>> planForResult){ 
		// This trigger will cause the plan scheme to fire that initiates the organization of the auction
		planInterface.addInternalTrigger(new OrganizeAuction<T>(auctionType, objectForSale, participants, minimalPrice, maximalPrice, decrementPerRound, nrOfWinners));
		
		// This selector is used to intercept the auction notifications that are send to the auctioneer 
		Predicate<Trigger> selector = (Trigger t) -> {
					return t instanceof AuctionResult && objectForSale.getClass().isInstance(((AuctionResult<?>)t).getTrigger().getObjectForSale());
				};  
		// Add the response to auction notifications to the auctioneer (new rounds and the final result)
		adoptAuctionInterceptor(planInterface, selector, planForRoundUpdate, planForResult);
	}
	
	/** Adds an interceptor to the auctioneer that ensures the correct processing of the new round notifications and the auction results. */
	private static final <T> void adoptAuctionInterceptor(final PlanToAgentInterface planInterface, final Predicate<Trigger> selector, 
			final DecoupledPlanBodyInterface<AuctionResult<T>> planForRoundUpdate,
			final DecoupledPlanBodyInterface<AuctionResult<T>> planForResult){ 
		planInterface.waitForInternalTrigger(selector, // Keep using the selector that is true for auction notifications
				(AuctionResult<T> result, PlanToAgentInterface planInt)-> { 
					if(result.getType() == ResultType.FINISHED) // When the auction is finished, then execute the result plan
						planForResult.execute(result, planInt);
					else { // Otherwise execute the round update plan, and add this interceptor again to process the next round or the final result
						planForRoundUpdate.execute(result, planInt);
						adoptAuctionInterceptor(planInt, selector, planForRoundUpdate, planForResult);
					}
				});
	}
	
	/** Ensures that a Vickrey auction is organized and handled. */
	public static final <T> void organizeVickreyAuction(final PlanToAgentInterface planInterface,
			final T objectForSale, final List<AgentID> participants, final double minimalPrice, 
			final int nrOfWinners, final DecoupledPlanBodyInterface<AuctionResult<T>> planForResult){ 
		organizeAuction(planInterface, AuctionType.VICKREY, objectForSale, participants, minimalPrice, 0d, 0d, nrOfWinners, (AuctionResult<T> result, PlanToAgentInterface pi)->{}, planForResult);
	}

	/** Ensures that an English auction is organized and handled. */
	public static final <T> void organizeEnglishAuction(final PlanToAgentInterface planInterface,
			final T objectForSale, final List<AgentID> participants, final double minimalPrice, 
			final int nrOfWinners, final DecoupledPlanBodyInterface<AuctionResult<T>> planForRoundUpdate,
			final DecoupledPlanBodyInterface<AuctionResult<T>> planForResult){ 
		organizeAuction(planInterface, AuctionType.ENGLISH, objectForSale, participants, minimalPrice, 0d, 0d, nrOfWinners, planForRoundUpdate, planForResult);
	}

	/** Ensures that a Dutch auction is organized and handled. */
	public static final <T> void organizeDutchAuction(final PlanToAgentInterface planInterface,
			final T objectForSale, final List<AgentID> participants, final double minimalPrice, final double maximalPrice,  final double decrementPerRound, 
			final int nrOfWinners, final DecoupledPlanBodyInterface<AuctionResult<T>> planForRoundUpdate,
			final DecoupledPlanBodyInterface<AuctionResult<T>> planForResult){ 
		organizeAuction(planInterface, AuctionType.DUTCH, objectForSale, participants, minimalPrice, maximalPrice, decrementPerRound, nrOfWinners, planForRoundUpdate, planForResult);
	}
	
	/** Call upon the buyer context to update the demands given the result of the auction. If the auction was won by this agent 
	 * then it will lower its desired quantity according to the allocated won resources.  */
	public static final void updateDemands(final PlanToAgentInterface planInterface, final AuctionEnded<?> auctionEnd){
		getBuyerContext(planInterface).updateDemands(auctionEnd);
	}
}
