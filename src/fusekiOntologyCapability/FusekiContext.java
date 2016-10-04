package fusekiOntologyCapability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import oo2apl.agent.Context;

/**
 * This context communicates with Fuseki through its web interface. The Apache Jena 
 * project also has a Java library for Fuseki, but it seem to not add anything to the 
 * functionality of what we require (except maybe for a fast-fail syntax check of 
 * SPARQL queries). 
 * 
 * For now the context implementation assumes that there is only one Fuseki server 
 * that is being used (with possibly multiple RDF bases).  
 * 
 * Note: this is an early version. Most methods still have to be revised to accord for consecutive 
 * method calls that go to quick for the Fuseki server.
 * 
 * @author Bas Testerink
 *
 */
public final class FusekiContext implements Context {
	private final int port;
	private final Map<String, String> prefixes;
	private final String fusekiDirectory, pathToConfigTemplatesDirectory;
	private Process serverProcess;
	private Thread shutdownHook;
	
	public FusekiContext(final int port, final String fusekiDirectory, final String pathToConfigTemplatesDirectory){
		this.port = port;
		this.fusekiDirectory = fusekiDirectory;
		this.pathToConfigTemplatesDirectory = pathToConfigTemplatesDirectory;
		this.prefixes = new HashMap<>();
	}

	/** Start the Fuseki server. If the server is already running, then it is restarted. 
	 * @throws InterruptedException */
	public final void startServer() throws IOException, InterruptedException{
		if(this.serverProcess != null && this.serverProcess.isAlive())
			stopServer();
		
		// Setup the execution service
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final ProcessBuilder pb = new ProcessBuilder("java", "-jar", this.fusekiDirectory+"fuseki-server.jar", "--port", Integer.toString(this.port));
		pb.directory(new File(this.fusekiDirectory));
		
		// Made the process final so it can be used safely in the lambda function
		final Process process = pb.start();	
		this.serverProcess = process;
		
		// Make sure the server is halted whenever the system is stopped.
		this.shutdownHook = new Thread(()->{
			if(process.isAlive())
				try { 
					process.destroyForcibly().waitFor();
				} catch (Exception e) { 
					e.printStackTrace();
				}});
		Runtime.getRuntime().addShutdownHook(this.shutdownHook);
		
		// Submit the task of running the server, will direct output to System.out
		executor.submit(()->{
			try {
				// Redirect the output
				BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String s = "";
				while((s = in.readLine()) != null){
				    System.out.println(s); 
				} 
				System.out.println("Exited");
				
				// Shutdown the executor when finished
				executor.shutdown(); 
			} catch (IOException e) { 
				e.printStackTrace();
			} 
		});  
	}
	
	/** Stop the Fuseki server 
	 * @throws InterruptedException */
	public final void stopServer() throws InterruptedException{
		if(this.serverProcess != null && this.serverProcess.isAlive()){
			this.serverProcess.destroyForcibly().waitFor();
			Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
		}
	}
	
	/** Register in the context a new RDF base. This RDF base has to already exist on the Fuseki server.  
	 * The prefixes will be appended in front of every query that is used for the registered RDF base.  */
	public final void useRDFBase(final String name, final String prefixesToUseForQuery){
		this.prefixes.put(name, prefixesToUseForQuery);
		 
	}
	
	/** Creates a new dataset. Overwrite an existing dataset. */
	public final void addDataSet(final String name, final boolean isPersistent, final String prefixesToUseForQuery) throws IOException{ 
		HttpURLConnection connection = postData("POST",
				"http://localhost:"+this.port+"/$/datasets/",
				"application/x-www-form-urlencoded; charset=UTF-8",
				"dbName="+name+"&dbType="+(isPersistent?"tdb":"mem") );
		connection.disconnect();
		this.prefixes.put(name, prefixesToUseForQuery);
	}
	
	
	/** Remove a dataset from the server. */
	public final void removeDataset(final String name) throws IOException {
		// Remove at the server administration
		URL url = new URL("http://localhost:"+this.port+"/$/datasets/"+name);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("DELETE"); 
		connection.connect(); 
		connection.getInputStream().close(); // TODO: not sure why this line is needed to send the request
		// TODO: I do not know why Fuseki does not remove the dataset from the file system, 
		// even if the dataset is non-persistent
		
		// Remove the dataset configuration from the file system, otherwise Fuseki remakes it
		File file = new File(this.fusekiDirectory+"run/configuration/"+name+".ttl");
		file.delete(); 
		
		// Delete the permanent database files
		file = new File(this.fusekiDirectory+"run/databases/"+name+"/");
		if(file.exists()){
			Arrays.asList(file.listFiles()).forEach((File subfile)->{ subfile.delete();} );
			file.delete();
		}  
	}
	
