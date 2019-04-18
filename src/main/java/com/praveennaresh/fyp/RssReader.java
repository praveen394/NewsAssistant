package com.praveennaresh.fyp;

import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Praveen Naresh
 * Created on 29-Dec-15
 * Read RSS data from URL
 */
public class RssReader {
	
	private String rssUrl;


	public RssReader(String rssUrl) {
		this.rssUrl = rssUrl;
	}


	public List<RssItem> getItems() throws Exception {
		// SAX parse RSS data
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		RssParseHandler handler = new RssParseHandler();
		
		saxParser.parse(rssUrl, handler);

		return handler.getItems();
		
	}

}
