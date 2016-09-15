package auctionCapability;
 
/**
 * Container that holds for a winning bidder for what price and quantity it has won.
 * 
 * @author Bas Testerink
 */
public final class AuctionPersonalResult {
	private final Bid bid; // The original bid
	private final double price; // The price that the agent has to pay
	private final int quantity; // The amount of units that the agent has won
	
	/**
	 * 
	 * @param bid The original bid
	 * @param price The price that the agent has to pay
	 * @param quantity The amount of units that the agent has won
	 */
	public AuctionPersonalResult(final Bid bid, final double price, final int quantity){
		this.bid = bid;
		this.price = price;
		this.quantity = quantity; 
	}
	
	public final Bid getBid(){ return this.bid; }
	public final double getPrice(){ return price; }  
	public final int getQuantity(){ return this.quantity; } 
	public static final AuctionPersonalResult NOTWON = new AuctionPersonalResult(null, 0d, 0);
}
