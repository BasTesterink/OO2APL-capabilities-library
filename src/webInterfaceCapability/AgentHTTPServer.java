package webInterfaceCapability;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
 
/**
 * This server allows an agent to receive and request http input. It is used through the WebServerContext.
 * 
 * @author Bas Testerink
 *
 */
public final class AgentHTTPServer { 
	private boolean isRunning = false;
	private HttpServer server;	// Built-in Java server
	private final HttpHandler handler; // Handler of request, which is intended to be the WebServerContext.
	private int port; // Port to be used 

	/**
	 * @param port Port to connect to.
	 */
	public AgentHTTPServer(final HttpHandler handler, final int port){
		this.handler = handler;
		this.port = port;
	}   

	/**
	 * Create and start a new server. If a previous server was created then it is stopped. 
	 */
	public final void startServer(){
		try {
			if(this.server != null && this.isRunning)
				stopServer();
			this.server = HttpServer.create(new InetSocketAddress(this.port), 0);
			HttpContext context = this.server.createContext("/", this.handler);
			context.getFilters().add(new ParameterFilter());
			this.server.setExecutor(null);
			this.server.start();
			this.isRunning = true;
		} catch (IOException e){  
			e.printStackTrace();
		}
	}
	
	/**
	 * Stops the server.
	 */
	public final void stopServer(){
		if(this.server != null && this.isRunning){
			this.server.stop(0);
			this.isRunning = false;
		}
	}
}
