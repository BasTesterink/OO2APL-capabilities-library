package webInterfaceCapability;

import java.io.IOException;
 
import oo2apl.agent.AgentBuilder;
import oo2apl.agent.AgentContextInterface;
import oo2apl.agent.PlanToAgentInterface;
import oo2apl.agent.Trigger;
import oo2apl.plan.builtin.SubPlanInterface;
import webInterfaceCapability.triggers.ConverterTrigger;
import webInterfaceCapability.triggers.HTTPTrigger;
import webInterfaceCapability.triggers.PageScrapeResult;
import webInterfaceCapability.triggers.ScrapePage;
import webInterfaceCapability.triggers.WebCapabilityInitiationTrigger;
/**
 * The web capability allows a programmer to easily make their agent accessible through HTTP requests. Currently the 
 * capability supports an easy-to-use system where converters can be added that convert an HTTP request to a trigger 
 * which is then added as an external trigger to the agent, as if the source of the trigger was local.
 * It is also possible to obtain web pages and parse them, the result is then added as an internal trigger. 
 * 
 * Note: I just used a built-in HTTP server which may require you to add the access rule com/sun/net/httpserver/* in your setup if 
 * you use Eclipse. 
 * 
 * @author Bas Testerink
 *
 */
public class WebCapability extends AgentBuilder {
	
	public WebCapability(){
		super.addContext(new WebServerContext());
		
		// Plan scheme to intialize the web capability. Will start the server.
		super.addInternalTriggerPlanScheme((Trigger trigger, AgentContextInterface contextInterface) -> {
			if(trigger instanceof WebCapabilityInitiationTrigger){
				WebCapabilityInitiationTrigger init = (WebCapabilityInitiationTrigger)trigger;
				return (PlanToAgentInterface planInterface) -> {
					WebServerContext context = planInterface.getContext(WebServerContext.class);
					context.startServer(init.getPort());
					context.setTriggerPublisher(init.getPublisher());
				};
			} else return SubPlanInterface.UNINSTANTIATED;
		});
		
		// Plan scheme to add converters to the web capability context. These converters will convert http requests
		// to triggers and add them as external triggers.
		super.addInternalTriggerPlanScheme((Trigger trigger, AgentContextInterface contextInterface) -> {
			if(trigger instanceof ConverterTrigger){
				ConverterTrigger conv = (ConverterTrigger) trigger;
				return (PlanToAgentInterface planInterface) -> {
					WebServerContext context = planInterface.getContext(WebServerContext.class);
					if(conv.isRemove()) context.removeHTTPTriggerConverter(conv.getConverter());
					else context.addHTTPTriggerConverter(conv.getConverter()); 
				};
			} else return SubPlanInterface.UNINSTANTIATED;
		});

		// Plan scheme to obtain and parse a page using http. The result is adopted as an internal trigger.
		super.addInternalTriggerPlanScheme((Trigger trigger, AgentContextInterface contextInterface) -> {
			if(trigger instanceof ScrapePage){
				ScrapePage req = (ScrapePage) trigger;
				return (PlanToAgentInterface planInterface) -> {
					WebServerContext context = planInterface.getContext(WebServerContext.class);
					boolean succeeded = false;
					String result;
					try {
						result = req.getParser().apply(context.getPage(req.getURL()));
						succeeded = true;
					} catch(IOException e){
						result = e.toString();
						succeeded = false;
					}
					planInterface.addInternalTrigger(new PageScrapeResult(succeeded, req.getURL(), result));
				};
			} else return SubPlanInterface.UNINSTANTIATED;
		});
	}
	
	/** Dummy trigger that is returned by checkTypeAndURL to test whether a trigger matched the type and URL that was specified. */
	public static final HTTPTrigger NO_MATCH = new HTTPTrigger(null);
	
	/** Return a HTTPTrigger cast if the input trigger is of the correct type and the url of the request matches. Otherwise returns 
	 * {@link WebCapability#NO_MATCH}*/
	public static HTTPTrigger checkTypeAndURL(final Trigger trigger, final String URL){
		if(trigger instanceof HTTPTrigger){
			HTTPTrigger http = (HTTPTrigger) trigger;
			if(http.getURI().toString().startsWith(URL)){
				return http;
			}
		}
		return NO_MATCH;
	}
}