	/** Submit a query to Fuseki and return the response as a JSON object. */
	public final JSONObject query(final String dataset, final String query) throws IOException{
		String queryPrefixes = this.prefixes.get(dataset);
		HttpURLConnection connection = postData("POST",
				"http://localhost:"+this.port+"/"+dataset+"/query", 
				"application/sparql-query", 
				queryPrefixes == null ? query : (queryPrefixes + query) );
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String responseJSON = "";
		String s = "";
		while((s = reader.readLine()) != null)
			responseJSON += s; 
		connection.disconnect();
		return new JSONObject(responseJSON);
	}
	
	/** Execute a SPARQL update query. */
	public final void update(final String dataset, final String update) throws IOException{
		String queryPrefixes = this.prefixes.get(dataset);
		HttpURLConnection connection = postData("POST",
				"http://localhost:"+this.port+"/"+dataset+"/update", 
				"application/sparql-update", 
				queryPrefixes == null ? update : (queryPrefixes + update) );
		connection.disconnect();
	}
	
	/** Load an ontology in the dataset.  */
	public final void loadOntology(final String dataset, final String pathToOntology) throws IOException{ 
		File ontologyFile = new File(pathToOntology);
		StringBuffer content = new StringBuffer();
		BufferedReader br = new BufferedReader(new FileReader(ontologyFile));
		String s = "";
		while((s = br.readLine()) != null) content.append(s).append("\r\n"); 
		HttpURLConnection connection = postData("PUT",
				"http://localhost:"+this.port+"/"+dataset+"/data", 
				"text/turtle", 
				content.toString()); 
		connection.disconnect();
		br.close(); 
	}
	
	/** Execute a SPARQL update query that consists of one added triple.  */
	public final void addTriple(final String dataset, final String source, final String relation, final String destiny) throws IOException{
		update(dataset, "INSERT DATA {"+source+" "+relation+" "+destiny+"}");
	}
	
	/** 
	 * Update the configuration file of a dataset to enable the standard Jena reasoner. 
	 * WARNING: This operation will restart Fuseki, all non-permanent datasets are wiped. 
	 * @throws IOException 
	 * @throws InterruptedException */
	public final void enableReasoner(final String dataset, boolean datasetIsPersistent) throws IOException, InterruptedException{
		BufferedReader br = new BufferedReader(new FileReader(
				datasetIsPersistent?
						(this.pathToConfigTemplatesDirectory+"persistentReasonerTemplate.ttl"):
						(this.pathToConfigTemplatesDirectory+"temporaryReasonerTemplate.ttl")));
		StringBuffer templateBuffer = new StringBuffer();
		String s = "";
		while((s = br.readLine()) != null) templateBuffer.append(s).append("\r\n");
		String template = templateBuffer.toString().replaceAll("DATASETNAME", dataset).replaceAll("FUSEKIDIRECTORY", this.fusekiDirectory+"run/databases/");
		File configuration = new File(this.fusekiDirectory+"run/configuration/"+dataset+".ttl");
		FileWriter writer = new FileWriter(configuration, false);
		writer.write(template);
		writer.flush();
		writer.close();  
		br.close();
		startServer();
	}
	
	// Aux method to create a POST connection. Does not disconnect the connection. You can still use a BufferedReader for instance 
	// to read the response. 
	private final HttpURLConnection postData(final String method, final String urlString, final String contentType, final String dataString) throws IOException{ 
		HttpURLConnection connection = (HttpURLConnection) (new URL(urlString)).openConnection();
		connection.setRequestMethod(method); 
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", contentType);
		byte[] data = dataString.getBytes(StandardCharsets.UTF_8);
		connection.setFixedLengthStreamingMode(data.length);
		connection.connect();
		try(OutputStream os = connection.getOutputStream()){
			os.write(data);
			os.flush();
			os.close();
		}  
		return connection;
	}
} 