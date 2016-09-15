package tuPrologCapability; 
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Var;
import oo2apl.agent.AgentBuilder; 
/**
 * 
 * This capability allows an agent to use Prolog by utilizing tuProlog: http://apice.unibo.it/xwiki/bin/view/Tuprolog/WebHome 
 * 
 * 
 * @author Bas Testerink
 *
 */
public class TuPrologCapability extends AgentBuilder {
	/** Including this capability allows an agent to use TuProlog engines. An example usage is to model declarative beliefs in a 
	 * Prolog file and reason about them using this context. E.g. for a PlanToAgentInterface planInt: 
	 * UUID beliefBase = planInt.getContext(TuPrologContext.class).newPrologEngine("MyBeliefBase.pl"); 
	 * boolean propositionValuation = planInt.getContext(TuPrologContext.class).queryIfTrue(beliefBase, "proposition");
	 * */
	public TuPrologCapability(){
		super.addContext(new TuPrologContext()); 
	}  

	public static final String resultToJSONString(final SolveInfo result) throws NoSolutionException {
		StringBuffer str = new StringBuffer();
		if(result.isSuccess()){
			str.append("[");
			for(Var var : result.getBindingVars())
				str.append("{\"name\":\""+var.getName()+"\", \"value\": \""+var.getTerm()+"\"},");
			if(!result.getBindingVars().isEmpty()) str.deleteCharAt(str.length()-1);
			str.append("]");
		} else str.append("\"false\"");
		return str.toString();
	}
}
