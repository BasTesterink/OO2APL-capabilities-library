package auctionCapability.demo;

import java.util.List;

import auctionCapability.triggers.OrganizeAuction.AuctionType;
import oo2apl.agent.AgentID;
import oo2apl.agent.Trigger;
/**
 * This trigger prompts the demo seller to start organizing an auction.
 * @author Bas Testerink 
 */
public class SellBookTrigger implements Trigger {
	private final Book book;
	private final List<AgentID> buyers;
	private final int quantityToSell;
	private final double minimalPricePerUnit, maximalPricePerUnit, decrementPerRound;
	private final AuctionType auctionType;
	
	public SellBookTrigger(final Book book, final List<AgentID> buyers, final int quantityToSell, final double minimalPricePerUnit, final double maximalPricePerUnit, final double decrementPerRound, final AuctionType auctionType){
		this.book = book;
		this.buyers = buyers;
		this.quantityToSell = quantityToSell;
		this.minimalPricePerUnit = minimalPricePerUnit;
		this.maximalPricePerUnit = maximalPricePerUnit;
		this.decrementPerRound = decrementPerRound;
		this.auctionType = auctionType;
	}

	public final Book getBook(){ return this.book; }
	public final List<AgentID> getBuyers(){ return this.buyers; }
	public final int getQuantityToSell(){ return this.quantityToSell; }
	public final double getMinimalPricePerUnit(){ return this.minimalPricePerUnit; }
	public final double getMaximalPricePerUnit(){ return this.maximalPricePerUnit; }
	public final double getDecrementPerRound(){ return this.decrementPerRound; }
	public final AuctionType getAuctionType(){ return this.auctionType; }
}
