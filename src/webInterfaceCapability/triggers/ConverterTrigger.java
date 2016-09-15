package webInterfaceCapability.triggers;

import oo2apl.agent.Trigger;
import webInterfaceCapability.HTTPTriggerConverter;
/**
 * Trigger that can be adopted in order to add a trigger converter to the web capability context.
 * 
 * @author Bas Testerink
 */
public final class ConverterTrigger implements Trigger {
	private final HTTPTriggerConverter converter;
	private final boolean remove;
	
	public ConverterTrigger(final HTTPTriggerConverter converter, final boolean remove){
		this.converter = converter;
		this.remove = remove;
	}
	
	public final HTTPTriggerConverter getConverter(){
		return this.converter;
	}
	
	public final boolean isRemove(){ return this.remove; }
}
