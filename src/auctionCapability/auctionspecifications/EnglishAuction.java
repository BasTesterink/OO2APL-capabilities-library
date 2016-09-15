package auctionCapability.auctionspecifications;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import auctionCapability.AuctionPersonalResult;
import auctionCapability.Bid;
import auctionCapability.triggers.AuctionAnnouncement;
import auctionCapability.triggers.AuctionResult;
import auctionCapability.triggers.OrganizeAuction;
import auctionCapability.triggers.ParticipantResponse;
import auctionCapability.triggers.AuctionResult.ResultType;
import oo2apl.agent.AgentID;
/**
 * An English auction has a minimal price and agent can push it higher by bidding a higher price. Each agent has a 
 * price it is willing to pay and the quantity of goods that it wants. A new bid has increase the price and/or the quantity. 
 * The auction continues until no agent increases the price or quantity that it is willing to pay/buy. In that case the bids are 
 * ordered on price first, and then quantity for tie-breaking, and units are assigned accordingly. 
 * An agent may submit multiple bids per round which will replace the previous bids. However, the new bids are only accepted if the agent's 
 * lowest price/quantity bid is higher than the previous lowest.  
 * @author Bas Testerink
 * @param <T>
 */
public final class EnglishAuction<T> extends Auction<T> {
	private final Map<AgentID, List<Bid>> allBids;
	private List<Bid> bidsThisRound; 
	// In an English auction, the current price indicates that if you bid that price plus an increment, then you outbid another person and you are guaranteed
	// of at least one unit if the auction ends the next round.
	private double currentPrice;

	public EnglishAuction(final OrganizeAuction<T> trigger, final UUID auctionID) {
		super(trigger, auctionID);  
		this.allBids = new HashMap<>();
		this.bidsThisRound = new ArrayList<>(); 
		this.currentPrice = trigger.getMinimalPrice() - 1;
	} 
	
	/** {@inheritDoc} */
	public final AuctionAnnouncement<T> makeInitialAnnouncement(final UUID auctionID, final AgentID auctioneer){
		return new AuctionAnnouncement<>(auctionID, auctioneer, this.trigger.getType(), this.trigger.getObjectForSale(), this.currentPrice, this.trigger.getQuantity(), this.trigger.getDecrementPerRound());
	}
	
	/** An English auction is finished if there are no bids in the current round, because that indicates that nobody wants to 
	 * bid on more units, or is willing to increase its minimal price per unit. */
	protected final boolean isFinished(){ return this.bidsThisRound.isEmpty(); }

	/** An English auction round consists of determining the new current price and making a report. */
	protected final AuctionResult<?> nextRound(){ 
		// Determine the new lowest price per unit
		List<Bid> sorted = getAllBidsFlatAndSorted(); 
		int available = this.trigger.getQuantity();
		for(Bid bid : sorted){
			// If the next bid is a winning bid that would exceed the available quantity, then it means that 
			// outbidding the next bid will ensure you at least some units if no-one else changes bids. Hence 
			// the new current price is the price of the next bid in that case. 
			available -= bid.getQuantity();
			if(available <= 0){
				this.currentPrice = bid.getPrice(); 
				break;
			}
		} 
		
		// Make report
		this.bidsThisRound.sort(Bid.COMPARATOR);
		AuctionResult<T> result = new AuctionResult<T>(super.auctionID, ResultType.NEWROUND, this.trigger, Collections.emptyList(), this.bidsThisRound, this.currentPrice, this.trigger.getQuantity(), this.trigger.getDecrementPerRound());
		this.bidsThisRound = new ArrayList<>();
		return result;	
	}
	
	
	private final List<Bid> getAllBidsFlatAndSorted(){
		List<Bid> allBids = this.allBids.values().stream().reduce(new ArrayList<>(), 
				(List<Bid> list, List<Bid> bidsFromAgent)->{
					list.addAll(bidsFromAgent); 
					return list;
				});
		allBids.sort(Bid.COMPARATOR);
		return allBids;
	}

