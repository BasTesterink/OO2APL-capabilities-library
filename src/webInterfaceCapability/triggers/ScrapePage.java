package webInterfaceCapability.triggers;

import java.util.function.Function;

import oo2apl.agent.Trigger;
/**
 * Trigger that is used to trigger the agent into scraping a page and adopting the result as an internal trigger.
 * @author Bas Testerink
 */
public class ScrapePage implements Trigger {
	private final String url;
	private final Function<String, String> parser;
	
	public ScrapePage(final String url, final Function<String, String> parser){
		this.url = url;
		this.parser = parser;
	}
	
	public final String getURL(){ return this.url; }
	public final Function<String, String> getParser(){ return this.parser; }
}
