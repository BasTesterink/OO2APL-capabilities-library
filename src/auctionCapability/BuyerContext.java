package auctionCapability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import auctionCapability.triggers.AuctionAnnouncement;
import auctionCapability.triggers.AuctionEnded;
import auctionCapability.triggers.ParticipantResponse;
import auctionCapability.triggers.OrganizeAuction.AuctionType;
import oo2apl.agent.AgentID;
import oo2apl.agent.Context;
/**
 * The context of a buyer maintains bidding strategies and a `wish list' of demands. Each demand is an evaluation function and a quantity. This 
 * can be used to implement priorities over quantities. For instance, if it is of high priority that at least two units are bought and of 
 * lower priority that in total five units are bought, then the agent may place a high bid for a quantity of two and a lower bid for a quantity of three. 
 * 
 * @author Bas Testerink
 */
public final class BuyerContext implements Context {
	// The strategies
	private VickreyStrategy vickreyStrategy = makeDefaultVickreyStrategy();
	private EnglishStrategy englishStrategy = makeDefaultEnglishStrategy();
	private DutchStrategy dutchStrategy = makeDefaultDutchStrategy(); 
	private final Map<Class<?>, List<Demand<?>>> demands;
	
	public BuyerContext(){
		this.demands = new HashMap<>();
	}
	
	/** Removes or updates the demand of an item. The highest demands are removed until the gained quantity is reached. */
	public final void updateDemands(final AuctionEnded<?> end){
		if(end.getResult() != AuctionPersonalResult.NOTWON) 
			handleAuctionEnd(end.getItemForSale().getClass(), end);
	}
	
	@SuppressWarnings("unchecked") // The casting in this method is guaranteed to be correct
	private final <T> void handleAuctionEnd(final Class<T> klass, final AuctionEnded<?> end){  
		List<Demand<?>> demandList = this.demands.get(klass);
		if(demandList != null){
			// Order the demands based on the valuation of the item. It is assumed that a high evaluation means a high 
			// priority. Therefore, the demands that are removed are those who's quantity is satisfied by the result of 
			// auction. Order is from high to low.
			List<Demand<T>> casted = new ArrayList<>();
			for(Demand<?> demand : demandList) casted.add((Demand<T>)demand);
			final T itemForSale = (T) end.getItemForSale();
			casted.sort((Demand<T> a, Demand<T> b) -> {
				EvaluationFunction<T> f1 = (EvaluationFunction<T>)a.getEvaluationFunction();
				EvaluationFunction<T> f2 = (EvaluationFunction<T>)b.getEvaluationFunction();
				return Double.compare(f1.evaluate(itemForSale), f2.evaluate(itemForSale));
			});
			int quantity = end.getResult().getQuantity();
			Iterator<Demand<T>> iterator = casted.iterator();
			// Go through the demands from high to low
			while(iterator.hasNext() && quantity > 0){
				Demand<T> demand = iterator.next();
				// Remove those demands who's quantity is completely covered
				if(demand.getDesiredQuantity() <= quantity){
					iterator.remove();
					quantity -= demand.getDesiredQuantity();
				} else {
					// The case where the final bit of quantity is not covering the next demand
					demand.setDesiredQuantity(demand.getDesiredQuantity() - quantity);
					quantity = 0;
				}
			}
		}
	}
	
	/** Process the initial announcement of an action. The response contains the bids that the agent places, which in turn is determined by 
	 * the relevant bidding strategy. */
	public final ParticipantResponse registerAuction(final AuctionAnnouncement<?> announcement, final AgentID myID){ 
		return new ParticipantResponse(announcement.getAuctionID(), myID, getBidsForItem(announcement.getItemForSale().getClass(), announcement, myID));
	}

	//Casting warnings are suppressed as the registration of evaluation functions ensures that the types are correct.
	/** Return the list of bids, depending on the demands and strategies of the agent. */ 
	@SuppressWarnings("unchecked")
	private final <R> List<Bid> getBidsForItem(final Class<R> klass, final AuctionAnnouncement<?> announcement, final AgentID myID){
		List<Bid> bids = new ArrayList<>();
		List<Demand<?>> demandForItem = this.demands.get(klass);
		if(demandForItem == null) return bids; // No interest in this item
		// Go through the demands
		for(Demand<?> d : demandForItem){
			Demand<R> demand = (Demand<R>)d;
			// The evaluation is how much the agent is willing to maximally pay per unit
			double evaluation = demand.getEvaluationFunction().evaluate((R)announcement.getItemForSale());
			// Convert the evaluation and auction data to a list of bids, depending on the appropriate bidding strategy
			if(announcement.getType() == AuctionType.VICKREY){ 
				for(PriceQuantityPair pqPair : this.vickreyStrategy.getBids(evaluation, demand.getDesiredQuantity()))
					bids.add(new Bid(pqPair.getPrice(), pqPair.getQuantity(), myID)); 
			} else if(announcement.getType() == AuctionType.ENGLISH){
				for(PriceQuantityPair pqPair : this.englishStrategy.getBids(evaluation, announcement.getPrice(), demand.getDesiredQuantity()))
					bids.add(new Bid(pqPair.getPrice(), pqPair.getQuantity(), myID));
			} else if(announcement.getType() == AuctionType.DUTCH){
				bids.add(new Bid(announcement.getPrice(), this.dutchStrategy.getAcceptedQuantity(evaluation, announcement.getQuantityAvailable(), announcement.getPrice(), demand.getDesiredQuantity(), announcement.getDecrement()), myID));
			}
		}
		return bids;
	}
	
