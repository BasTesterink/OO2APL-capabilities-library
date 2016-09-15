package auctionCapability.auctionspecifications;

import java.util.ArrayList;
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
 * A Vickrey auction is a one-round auction. The bidders can place price/quantity bids and these bids are then ordered. 
 * Until the quantity is reached, each highest bid is honered with the price of the one below it. 
 * 
 * @author Bas Testerink
 * @param <T>
 */
public final class VickreyAuction<T> extends Auction<T> {
	private final List<Bid> bids;

	public VickreyAuction(final OrganizeAuction<T> trigger, final UUID auctionID) {
		super(trigger, auctionID);  
		this.bids = new ArrayList<>();
	}

	/** {@inheritDoc} */
	public final AuctionAnnouncement<T> makeInitialAnnouncement(final UUID auctionID, final AgentID auctioneer){
		return new AuctionAnnouncement<>(auctionID, auctioneer, this.trigger.getType(), this.trigger.getObjectForSale(), this.trigger.getMinimalPrice(), this.trigger.getQuantity(), this.trigger.getDecrementPerRound());
	}
	
	/** A Vickrey auction is finished with one round. */
	protected final boolean isFinished(){ return true; }

	/** A Vickrey auction does not need to implement the round method. */
	protected final AuctionResult<?> nextRound(){ return AuctionResult.WAITING; }

	/** {@inheritDoc} */
	protected final void storeBids(final ParticipantResponse response){
		// Store a bid if it exceeds the minimal price
		response.getBids().forEach((Bid bid) -> {
			if(bid.getPrice() >= this.trigger.getMinimalPrice())
				this.bids.add(bid);
		});  
	}
	
	/** The bids are ordered and the top bids are winners until the available quantity is reached. If a bid is a winning bid, 
	 * then its price per unit is that of the next bid. If there is no next bid, then the bid's original price is used. */
	protected final AuctionResult<T> getPersonalResults(){  
		List<AuctionPersonalResult> winners = new ArrayList<>(); 
		// Sort the bids
		this.bids.sort(Bid.COMPARATOR);
		// Keep track of how many units are assigned
		int quantityAvailable = this.trigger.getQuantity();
		// Keep assigning winning bids until you run out of bids or stock
		for(int i = 0; i < this.bids.size() && quantityAvailable > 0; i++){
			Bid bid = this.bids.get(i);
			double paidPrice = bid.getPrice(); 
			// The bid's price is overwritten if there is a next bid
			if(i+1 < this.bids.size())
				paidPrice = this.bids.get(i+1).getPrice();
			// The quantity of the bid is the quantity that its bidder desired, unless there is not enough available. 
			// In the latter case, the assigned quantity is whatever is left.
			winners.add(new AuctionPersonalResult(bid, paidPrice, Math.min(quantityAvailable, bid.getQuantity())));
			quantityAvailable -= bid.getQuantity();
		} 
		return new AuctionResult<T>(super.auctionID, ResultType.FINISHED, this.trigger, winners, this.bids, 0d, Math.max(0, quantityAvailable), this.trigger.getDecrementPerRound()); 
	}
}