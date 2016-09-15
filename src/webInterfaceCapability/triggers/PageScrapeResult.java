package webInterfaceCapability.triggers;

import oo2apl.agent.Trigger;
/**
 * Trigger that is used to trigger any schemes that were waiting on the result of a page scrape.
 * @author Bas Testerink
 */
public final class PageScrapeResult implements Trigger {
	private final boolean succeeded;
	private final String url, result;
	
	public PageScrapeResult(final boolean succeeded, final String url, final String result){
		this.succeeded = succeeded;
		this.url = url;
		this.result = result;
	}
	

	public final String getURL(){ return this.url; }
	public final String getResult(){ return this.result; }
	public final boolean isSucceeded(){ return this.succeeded; }
}