	/** Register a new demand to the `wish list' of the agent.  */
	public final <T> void putDemand(final Class<T> klass, final Demand<T> newDemand){
		List<Demand<?>> demand = this.demands.get(klass);
		if(demand == null){
			demand = new ArrayList<>();
			this.demands.put(klass, demand);
		}
		demand.add(newDemand);
	} 	
	
	/** Remove a demand from the `wish list' of the agent. NOTE: Do  not do this whilst the agent is still in an auction!  */
	public final <T> void removeDemand(final Class<T> klass, final Demand<T> demandToRemove){
		List<Demand<?>> demand = this.demands.get(klass);
		if(demand != null){
			demand.remove(demandToRemove);
		} 
	} 
	
	// Setters for the bidding strategies
	public final void setVickreyStrategy(final VickreyStrategy strategy){ this.vickreyStrategy = strategy; } 
	public final void setEnglishStrategy(final EnglishStrategy strategy){ this.englishStrategy = strategy; } 
	public final void setDutchStrategy(final DutchStrategy strategy){ this.dutchStrategy = strategy; } 
	
	/** Functional interface to determine how much worth an object is to an agent. */
	public interface EvaluationFunction<T> {
		/** Returns the price per unit of the object for sale that the agent would be willing to maximally pay. */
		public double evaluate(T objectForSale);
	}
	
	/** Functional interface to determine how much to bid in a Vickrey auction. */
	public interface VickreyStrategy {
		/** Returns of price-per-unit and quantity-of-unit pairs, which will be transformed in to the bids that the agent makes. */
		public List<PriceQuantityPair> getBids(double evaluation, int desiredQuantity);
	}
	
	/** Functional interface to determine how much to bid in an English auction. */
	public interface EnglishStrategy { 
		/** Returns of price-per-unit and quantity-of-unit pairs, which will be transformed in to the bids that the agent makes. 
		 * @param evaluation The maximal price that the agent is willing to pay per unit. 
		 * @param currentPrice A price indicator; if no other agent changes a bid, then bidding above this price will guarantee at least 1 unit to be bought. 
		 * @param desiredQuantity The amount of units that the agent wants. */
		public List<PriceQuantityPair> getBids(double evaluation, double currentPrice, int desiredQuantity);
	}
		
	/** Functional interface to determine how much to bid in a Dutch auction. */
	public interface DutchStrategy {
		/** Returns the quantity to buy for the current price.
		 * @param evaluation The maximal price that the agent is willing to pay per unit. 
		 * @param remainingQuantity The amount of units that are left in the auction.
		 * @param currentPrice The current price per unit. 
		 * @param desiredQuantity The amount of units that the agent wants. */
		public int getAcceptedQuantity(double evaluation, int remainingQuantity, double currentPrice, int desiredQuantity, double decrementPerRound);
	} 
	
	/** The default strategy is to bid the valuation of a unit. */
	private static final VickreyStrategy makeDefaultVickreyStrategy(){
		return (double evaluation, int quantity) -> {
			if(evaluation == 0) return Collections.emptyList(); 
			List<PriceQuantityPair> pairs = new ArrayList<>();
			pairs.add(new PriceQuantityPair(evaluation, quantity));
			return pairs;
		};
	}

	/** The default strategy is to bid the valuation of a unit. */
	private static final EnglishStrategy makeDefaultEnglishStrategy(){
		return (double evaluation, double currentPrice, int quantity) -> {
			if(evaluation == 0) return Collections.emptyList();
			List<PriceQuantityPair> pairs = new ArrayList<>();
			pairs.add(new PriceQuantityPair(evaluation, quantity));
			return pairs;
		};
	}

	/** The default strategy is to accept the price for the full desired quantity as soon as it drops below the unit's evaluation. */
	private static final DutchStrategy makeDefaultDutchStrategy(){
		return new DutchStrategy(){ 
			public int getAcceptedQuantity(double evaluation, int quantityAvailable, double currentPrice, int desiredQuantity, double decrementPerRound) {
				if(evaluation == 0) return 0;
				// First check if the price is acceptable, then check whether this is the first time that the price dipped below or at the evaluation. 
				// The second check ensures that the agent doesn't keep bidding if the acceptable price is reached. 
				if(currentPrice <= evaluation && (currentPrice + decrementPerRound) > evaluation){ 
					return desiredQuantity;
				} else return 0;
			}
		};
	} 
}