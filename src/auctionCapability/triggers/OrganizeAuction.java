package auctionCapability.triggers;

import java.util.List;

import oo2apl.agent.AgentID;
import oo2apl.agent.Trigger;
/**
 * This trigger represents that the agent wants to organize an auction. It can be adopted for instance as an internal trigger which then fires the 
 * organizing plan schemes that take care of the auction itself.
 * 
 * @author Bas Testerink
 */
public final class OrganizeAuction<T> implements Trigger {
	public static enum AuctionType {VICKREY, ENGLISH, DUTCH};  
	private final AuctionType type;
	private final T objectForSale;
	private final List<AgentID> participants;  
	private final double minimalPrice; 
	private final double maximalPrice; // For Dutch auction
	private final double decrementPerRound; // For Dutch auction
	private final int quantity; // Available quantity 
	
	public OrganizeAuction(final AuctionType type, final T objectForSale, final List<AgentID> participants, final double minimalPrice,
			final double maximalPrice, final double decrementPerRound, final int quantity){
		this.type = type;
		this.objectForSale = objectForSale;
		this.participants = participants; 
		this.minimalPrice = minimalPrice;
		this.maximalPrice = maximalPrice;
		this.decrementPerRound = decrementPerRound;
		this.quantity = quantity; 
	} 

	public final AuctionType getType(){ return this.type; } 
	public final T getObjectForSale(){ return this.objectForSale; } 
	public final List<AgentID> getParticipants(){ return this.participants; } 
	public final double getMinimalPrice(){ return this.minimalPrice; } 
	public final double getMaximalPrice(){ return this.maximalPrice; }
	public final double getDecrementPerRound(){ return this.decrementPerRound; }
	public final int getQuantity(){ return this.quantity; } 
	
}
