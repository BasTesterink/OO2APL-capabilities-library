package ivonaSpeechCapability;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File; 
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
 
import com.ivona.services.tts.IvonaSpeechCloudClient;
import com.ivona.services.tts.model.CreateSpeechRequest;
import com.ivona.services.tts.model.CreateSpeechResult;
import com.ivona.services.tts.model.Input;
import com.ivona.services.tts.model.Parameters;
import com.ivona.services.tts.model.Voice;

import oo2apl.agent.Context;

import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.PlugInManager;
import javax.media.format.AudioFormat;
/**
 * This context connects to the Ivona cloud so that it can speak out loud sentences. Sentences and their sound 
 * files are buffered in order to limit the amount of calls to the cloud. 
 * 
 * @author Bas Testerink
 *
 */
public final class IvonaContext implements Context {
	// Ivona data
	private IvonaSpeechCloudClient speechCloud;
	private Voice voice;
	private String speed = "slow";
	private final String pathToBuffer, pathToCredentialsFile;

	// Buffer
	private static final int MAX_BUFFER = 2000; // Max. # of files to store
	private LinkedList<String> requestOrder;
	private Map<Integer, String> stringPerHash;

	public IvonaContext(final String pathToBuffer, final String pathToCredentialsFile){
		this.pathToBuffer = pathToBuffer;
		this.pathToCredentialsFile = pathToCredentialsFile;
		try {
			// Buffer data
			this.requestOrder = new LinkedList<>(); 
			this.stringPerHash = new HashMap<>();
			readBuffer();
			
			// For playing files
			PlugInManager.addPlugIn(
				"com.sun.media.codec.audio.mp3.JavaDecoder",
				new Format[]{new AudioFormat(AudioFormat.MPEGLAYER3), new AudioFormat(AudioFormat.MPEG)},
				new Format[]{new AudioFormat(AudioFormat.LINEAR)},
				PlugInManager.CODEC
			);

			// Make the connection to the Ivona cloud service
			FileReader reader = new FileReader(this.pathToCredentialsFile);
			BufferedReader breader = new BufferedReader(reader);
			String accesskey = breader.readLine();
			String secretkey = breader.readLine();
			breader.close();
			reader.close(); 
			IvonaCredentials credentials = new IvonaCredentials(secretkey.substring(secretkey.indexOf('=')+2), accesskey.substring(accesskey.indexOf('=')+2));
			this.speechCloud = new IvonaSpeechCloudClient(credentials); 
			this.speechCloud.setEndpoint("https://tts.eu-west-1.ivonacloud.com");

			// Set the voice
			setVoiceToEnglish();
		} catch(Exception e){ 
			e.printStackTrace();
		}
	} 

	/** Get the file for a text. Will use the cloud of Ivona to construct the file if it is not buffered locally. 
	 * An agent can use the file to execute a different player than the built-in player. */
	public final String retrieveFile(final String text){
		// Check if in stringPerHash and see if it is a match
		String string = this.stringPerHash.get(text.hashCode());
		if(string != null && text.equals(string)){ // Same request as earlier
			// Ensure the request is at the top
			this.requestOrder.remove(string);
			this.requestOrder.addFirst(string); 
			// Return the file name
			return text.hashCode()+".mp3";
		} else if(string != null) { // There is another String with the same hash
			// Remove the impostor string
			this.requestOrder.remove(string);	
			File file = new File(this.pathToBuffer+text.hashCode()+".mp3");
			file.delete();		
		}
		
		// Add the new request to the list and make the file
		this.requestOrder.addFirst(text);
		this.stringPerHash.put(text.hashCode(), text);
		makeFile(text); 

		// Delete oldest request if the buffer is full
		if(this.requestOrder.size() > MAX_BUFFER){ 
			String fileName = this.requestOrder.removeLast();
			File file = new File(fileName);
			file.delete();
			this.stringPerHash.remove(fileName.hashCode());
		}
		saveBuffer();

		// Return the file name
		return text.hashCode()+".mp3"; 
	}

	/** For a string of text create the music file by calling upon the Ivona cloud service. */
	private final void makeFile(final String text){
		// Build the request
		Input input = new Input(); 
		input.setData(text);
		CreateSpeechRequest createSpeechRequest = new CreateSpeechRequest();
		createSpeechRequest.setInput(input);
		createSpeechRequest.setVoice(this.voice);
		Parameters p = new Parameters();
		p.setRate(this.speed);
		createSpeechRequest.setParameters(p); 
		InputStream inputStream = null;
		FileOutputStream outputStream = null;  
		try { 
			// Send the request
			CreateSpeechResult createSpeechResult = speechCloud.createSpeech(createSpeechRequest);   
			inputStream = createSpeechResult.getBody();
			// Obtain and write to file
			outputStream = new FileOutputStream(new File(this.pathToBuffer+text.hashCode()+".mp3")); 
			byte[] buffer = new byte[2 * 1024];
			int readBytes; 
			while ((readBytes = inputStream.read(buffer)) > 0) { 
				outputStream.write(buffer, 0, readBytes);
			}  
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				if(inputStream != null) inputStream.close();
				if(outputStream != null) outputStream.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	/** Speak the given text. */
	public final void speak(final String text){
		try{ 
			Player player = Manager.createPlayer(new MediaLocator(new File(this.pathToBuffer+retrieveFile(text)).toURI().toURL()));
			player.start(); 
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/** Save the buffer to a file. */
	private final void saveBuffer(){
		try{
			FileWriter writer = new FileWriter(new File(this.pathToBuffer+"buffer.txt"));
			BufferedWriter bwriter = new BufferedWriter(writer);
			Iterator<String> iterator = this.requestOrder.iterator();
			while(iterator.hasNext())
				bwriter.write(iterator.next()+"\r\n");
			bwriter.flush();
			bwriter.close();
			writer.close();
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/** Read the buffer file. */
	public final void readBuffer(){
		try{
			FileReader reader = new FileReader(new File(this.pathToBuffer+"buffer.txt"));
			BufferedReader breader = new BufferedReader(reader);
			String file = "";
			while((file = breader.readLine()) != null){
				this.requestOrder.addLast(file);
				this.stringPerHash.put(file.hashCode(), file);
			}
			breader.close();
			reader.close();
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/** Set the voice to Dutch. */
	public final void setVoiceToDutch(){
		this.voice = new Voice(); 
		this.voice.setLanguage("nl-NL");
		this.voice.setName("Ruben");
		this.speed = "slow";
	}

	/** Set the voice to English. */
	public final void setVoiceToEnglish(){
		this.voice = new Voice();
		this.voice.setLanguage("en-GB");
		this.voice.setName("Amy");
		this.speed = "medium";
	}
}
