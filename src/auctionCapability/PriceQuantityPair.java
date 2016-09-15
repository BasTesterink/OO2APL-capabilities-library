package auctionCapability;
 /**
  * Auxiliary class for storing a pair of a price per unit and quantity. 
  * @author Bas Testerink
  */
public final class PriceQuantityPair {
	private final double price;
	private final int quantity;
	public PriceQuantityPair(final double price, final int quantity){
		this.price = price;
		this.quantity = quantity;
	}
	public final double getPrice(){ return this.price; }
	public final int getQuantity(){ return this.quantity; }
}