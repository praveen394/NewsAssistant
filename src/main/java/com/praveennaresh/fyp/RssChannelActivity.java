package com.praveennaresh.fyp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Praveen Naresh
 * Created on 21-Dec-15
 * Activity for each tab
 */
public class RssChannelActivity extends Activity {

    private String API_KEY = "Gqsxoydhm5LT";

    private String startSearch = "https://uclassify.com/browse/mvazquez/news-classifier/ClassifyText?readkey="+API_KEY;
    private String endSearch = "&output=json&version=1.01&text=";
	
	// A reference to this activity
    private RssChannelActivity local;
    private String START_LINK = "https://news.google.com/news?cf=all&hl=en&pz=1&ned=us&q=";
    private String END_LINK = "&output=rss";

    private String search_query;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_rss_channel);
		
		// Get the RSS URL that was set in the main activity
		String rssUrl = (String)getIntent().getExtras().get("rss-url");
        String recUrl = (String)getIntent().getExtras().get("rec-url");

        if(rssUrl.startsWith("http"))
        {
            // Set reference to this activity
            local = this;

            GetRSSDataTask task = new GetRSSDataTask();

            // Start process RSS task
            task.execute(rssUrl);
        }
        else if(recUrl != null)
        {
            String link1 = generateUrl(rssUrl);
            String link2 = generateUrl(recUrl);

            local = this;

            GetRecommended task = new GetRecommended(link1,link2);

            task.execute();
        }
        else
        {

            String url = generateUrl(rssUrl);

            String finalLink = startSearch+endSearch+search_query;
            new UrlCategorization(getApplicationContext()).execute(finalLink);

            local = this;

            GetRSSSearchTask task = new GetRSSSearchTask(url);


            task.execute();
        }
	}
    //generate search url
    private String generateUrl(String rssUrl)
    {
        String[] query = rssUrl.split("\\s+");
        String link = "";
        for(int i=0; i<query.length; i++)
        {
            if(i == query.length-1)
            {
                link = link+query[i];
            }
            else
            {
                link = link+query[i]+"+";
            }
        }
        search_query = link;
        String url = START_LINK+link+END_LINK;
        return url;
    }


    private class GetRSSSearchTask extends AsyncTask<String,Void,List<RssItem>>{
        String link;

        public GetRSSSearchTask(String link){
            this.link = link;
        }
        @Override
        protected List<RssItem> doInBackground(String... urls) {
            try {
                // Create RSS reader
                RssSearchReader searchReader = new RssSearchReader();
                // Parse RSS, get items
                return searchReader.parse(getInputStream(link));

            } catch (Exception e) {
                Log.e("RssChannelActivity", e.getMessage());
            }

            return null;
        }

        public InputStream getInputStream(String link)
        {
            try{
                URL url = new URL(link);
                return url.openConnection().getInputStream();
            }
            catch (IOException e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<RssItem> result) {


            // Get a ListView from the RSS Channel view
            ListView itcItems = (ListView) findViewById(R.id.rssChannelListView);

            // Create a list adapter
            ArrayAdapter<RssItem> adapter = new ArrayAdapter<RssItem>
                    (local,android.R.layout.simple_list_item_1, result);
            // Set list adapter for the ListView
            itcItems.setAdapter(adapter);

            // Set list view item click listener
            itcItems.setOnItemClickListener(new ListListener(result, getApplicationContext(),local));
        }
    }

    //get recommended news
    private class GetRecommended extends AsyncTask<String,Void,List<RssItem>>{
        String link1;
        String link2;


        public GetRecommended(String link1, String link2){
            this.link1 = link1;
            this.link2 = link2;
        }
        @Override
        protected List<RssItem> doInBackground(String... urls) {
            try {
                List<RssItem> lista;
                List<RssItem> listb;
                List<RssItem> reader = new ArrayList<RssItem>();
                // Create RSS reader
                RssSearchReader searchReader = new RssSearchReader();
                // Parse RSS, get items
                lista = searchReader.parse(getInputStream(link1));
                listb = searchReader.parse(getInputStream(link2));

                reader.addAll(lista);
                reader.addAll(listb);

                return reader;

            } catch (Exception e) {
                Log.e("RssChannelActivity", e.getMessage());
            }

            return null;
        }

        public InputStream getInputStream(String link)
        {
            try{
                URL url = new URL(link);
                return url.openConnection().getInputStream();
            }
            catch (IOException e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<RssItem> result) {

            // Get a ListView from the RSS Channel view
            ListView itcItems = (ListView) findViewById(R.id.rssChannelListView);

            // Create a list adapter
            ArrayAdapter<RssItem> adapter = new ArrayAdapter<RssItem>
                    (local,android.R.layout.simple_list_item_1, result);
            // Set list adapter for the ListView
            itcItems.setAdapter(adapter);

            // Set list view item click listener
            itcItems.setOnItemClickListener(new ListListener(result, getApplicationContext(),local));
        }
    }


	private class GetRSSDataTask extends AsyncTask<String, Void, List<RssItem> > {

        @Override
        protected List<RssItem> doInBackground(String... urls) {
            try {
                // Create RSS reader
                RssReader rssReader = new RssReader(urls[0]);
             
                // Parse RSS, get items
                return rssReader.getItems();
             
            } catch (Exception e) {
                Log.e("RssChannelActivity", e.getMessage());
            }
             
            return null;
        }
         
        @Override
        protected void onPostExecute(List<RssItem> result) {
            if(result == null || result.size() < 1 || result.isEmpty())
            {
                String message = "Error retrieving news from daily mirror!";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
            else
            {
                // Get a ListView from the RSS Channel view
                ListView itcItems = (ListView) findViewById(R.id.rssChannelListView);

                // Create a list adapter
                ArrayAdapter<RssItem> adapter = new ArrayAdapter<RssItem>
                        (local,android.R.layout.simple_list_item_1, result);
                // Set list adapter for the ListView
                itcItems.setAdapter(adapter);

                // Set list view item click listener
                itcItems.setOnItemClickListener(new ListListener(result, getApplicationContext(),local));
            }
        }
    }
	
}
