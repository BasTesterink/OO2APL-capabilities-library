package auctionCapability;

import auctionCapability.BuyerContext.EvaluationFunction;
/**
 * Container to maintain a demand for a unit by an agent. 
 * @author Bas Testerink
 * @param <T>
 */
public final class Demand<T> {
	private final EvaluationFunction<T> evaluationFunction;
	private int desiredQuantity;
	
	/**
	 * Constructor
	 * @param evaluationFunction A function that tells the maximum price that an agent is willing to pay for a given unit.
	 * @param desiredQuantity The amount of units that the agent wants.
	 */
	public Demand(final EvaluationFunction<T> evaluationFunction, final int desiredQuantity){
		this.evaluationFunction = evaluationFunction;
		this.desiredQuantity = desiredQuantity; 
	}
	public EvaluationFunction<T> getEvaluationFunction(){ return this.evaluationFunction; }
	public int getDesiredQuantity(){ return this.desiredQuantity; }
	public void setDesiredQuantity(int desiredQuantity){ this.desiredQuantity = desiredQuantity; }
}