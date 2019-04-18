package com.praveennaresh.fyp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

/**
 * Praveen Naresh
 * Created on 22-Feb-16
 * Activity which performs list listener item click events
 * and news options (share, save, view, listen)
 */
public class ListListener extends Activity implements OnItemClickListener {


    private String API_KEY = "Gqsxoydhm5LT";

    private String startURL = "https://uclassify.com/browse/mvazquez/news-classifier/ClassifyUrl?readkey="+API_KEY;
    private String endURL = "&output=json&removeHtml=true&version=1.01&url=";

    private TextToSpeech textToSpeech;
	// List item's reference
	List<RssItem> listItems;
	// Calling activity reference
	Context activity;

    Activity base;

	public ListListener(List<RssItem> aListItems, Context anActivity,Activity aBase) {
		listItems = aListItems;
		activity  = anActivity;
        base = aBase;

        textToSpeech = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == textToSpeech.SUCCESS)
                {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

	}

    //listview item click listener
	public void onItemClick(AdapterView<?> parent, View view, final int pos, long id) {
        final Uri url = Uri.parse(listItems.get(pos).getLink());
        final String title = listItems.get(pos).getTitle().toString();
        final String finalurl = startURL+endURL+url.toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(base);
        builder.setTitle("Choose action");
        builder.setItems(new CharSequence[]
                        {"Share", "Save", "View", "Listen"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0://share
                                Intent share = new Intent(Intent.ACTION_SEND);
                                share.setType("text/plain");
                                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                share.putExtra(Intent.EXTRA_SUBJECT, "News Link");
                                share.putExtra(Intent.EXTRA_TEXT, url.toString());
                                base.startActivity(Intent.createChooser(share, "Share via"));
                                break;
                            case 1://save
                                DBHelper helper = new DBHelper(activity);
                                String editedTitle = editText(title);
                                String insert = helper.insertLink(editedTitle, url.toString());
                                if (insert.matches("failed")) {
                                    Toast.makeText(activity, "Link already saved", Toast.LENGTH_LONG).show();
                                } else {//get categorization
                                    new UrlCategorization(activity).execute(finalurl);
                                    Toast.makeText(activity, "Link Saved", Toast.LENGTH_LONG).show();
                                }
                                break;
                            case 2://view
                                new UrlCategorization(activity).execute(finalurl); //get categorization
                                Intent browser = new Intent(Intent.ACTION_VIEW);
                                browser.setData(url);
                                browser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(browser);
                                break;
                            case 3://listen
                                speakout(url.toString());
                                break;
                        }
                    }
                }
        );
        builder.create().show();
	}

    private String editText(String title)
    {
        String result = title.replace("'","");
        return result;
    }

    private void speakout(final String text)
    {
        new HttpAsyncTask().execute(text);
    }

    public void onPause()
    {
        if(textToSpeech != null)
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
    }

    //open inputstream to get content from web
    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }
    //get the news content from the website and play audio to user
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        //using jsoup to get the news description
        org.jsoup.nodes.Document doc = Jsoup.parse(result);
        Element link = doc.select("meta[property=og:description]").first();
        if(link != null)
        {
            result = link.attr("content");
        }
        inputStream.close();
        return result;
    }

    //async task to listen to the news
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog = new ProgressDialog(base);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if(dialog.isShowing())
            {
                dialog.dismiss();
            }
            textToSpeech.speak(result, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
