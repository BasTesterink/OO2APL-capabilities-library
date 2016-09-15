package auctionCapability;
 
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import auctionCapability.triggers.AuctionAnnouncement;
import auctionCapability.triggers.AuctionResult;
import auctionCapability.triggers.OrganizeAuction;
import auctionCapability.triggers.ParticipantResponse;
import auctionCapability.triggers.OrganizeAuction.AuctionType;
import oo2apl.agent.AgentID;
import oo2apl.agent.Context;
import auctionCapability.auctionspecifications.Auction;
import auctionCapability.auctionspecifications.DutchAuction;
import auctionCapability.auctionspecifications.EnglishAuction;
import auctionCapability.auctionspecifications.VickreyAuction; 
/**
 * Context that is used for bookkeeping data of auctions of which the agent is the auctioneer.
 * 
 * @author Bas Testerink
 *
 */
public final class AuctioneerContext implements Context {
	/** The current ongoing auctions. */
	private final Map<UUID, Auction<?>> auctions;
	
	public AuctioneerContext(){
		this.auctions = new HashMap<>();
	}
	
	/**
	 * Register a new auction and produce the announcement that must be send to the participants.
	 * @param trigger The trigger that prompted the organization of the auction.
	 * @param auctioneer The agent that is organizing the auction.
	 * @return An announcement to be send to all the participants of the auctions. This in turn will prompt them to return bids.
	 */
	public final <T> AuctionAnnouncement<T> newAuction(final OrganizeAuction<T> trigger, final AgentID auctioneer){ 
		UUID auctionID = UUID.randomUUID();
		while(this.auctions.get(auctionID) != null) // This is unlikely to happen but still, just to be safe...
			auctionID = UUID.randomUUID(); 
		Auction<T> auction = trigger.getType() == AuctionType.VICKREY ? new VickreyAuction<>(trigger, auctionID) :(
							 trigger.getType() == AuctionType.ENGLISH ? new EnglishAuction<>(trigger, auctionID) : (
							 trigger.getType() == AuctionType.DUTCH ? new DutchAuction<>(trigger, auctionID) : null)); 
		this.auctions.put(auctionID, auction); 
		return auction.makeInitialAnnouncement(auctionID, auctioneer); 
	}
	
	/**
	 * Register the response of a participant of an auction. 
	 * @param participantResponse Response that is received from a participant in one of the active auctions. 
	 * @return The result indicates whether the auction is finished (its type is then AuctionResult.FINISHED) or whether it has entered a new round (AuctionResult.NEWROUND), or whether not all bids for this round are in yet (AuctionResult.WAITING)
	 */
	public final AuctionResult<?> handleParticipantResponse(final ParticipantResponse participantResponse){
		Auction<?> data = this.auctions.get(participantResponse.getAuctionID()); 
		return data.handleParticipantResponse(participantResponse);
	}
	
	/** Remove the data of an auction. */
	public final void clearData(final UUID auctionID){
		this.auctions.remove(auctionID);
	} 
}
