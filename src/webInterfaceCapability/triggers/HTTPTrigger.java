package webInterfaceCapability.triggers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import oo2apl.agent.Trigger;
import webInterfaceCapability.DoubleResponseException;
/**
 * This class represents input from an HTTP request and originates from the web server capability.
 *   
 * @author Bas Testerink
 */
public final class HTTPTrigger implements Trigger {
	private final HttpExchange exchange;
	private final static String DEFAULT_RESPONSE = "{\"status\": \"Message received\"}";
	private boolean responded = false;
	private String requestBody;

	public HTTPTrigger(final HttpExchange exchange){
		this.exchange = exchange; 
	}

	public final URI getURI(){ 
		return this.exchange.getRequestURI();
	}

	public final String getRequestBody() throws IOException{
		if(this.requestBody == null){
			InputStreamReader input =  new InputStreamReader(this.exchange.getRequestBody(),"utf-8");
			BufferedReader br = new BufferedReader(input);
			int b;
			StringBuffer buffer = new StringBuffer(); 
			while ((b = br.read()) != -1)
				buffer.append((char) b);
			br.close();
			input.close();
			this.requestBody = buffer.toString();	 
		} 
		return this.requestBody;
	}

	/** Send the default acknowledgement that the message has been received. Only one answer can be send
	 *  per trigger. */
	public final void sendDefaultAnswer() throws IOException, DoubleResponseException {
		sendAnswer(DEFAULT_RESPONSE);
	}

	/** Send an answer to an http request. Only one answer can be send per trigger. */
	public final void sendAnswer(final String answer) throws IOException, DoubleResponseException {
		if(this.responded) throw new DoubleResponseException();
		else {
			Headers responseHeaders = this.exchange.getResponseHeaders();
			responseHeaders.set("Content-Type","application/jsonp; charset=UTF-8");
			responseHeaders.add("Access-Control-Allow-Origin","*");
			responseHeaders.add("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept");
			responseHeaders.add("Access-Control-Allow-Methods","POST, GET, OPTIONS");
			this.exchange.sendResponseHeaders(200, 0);
			OutputStream os = this.exchange.getResponseBody();
			os.write(answer.toString().getBytes("UTF-8"));
			os.flush();
			os.close();
			this.exchange.close();
		}
	}
	
	/** Obtain the original exchange. */
	public final HttpExchange getExchange(){
		return this.exchange;
	}
}
