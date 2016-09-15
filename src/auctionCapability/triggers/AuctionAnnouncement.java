
package auctionCapability.triggers;
 
import java.util.UUID;

import auctionCapability.triggers.OrganizeAuction.AuctionType;
import oo2apl.agent.AgentID;
import oo2apl.agent.Trigger;
/**
 * This class represents the event of an auctioneer organizing an auction.
 * @author Bas Testerink
 * @param <T>
 */
public final class AuctionAnnouncement<T> implements Trigger {
	private final UUID auctionID;  
	private final AgentID auctioneer;
	private final AuctionType type;
	private final T itemForSale;
	private final double price;  
	private final double decrement;  
	private final int quantityAvailable; 
	
	/** 
	 * @param auctionID Used by auctioneer to identify the auction to which a bid was made. Use this ID when replying to this trigger.
	 * @param auctioneer The auctioneer of the auction.
	 * @param type The type of auction such as Vickrey, English or Dutch.
	 * @param itemForSale The item that is on sale. 
	 * @param price The minimum price per unit in a Vickrey and English auction, and the current ticker price of a Dutch auction. 
	 * @param quantityAvailable The amount of units that are for sale. 
	 */
	public AuctionAnnouncement(final UUID auctionID, final AgentID auctioneer, final AuctionType type, final T itemForSale, final double price, final int quantityAvailable, final double decrement){
		this.auctionID = auctionID;
		this.auctioneer = auctioneer;
		this.type = type;
		this.itemForSale = itemForSale;
		this.price = price;
		this.quantityAvailable = quantityAvailable;
		this.decrement = decrement;
	}
	
	public final AuctionType getType(){ return this.type; } 
	public final UUID getAuctionID(){ return this.auctionID; } 
	public final AgentID getAuctioneer(){ return this.auctioneer; } 
	public final T getItemForSale(){ return this.itemForSale; } 
	public final double getPrice(){ return this.price; }  
	public final int getQuantityAvailable(){ return this.quantityAvailable; }  
	public final double getDecrement(){ return this.decrement; }  
}
