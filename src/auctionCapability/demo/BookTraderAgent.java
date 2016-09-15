package auctionCapability.demo;

import java.util.ArrayList;
import java.util.List;

import auctionCapability.AuctionPersonalResult;
import auctionCapability.Bid;
import auctionCapability.PriceQuantityPair;
import auctionCapability.TraderCapability;
import auctionCapability.BuyerContext.EnglishStrategy;
import auctionCapability.BuyerContext.EvaluationFunction;
import auctionCapability.triggers.AuctionResult;
import oo2apl.agent.AgentBuilder;
import oo2apl.agent.PlanToAgentInterface;
import oo2apl.agent.AgentContextInterface;
import oo2apl.agent.Trigger;
import oo2apl.plan.builtin.DecoupledPlanBodyInterface; 
import oo2apl.plan.builtin.FunctionalPlanSchemeInterface;
import oo2apl.plan.builtin.SubPlanInterface;
/**
 * The book trader agent exemplifies the usage of the trader capability. It has a custom English 
 * auction strategy and custom functions to handle rounds and auction ends. The latter functions simply
 * print the results. 
 * 
 * @author Bas Testerink
 */
public final class BookTraderAgent {
	/** Creates an agent that wants to buy a single book with the evaluation that is given by the provided evaluation function. */
	public static final AgentBuilder makeBookTraderAgent(final EvaluationFunction<Book> evaluationFunction){
		return (new AgentBuilder()) 										// Agent builder for base components
					.addExternalTriggerPlanScheme(makeSellBookScheme())		// Add the sell book scheme that will start an auction to sell a book
					.include((new TraderCapability())						// Include the trader capability
							.addDemand(Book.class, evaluationFunction, 1)	// Initial demand for a book
							.setEnglishStrategy(makeEnglishStrategy()));	// Override the default  English auction strategy
	}
	
	/** Specify a new custom English strategy.  */
	private static final EnglishStrategy makeEnglishStrategy(){
		return new EnglishStrategy(){ 
			public List<PriceQuantityPair> getBids(double evaluation, double currentPrice, int desiredQuantity) { 
				List<PriceQuantityPair> result = new ArrayList<>();
				// The idea is to bid in increments of 10% of the evaluation price, note that if some other agent has the 
				// same evaluation and the same strategy, then they will both potentially halt before their maximum price 
				// and one of them gets the good in a tie-breaker. Note that this is beneficial for the agents but not for 
				// the auctioneer.
				double ratio = currentPrice/evaluation;
				if(ratio <= 1.0){ // Current price is below the evaluation
					double newBid = (Math.ceil(ratio*10)/10)*evaluation; // Get the next increment
					result.add(new PriceQuantityPair(newBid, desiredQuantity)); // Return the bid
				}
				return result;
			} 
		};
	}
	
	/** The scheme that handles the trigger to organize an auction, which is exactly what its plan consists of. */
	private static final FunctionalPlanSchemeInterface makeSellBookScheme(){
		return (Trigger t, AgentContextInterface agentContext) -> {
			if(t instanceof SellBookTrigger){ 
				SellBookTrigger trigger = (SellBookTrigger)t;
				return (PlanToAgentInterface planInterface) -> {
					TraderCapability.organizeAuction(planInterface,
							trigger.getAuctionType(),
							trigger.getBook(), 
							trigger.getBuyers(), 
							trigger.getMinimalPricePerUnit(),
							trigger.getMaximalPricePerUnit(),
							trigger.getDecrementPerRound(), 
							trigger.getQuantityToSell(),
							getPrintRoundPlan(), 
							getPrintResultPlan());
				};
			} else return SubPlanInterface.UNINSTANTIATED;
		};
		
	}
	
	/** The plan that is executed after every round. Simply prints the results. */
	private static final DecoupledPlanBodyInterface<AuctionResult<Book>> getPrintResultPlan(){
		return (AuctionResult<Book> result, PlanToAgentInterface planInt)-> {
			System.out.println("+++++");
			System.out.println("Winners:");
			List<AuctionPersonalResult> winners = result.getPersonalResults();
			for(AuctionPersonalResult winner : winners)
					System.out.println(winner.getBid().getBidder()+" won with bid "+winner.getBid().getPrice() +" and "+winner.getBid().getQuantity()+" unit(s), and pays "+winner.getPrice()+" per unit for "+winner.getQuantity()+" unit(s)");
			
			System.out.println("The final bids above minimum: ");
			for(Bid bid : result.getBids()){
				System.out.println(bid.getBidder()+"  "+bid.getPrice());
			}
			
		}; 
	}

	/** The plan that is executed after the auction. Simply prints the results. */
	private static final DecoupledPlanBodyInterface<AuctionResult<Book>> getPrintRoundPlan(){
		return (AuctionResult<Book> result, PlanToAgentInterface planInt)-> { 
			System.out.println("=== new round ===");
			System.out.println("Bids that were above the current minimum: ");
			for(Bid bid : result.getBids()){
				System.out.println(bid.getBidder()+"  "+bid.getPrice());
			}
			System.out.println("New (minimum) price: "+result.getPrice());
		};
	}
}
