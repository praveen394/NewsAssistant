package com.praveennaresh.fyp;

/**
 * Praveen Naresh
 * Created on 22-Dec-15
 * RssItem Model
 */
public class RssItem {
	
	// item title
	private String title;
	// item link
	private String link;

	public RssItem(){}


	public RssItem(String title, String link){
		this.title = title;
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	@Override
	public String toString() {
		return title;
	}
	
}
