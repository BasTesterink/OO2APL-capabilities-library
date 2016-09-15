package webInterfaceCapability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection; 
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import agent.webcapability.triggers.HTTPTrigger;
import oo2apl.agent.Context;
import oo2apl.agent.ExternalProcessToAgentInterface;
import oo2apl.agent.Trigger;
/**
 * The web server context maintains the server of the agent. Currently only supports a single server.
 * 
 * @author Bas Testerink
 */
public final class WebServerContext implements Context, HttpHandler {
	private ExternalProcessToAgentInterface triggerPublisher; // The interface to send http requests as triggers to
	private AgentHTTPServer server;
	private final List<HTTPTriggerConverter> converters = new ArrayList<>(); // Converters that convert http requests to internal triggers
	
	/** Start the server. */
	public final void startServer(final int port){
		if(this.server != null) this.server.stopServer();
		this.server = new AgentHTTPServer(this, port);
		this.server.startServer();

		System.out.println("Started server on port "+port);
	}

	/** Stop the server. */
	public final void stopServer(){
		if(this.server != null){
			this.server.stopServer();
		}
	}
 
	/** Add a converter to the context. Converters are applied to new triggers in the order of their addition. */
	public final void addHTTPTriggerConverter(final HTTPTriggerConverter converter){
		this.converters.add(converter);
	}
	
	/** Remove a converter. */
	public final void removeHTTPTriggerConverter(final HTTPTriggerConverter converter){
		this.converters.remove(converter);
	}
	 
	public final void handle(final HttpExchange exchange) throws IOException {
		HTTPTrigger trigger = new HTTPTrigger(exchange);
		if(this.triggerPublisher == null){
			try {
				trigger.sendAnswer("Server can't reach agent");
			} catch (DoubleResponseException e) { 
				e.printStackTrace();
			}
		} else {
			// Try to see if a converter can transform the trigger
			Trigger conversion = HTTPTriggerConverter.NOT_CONVERTED;
			for(int i = 0; i < this.converters.size() && conversion == HTTPTriggerConverter.NOT_CONVERTED; i++)
				conversion = this.converters.get(i).convert(trigger.getURI(), trigger.getRequestBody());
			// If not, then the trigger is adopted as an external trigger
			if(conversion == HTTPTriggerConverter.NOT_CONVERTED)
				this.triggerPublisher.addExternalTrigger(trigger);
			else {
				// Otherwise send the default answer to close the connection and then adopt the trigger as an external trigger.
				try {
					trigger.sendDefaultAnswer();
				} catch (DoubleResponseException e) { 
					e.printStackTrace();
				}
				this.triggerPublisher.addExternalTrigger(conversion);
			}
		}
	}

	public final String getPage(final String address) throws IOException {
		StringBuffer result = new StringBuffer();
		URL url = new URL(address);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET"); 
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line; 
		while ((line = rd.readLine()) != null)
			result.append(line);
		rd.close(); 
		return result.toString();
	}
	
	public final void setTriggerPublisher(final ExternalProcessToAgentInterface triggerPublisher){ this.triggerPublisher = triggerPublisher; }
}
