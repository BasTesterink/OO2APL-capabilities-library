package argumentationToastCapability;

import java.util.UUID;

import oo2apl.agent.AgentBuilder; 
/**
 * 
 * This capability allows an agent to use TOAST to evaluate argumentation theories. It is 
 * still a very early version that can only return TOAST output as a JSON object. Future 
 * updates will include a more advanced query API for argumentation.
 *  
 * @author Bas Testerink
 */
public final class ArgumentationToastCapability extends AgentBuilder {
	/** Including this capability allows an agent to use TOAST compatible argumentation theories and 
	 * evaluated them. Example usage for a PlanToAgentInterface planInt:
	 * 
	 * <code>
	 * ToastContext context = planInt.getContext(ToastContext.class);
	 * UUID id = planInt.getContext(ToastContext.class).newArgumentationTheory();
		ArgumentationTheory at = context.getArgumentationTheory(id) 
				.addPremise("i2")
				.addPremise("i3")
				.addPremise("i5")
				.addPremise("i7")
				.addPremise("i8")
				.putDefeasibleRule("r1", "i1", "i2","i3") 	// [r1] i1 <= i2, i3
				.putStrictRule("r2", "i4", "i5","i1") 		// [r2] i4 <- i5, i1
				.putDefeasibleRule("r3", "i6", "i7")		// [r3] i6 <= i7
				.putStrictRule("r4", "i9", "i8","i6")		// [r4] i9 <- i8, i6
				.addContradictoryPair("i5", "i9");
		System.out.println(at.getTOASTOutput());
		context.removeArgumentationTheory(id); </code> 
	 * 
	 * Note: You can host TOAST yourself or use "http://www.arg.dundee.ac.uk/toast/api/evaluate"
	 * */
	public ArgumentationToastCapability(final String toastWebInterfaceURL){
		super.addContext(new ToastContext(toastWebInterfaceURL)); 
	}   
}
