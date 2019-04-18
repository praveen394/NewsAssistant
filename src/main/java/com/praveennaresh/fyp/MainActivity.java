package com.praveennaresh.fyp;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main Activity
 * Praveen Naresh
 * Created on 20-Dec-15
 * Main activity class
 */
@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {

    //classifier api key
    private String API_KEY = "Gqsxoydhm5LT";

    private String startSearch = "https://uclassify.com/browse/mvazquez/news-classifier/ClassifyText?readkey="+API_KEY;
    private String endSearch = "&output=json&version=1.01&text=";


    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private Button search;
    private EditText query;
    private TextView recom;



    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_rss_tabs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case R.id.menu_settings://settings
                startActivity(new Intent(this, AppPreferences.class));
                return true;
            case R.id.menu_help://manual
                startActivity(new Intent(this,Help.class));
                return true;
            case R.id.menu_saved://saved links
                Intent savedLinks = new Intent(MainActivity.this,LinkActivity.class);
                startActivity(savedLinks);
                finish();
                return true;
            case R.id.menu_home://refresh
                Intent i = new Intent(MainActivity.this,MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(i);
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load facebook sdk
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        //set view
        setContentView(R.layout.activity_rss_tabs);

        //get tab layout
        TabHost tabHost = getTabHost();

        //initialize button & textview
        search = (Button) findViewById(R.id.btnSearch);
        query = (EditText) findViewById(R.id.txtSearchQuery);
        recom = (TextView)findViewById(R.id.txtRecommend);

        //initialize facebook login button
        loginButton = (LoginButton) findViewById(R.id.login_button);
        //set permissions
        loginButton.setReadPermissions("user_posts");
        //facebook login

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if(info != null && info.isConnected())
        {
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    try {
                                        String name = "Hi, " + object.getString("name");
                                        Toast.makeText(getApplicationContext(), name, Toast.LENGTH_LONG).show();

                                        GraphRequest request = GraphRequest.newGraphPathRequest(
                                                AccessToken.getCurrentAccessToken(),
                                                "/me/feed",
                                                new GraphRequest.Callback() {
                                                    @Override
                                                    public void onCompleted(GraphResponse response) {
                                                        try {
                                                            //get user content from fb and categorize them
                                                            JSONArray array = response.getJSONObject().getJSONArray("data");
                                                            List<String> fbposts = new ArrayList<String>();
                                                            for (int i = 1; i < array.length(); i++) {
                                                                if (array.getJSONObject(i).has("message")) {
                                                                    String message = array.getJSONObject(i).getString("message");
                                                                    message = message.replaceAll("[^A-Za-z0-9()\\[\\]]", "");
                                                                    String uri = startSearch+endSearch+message;
                                                                    fbposts.add(uri);
                                                                } else {
                                                                    continue;
                                                                }
                                                            }
                                                            if(fbposts.size() > 0)
                                                            {
                                                                ReadFb(fbposts);
                                                            }

                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });

                                        Bundle parameters = new Bundle();
                                        parameters.putString("fields", "message");
                                        parameters.putString("limit", "15");
                                        request.setParameters(parameters);
                                        request.executeAsync();


                                    } catch (JSONException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,gender, birthday");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                    Toast.makeText(getApplicationContext(), "Login Attempt Cancelled", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(getApplicationContext(), "Login Attempt Failed", Toast.LENGTH_LONG).show();
                }
            });


            //search button onClick event
            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String search_query = query.getText().toString();
                    if (TextUtils.isEmpty(search_query)) {
                        query.setError("Please enter search criteria");
                        return;
                    } else {
                        View view = MainActivity.this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        DBHelper helper = new DBHelper(getApplicationContext());
                        helper.insertSearchResult(search_query);
                        SearchNews(search_query);
                    }
                }
            });


            /*********************
             * Recommendation Tab *
             *********************/
            DBHelper helper = new DBHelper(getApplicationContext());
            String recText[] = helper.getLastSearchResult();
            if(recText != null)
            {
                recom.setText("Based on your trends: " + recText[0] + " & " + recText[1]);
                Intent rec = new Intent().setClass(this, RssChannelActivity.class);
                rec.putExtra("rss-url", recText[0]);
                rec.putExtra("rec-url",recText[1]);
                tabHost.clearAllTabs();
                String tabName = "Recommended for you";
                TabSpec tSpec = tabHost.newTabSpec(tabName)
                        .setIndicator(tabName)
                        .setContent(rec);
                tabHost.addTab(tSpec);
            }
            tabHost.setCurrentTab(2);


            /*********************
             * Breaking News Tab *
             *********************/
            Intent artIntent = new Intent().setClass(this, RssChannelActivity.class);
            artIntent.putExtra("rss-url", "http://www.dailymirror.lk/RSS_Feeds/breaking-news");
            //http://newsfirst.lk/english/feed
            String artTabName = getResources().getString(R.string.tab_breaking);
            TabSpec artTabSpec = tabHost.newTabSpec(artTabName)
                    .setIndicator(artTabName)
                    .setContent(artIntent);
            tabHost.addTab(artTabSpec);

            tabHost.setCurrentTab(1);
            //Load other tabs based on settings
            LoadNews();
        }
        else
        {
            Toast.makeText(this,"No internet connectivity. Please use WiFi/Mobile Data.",Toast.LENGTH_SHORT).show();
        }

    }

    //get the search query and load tab
    private void SearchNews(String query)
    {
        this.query.setText("");
        Intent i = new Intent().setClass(this, RssChannelActivity.class);
        i.putExtra("rss-url", query);
        TabHost tabHost = getTabHost();
        tabHost.clearAllTabs();
        String tabName = "Search Results for "+query;
        TabSpec tSpec = tabHost.newTabSpec(tabName)
                .setIndicator(tabName)
                .setContent(i);
            tabHost.addTab(tSpec);
    }
    //categorize facebook content
    private void ReadFb(List<String> fbPosts)
    {
        for(int i=0; i<fbPosts.size(); i++)
        {
            new UrlCategorization(getApplicationContext()).execute(fbPosts.get(i).toString());
        }
    }
    //load other tabs based on settings
    private void LoadNews()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Set<String> selections = preferences.getStringSet("newsSettings", new HashSet<String>());
        String[] selected= selections.toArray(new String[]{});
        TabHost tabHost = getTabHost();

        for (int i = 0; i < selected.length ; i++){

            /*********************
             * World News Tab *
             *********************/
            if(selected[i].matches("1")){
                Intent World = new Intent().setClass(this, RssChannelActivity.class);
                World.putExtra("rss-url", "http://www.dailymirror.lk/RSS_Feeds/world-news-main");

                String WorldName = getResources().getString(R.string.tab_world);
                TabSpec WorldSpec = tabHost.newTabSpec(WorldName)
                        .setIndicator(WorldName)
                        .setContent(World);
                tabHost.addTab(WorldSpec);
            }

            /*********************
             * Sports News Tab *
             *********************/
            else if(selected[i].matches("2")) {
                Intent sportsIntent = new Intent().setClass(this, RssChannelActivity.class);
                sportsIntent.putExtra("rss-url", "http://content.sports.dailymirror.lk/feed/");

                String sportsTabName = getResources().getString(R.string.tab_sports);
                TabSpec sportsTabSpec = tabHost.newTabSpec(sportsTabName)
                        .setIndicator(sportsTabName)
                        .setContent(sportsIntent);
                tabHost.addTab(sportsTabSpec);
            }

            /*********************
             * Technology News Tab *
             *********************/
            else if(selected[i].matches("3")) {
                Intent tIntent = new Intent().setClass(this, RssChannelActivity.class);
                tIntent.putExtra("rss-url", "http://www.dailymirror.lk/RSS_Feeds/technology");

                String tName = getResources().getString(R.string.tab_tech);
                TabSpec tSpec = tabHost.newTabSpec(tName)
                        .setIndicator(tName)
                        .setContent(tIntent);
                tabHost.addTab(tSpec);
            }

            /*********************
             * Business News Tab *
             *********************/
            else if(selected[i].matches("4")) {
                Intent Business = new Intent().setClass(this, RssChannelActivity.class);
                Business.putExtra("rss-url", "http://www.dailymirror.lk/RSS_Feeds/business-main");

                String BName = getResources().getString(R.string.tab_business);
                TabSpec BSpec = tabHost.newTabSpec(BName)
                        .setIndicator(BName)
                        .setContent(Business);
                tabHost.addTab(BSpec);
            }

            /*********************
             * Travel News Tab *
             *********************/
            else if(selected[i].matches("5")) {
                Intent Travel = new Intent().setClass(this, RssChannelActivity.class);
                Travel.putExtra("rss-url", "http://www.dailymirror.lk/RSS_Feeds/travel-main");

                String TravelName = getResources().getString(R.string.tab_travel);
                TabSpec TrSpec = tabHost.newTabSpec(TravelName)
                        .setIndicator(TravelName)
                        .setContent(Travel);
                tabHost.addTab(TrSpec);
            }

            /*********************
             * Video News Tab *
             *********************/
            else if(selected[i].matches("6")) {
                Intent video = new Intent().setClass(this, RssChannelActivity.class);
                video.putExtra("rss-url", "http://www.dailymirror.lk/RSS_Feeds/videos");

                String VideoName = getResources().getString(R.string.tab_video);
                TabSpec Vspec = tabHost.newTabSpec(VideoName)
                        .setIndicator(VideoName)
                        .setContent(video);
                tabHost.addTab(Vspec);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed ()
    {
        Intent back = new Intent(MainActivity.this,MainActivity.class);
        startActivity(back);
        finish();
    }
}
