package auctionCapability.auctionspecifications;
  
import java.util.UUID;

import auctionCapability.triggers.AuctionAnnouncement;
import auctionCapability.triggers.AuctionResult;
import auctionCapability.triggers.OrganizeAuction;
import auctionCapability.triggers.ParticipantResponse;
import oo2apl.agent.AgentID;
/** * An auction specifies the rules of the auction; i.e. the state of the auction, how this state transitions over rounds (if applicable) 
 * and how bids are processed. 
 * 
 * @author Bas Testerink
 * @param <T>
 */
public abstract class Auction<T> {
	protected final OrganizeAuction<T> trigger; 
	protected final UUID auctionID;
	private int responseCounter;  
	
	public Auction(final OrganizeAuction<T> trigger, final UUID auctionID){
		this.trigger = trigger; 
		this.auctionID = auctionID; 
		this.responseCounter = 0; 
	} 
	
	/** Process the response of an auction participant. */
	public final AuctionResult<?> handleParticipantResponse(final ParticipantResponse response){
		this.responseCounter++; 
		// Store the bids
		storeBids(response); 
		// Check if all awaited responses are in
		if(this.responseCounter == this.trigger.getParticipants().size()){ 
			// Reset the counter
			this.responseCounter = 0; 
			// Check for winners if the auction is finished
			if(isFinished()) return getPersonalResults(); 
			else return nextRound(); // Otherwise move to next round
		}
		return AuctionResult.WAITING;
	}
	
	/** Create the announcement that is sent to participant upon the creation of the auction. */
	public abstract AuctionAnnouncement<T> makeInitialAnnouncement(final UUID auctionID, final AgentID auctioneer);
	
	/** A check to determine whether the auction is finished. Will be called after all participants have responded. */
	protected abstract boolean isFinished();
	
	/** If this is a multi-round auction, then implement this method to determine how the auction transitions to the next round. 
	 * This method will be called if isFinished() returns false after all the participants have responded. */
	protected abstract AuctionResult<?> nextRound();
	
	/** Store the bids of a participant. */
	protected abstract void storeBids(final ParticipantResponse response);

	/** Produce the final report of the auction. 
	 * This method will be called if isFinished() returns false after all the participants have responded. */
	protected abstract AuctionResult<T> getPersonalResults();
}
