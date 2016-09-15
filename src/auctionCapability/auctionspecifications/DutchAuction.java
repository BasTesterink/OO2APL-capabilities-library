package auctionCapability.auctionspecifications;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * A Dutch auction has a current price that drops per round. A bidder can accept the current 
 * price for a given quantity of goods. 
 * 
 * @author Bas Testerink
 * @param <T>
 */
public final class DutchAuction<T> extends Auction<T> {
	private final List<Bid> allBids;
	private List<Bid> bidsThisRound;
	private int quantityAvailable;
	private double currentPrice;

	public DutchAuction(final OrganizeAuction<T> trigger, final UUID auctionID) {
		super(trigger, auctionID);  
		this.allBids = new ArrayList<>();
		this.bidsThisRound = new ArrayList<>();
		this.quantityAvailable = trigger.getQuantity();
		this.currentPrice = trigger.getMaximalPrice();
	}
	
	/** {@inheritDoc} */
	public final AuctionAnnouncement<T> makeInitialAnnouncement(final UUID auctionID, final AgentID auctioneer){
		return new AuctionAnnouncement<>(auctionID, auctioneer, this.trigger.getType(), this.trigger.getObjectForSale(), this.currentPrice, this.quantityAvailable, this.trigger.getDecrementPerRound());
	}

	/** A Dutch auction is finished if there are no more units to sell or if another round would drop the price below minimum. */
	protected final boolean isFinished(){ 
		return this.quantityAvailable <= 0 
				|| (this.currentPrice - this.trigger.getDecrementPerRound()) < this.trigger.getMinimalPrice(); 
		}

	/** A Dutch round consists of decrementing the price and making a report. */
	protected final AuctionResult<?> nextRound(){ 
		// Lower the price
		this.currentPrice -= this.trigger.getDecrementPerRound();	 
		// Make report
		this.bidsThisRound.sort(Bid.COMPARATOR);
		AuctionResult<T> result = new AuctionResult<T>(super.auctionID, ResultType.NEWROUND, this.trigger, Collections.emptyList(), this.bidsThisRound, this.currentPrice, this.quantityAvailable, this.trigger.getDecrementPerRound());
		this.bidsThisRound = new ArrayList<>();
		return result;	
	}

	/** {@inheritDoc} */
	protected final void storeBids(final ParticipantResponse response){
		response.getBids().forEach((Bid bid) ->{
			// If the agent accepted the current price, then add its bid
			if(bid.getQuantity() > 0){ 
				// Ensure that the current price is used
				Bid price = (bid.getPrice() == this.currentPrice? bid : new Bid(this.currentPrice, bid.getQuantity(), bid.getBidder()));
				this.allBids.add(price);
				this.bidsThisRound.add(price); 
				// Update the available quantity
				this.quantityAvailable -= bid.getQuantity();
			}
		}); 
	}
	
	/** The bids are ordered and the top bids are winners until the available quantity is reached. If a bid is a winning bid, 
	 * then its price per unit is that of the next bid.If there is no next bid, then the bid's original price is used. */
	protected final AuctionResult<T> getPersonalResults(){  
		List<AuctionPersonalResult> winners = new ArrayList<>();
		// Sort the bids from highest to lowest
		this.allBids.sort(Bid.COMPARATOR);
		// Reset quantity
		this.quantityAvailable = this.trigger.getQuantity();
		// Add winners until quantity or end of bids is reached
		for(int i = 0; i < this.allBids.size() && this.quantityAvailable > 0; i++){
			Bid bid = this.allBids.get(i);
			// Ensure that not too many are sold because at the final price multiple bids came in
			winners.add(new AuctionPersonalResult(bid, bid.getPrice(), Math.min(this.quantityAvailable, bid.getQuantity()))); 
			this.quantityAvailable -= bid.getQuantity();
		} 
		return new AuctionResult<T>(super.auctionID, ResultType.FINISHED, this.trigger, winners, this.allBids, this.currentPrice, Math.max(0, this.quantityAvailable), this.trigger.getDecrementPerRound()); 
	}
}