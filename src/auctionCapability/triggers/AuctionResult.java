package auctionCapability.triggers;
 
import java.util.List; 
import java.util.UUID;

import auctionCapability.AuctionPersonalResult;
import auctionCapability.Bid;
import oo2apl.agent.Trigger;
/**
 * A container that is used to report on the progress of an auction; either its final results or the inbetween results if a new round is entered. 
 * @author Bas Testerink
 * @param <T>
 */
public final class AuctionResult<T> implements Trigger {
	public static enum ResultType { FINISHED, NEWROUND, WAITING }
	/** Use this result to indicate that the current round is not finished yet. */
	public static final AuctionResult<Object> WAITING = new AuctionResult<>(null, null, null, null, null, 0d, 0, 0d);
	private final UUID auctionID;
	private final ResultType type;
	private final OrganizeAuction<T> trigger;
	private final List<AuctionPersonalResult> personalResults;
	private final List<Bid> bids;
	private final double price, decrement;
	private final int quantityAvailable;  

	/**
	 * 
	 * @param type Whether the auction is still in between rounds, is finished, or is entering a new round. 
	 * @param trigger The original trigger that prompted the auctioneer to organize an auction.
	 * @param winners The winning data of the auction. Contains bidders, the price they pay per unit and the quantity of units they won. 
	 * @param bids The bids above minimum that were placed in the auction so far. 
	 * @param price The current minimal price in a Vickrey or English auction or the clock price in a Dutch auction. 
	 * @param quantityAvailable The amount of units that are still left over. 
	 */
	public AuctionResult(final UUID auctionID, final ResultType type, final OrganizeAuction<T> trigger, final List<AuctionPersonalResult> personalResults, final List<Bid> bids, final double price, final int quantityAvailable, final double decrement){
		this.auctionID = auctionID;
		this.type = type;
		this.trigger = trigger;
		this.personalResults = personalResults;
		this.bids = bids;
		this.price = price;
		this.quantityAvailable = quantityAvailable;
		this.decrement = decrement;
	}

	public final UUID getAuctionID(){ return this.auctionID; }
	public final ResultType getType(){ return this.type; }
	public final OrganizeAuction<T> getTrigger(){ return this.trigger; }
	public final List<Bid> getBids(){ return this.bids; }
	public final List<AuctionPersonalResult> getPersonalResults(){ return this.personalResults; }
	public final double getPrice(){ return this.price; }
	public final int getQuantityAvailable(){ return this.quantityAvailable; }   
	public final double getDecrement(){ return this.decrement; }
}
