package auctionCapability;
 
import java.util.Comparator;

import oo2apl.agent.AgentID;
/**
 * A bid is a data container that maintains a price per unit and desired quantity for a given bidder. The bidder 
 * is not necessarily a participant in the auction. This happens when a participant bids on behalf of another agent.
 * 
 * @author Bas Testerink
 */
public final class Bid implements Comparable<Bid> { 
	/** COMPARATOR.compare(a, b) returns -1 if a is bigger than b, 0 if equal, and 1 otherwise. */
	public static final Comparator<Bid> COMPARATOR = (Bid a, Bid b)-> { return a.compareTo(b); };
	/** The bid of the agent. */
	private final double price; // The amount that was bid
	/** The quantity of untis that the agent desires. */
	private final int quantity; // Quantity of the bid
	/** The ID of the agent to which this bid belongs. */
	private final AgentID bidder; // The agent that made the bid, used for the comparable implementation

	/**
	 * Constructor.
	 * @param price The price that the agent is bidding. Note that in for instance in a Dutch auction the price can be reset to match the current price of the auction.
	 * @param quantity The amount of units that the agent wants. The assigned quantity might be less, even if the agent wins, depending on the available quantity and other bids.
	 * @param bidder The agent on who's behalf this bid was made.
	 */
	public Bid(final double price, final int quantity, final AgentID bidder){ 
		this.price = price;
		this.quantity = quantity;
		this.bidder = bidder;
	}  
	
	// Getters
	public final double getPrice(){ return this.price; }
	public final int getQuantity(){ return this.quantity; }
	public final AgentID getBidder(){ return this.bidder; }
 
	/**
	 * First order on price per unit as this is important to the auctioneer.
	 * Then order on the quantity, as these are the participants that want to spend the most.
	 * Then order on bidder name, to ensure reproducibility, under the assumption that AgentID.toString() is unique for each existing agent ID.
	 */
	public final int compareTo(Bid o){ 
		if(this.price > o.getPrice()) return -1;
		else if(this.price < o.getPrice()) return 1;
		else if(this.quantity > o.getQuantity()) return -1;
		else if(this.quantity < o.getQuantity()) return 1;
		else if(this.getBidder() == null) return 1; // In case this is a dummy bid
		else if(o.getBidder() == null) return -1;	// in case o is a dummy bid
		else return o.getBidder().toString().compareTo(this.bidder.toString()); // Note that each agent ID is unique and will not compare to 0
	}
}