	/** 
	 * An agent can submit a list of bids in a round to overwrite its last submitted list of bids. However, an English auction requires a 
	 * monotonous increase of what an agent is bidding. In this implementation that is interpreted as that an agent's new list of bids cannot have 
	 * less units that are bid upon than in the previous list of bids, and the minimal price per unit that occurs in the new list of bids must 
	 * be higher than the minimal price per unit in the previously submitted list of bids. If the new bids do not comply with this constraint, 
	 * then the entire list of new bids is ignored. 
	 */
	protected final void storeBids(final ParticipantResponse response){
		List<Bid> previousBids = this.allBids.get(response.getBidder());  
		if(previousBids != null && previousBids.size() > 0){
			// An agent cannot bid less containers than the previous bids that it made
			int previousQuantity = previousBids.stream().mapToInt((Bid b)-> b.getQuantity()).sum();
			int newQuantity = response.getBids().stream().mapToInt((Bid b)-> b.getQuantity()).sum(); 
			// The minimal price per container cannot be lowered than the minimal price the agent was willing to pay in the previous round
			Bid previousLowestBid = previousBids.get(previousBids.size()-1); 
			boolean illegal = response.getBids().stream().filter((Bid b)->{return b.getPrice() < previousLowestBid.getPrice();}).findAny().isPresent();
			if(newQuantity >= previousQuantity && !illegal){
				List<Bid> newBids = new ArrayList<>(response.getBids());
				// Register the bids
				newBids.sort(Bid.COMPARATOR);
				// Check whether these are not exactly the same bids as before
				if(newBids.size() == previousBids.size()){
					boolean same = true;
					for(int i = 0; i < newBids.size(); i++)
						same &=
							newBids.get(i).getPrice() == previousBids.get(i).getPrice() &&
							newBids.get(i).getQuantity() == previousBids.get(i).getQuantity();
					if(!same){ // The total quantity was same or higher, and the lowest price was not lowered, and at least something changed, hence store the new bids
						this.bidsThisRound.addAll(newBids);
						this.allBids.put(response.getBidder(), newBids); 
					}
				}
			}
		} else {
			// If the agent bids for the first time, then add those bids from the agent that are above the current price
			List<Bid> newBids = new ArrayList<>();
			response.getBids().forEach((Bid bid) -> {
				// Each bid has to be higher than the the minimal price of the auction at the moment
				if(bid.getPrice() > this.currentPrice){
					newBids.add(bid);
					this.bidsThisRound.add(bid);
				}
			}); 
			// Register the bids
			newBids.sort(Bid.COMPARATOR);
			this.allBids.put(response.getBidder(), newBids);
		}  
	}
	
	/** The bids are ordered and the top bids are winners until the available quantity is reached. If an bid is a winning bid, 
	 * then its price per unit is that of the next bid.If there is no next bid, then the bid's original price is used. */
	protected final AuctionResult<T> getPersonalResults(){  
		List<AuctionPersonalResult> winners = new ArrayList<>();
		List<Bid> allBidsSorted = getAllBidsFlatAndSorted();
		int quantityAvailable = this.trigger.getQuantity();
		// Assign winning bids until the available quantity is assigned, or you run out of bids
		for(int i = 0; i < allBidsSorted.size() && quantityAvailable > 0; i++){
			Bid bid = allBidsSorted.get(i); 
			winners.add(new AuctionPersonalResult(bid, bid.getPrice(), Math.min(quantityAvailable, bid.getQuantity())));
			quantityAvailable -= bid.getQuantity();
		}  
		return new AuctionResult<T>(super.auctionID, ResultType.FINISHED, this.trigger, winners, allBidsSorted, this.currentPrice, Math.max(0, quantityAvailable), this.trigger.getDecrementPerRound()); 
	}
}
