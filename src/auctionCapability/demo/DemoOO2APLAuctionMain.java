package auctionCapability.demo;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import auctionCapability.triggers.OrganizeAuction.AuctionType;
import oo2apl.agent.AgentID;
import oo2apl.agent.ExternalProcessToAgentInterface;
import oo2apl.defaults.messenger.DefaultMessenger;
import oo2apl.platform.AdminToPlatformInterface;
import oo2apl.platform.Platform;

public class DemoOO2APLAuctionMain {   
	private final static Random random = new Random(3);
	
	public static void main(String[] args){ 
		DemoOO2APLAuctionMain.englishAuctionDemo();
		//DemoOO2APLAuctionMain.dutchAuctionDemo();
		//DemoOO2APLAuctionMain.vickreyAuctionDemo(); 
	}  

	public static final void englishAuctionDemo(){  
		auctionDemo(1, 20d, 120d, 10, AuctionType.ENGLISH); 
	}
	
	public static final void dutchAuctionDemo(){ 
		auctionDemo(1, 20d, 120d, 10, AuctionType.DUTCH); 
	}
	
	public static final void vickreyAuctionDemo(){ 
		auctionDemo(1, 20d, 120d, 10, AuctionType.VICKREY); 
	}
	
	private static final void auctionDemo(final int quantityToSell, final double minimalPricePerUnit, final double maximalPricePerUnit, final double decrementPerRound,final AuctionType auctionType){
		// Create a platform
		AdminToPlatformInterface adminInterface = Platform.newPlatform(1, new DefaultMessenger());
		
		// Create buyers
		List<AgentID> buyers = new ArrayList<>(); 
		for(int i = 0; i < 10; i++){ 
			final int value =  random.nextInt(100);
			ExternalProcessToAgentInterface buyer = adminInterface.newAgent(BookTraderAgent.makeBookTraderAgent((Book book)->{return value;}));
			buyers.add(buyer.getAgentID());
			System.out.println("Created a buyer that values the book for "+value+" and with id: "+buyer.getAgentID());
		} 
		
		// Create seller
		ExternalProcessToAgentInterface seller = adminInterface.newAgent(BookTraderAgent.makeBookTraderAgent((Book book)->{return 0d;}));
		System.out.println("Created a seller with id: "+seller.getAgentID());
		
		// Trigger the seller to organize the auction
		seller.addExternalTrigger(new SellBookTrigger(new Book(), buyers, quantityToSell, minimalPricePerUnit, maximalPricePerUnit, decrementPerRound, auctionType)); 
	}
}