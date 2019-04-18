package com.praveennaresh.fyp;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Praveen Naresh
 * Created by Praveen on 08-Mar-16
 * Categorization and recommendation is performed through this class
 */
public class UrlCategorization extends AsyncTask<String,String,String>{
    Context ctx;

    public UrlCategorization(Context ctx)
    {
        this.ctx = ctx;
    }

    //call the classifier
    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }
        return responseString;
    }


    @Override
    protected void onPostExecute(String s) {
        try
        {
            JSONObject jsonArray = new JSONObject(s);
            JSONObject obj = jsonArray.getJSONObject("cls1");
            //get classifier json objects
            float business = Float.parseFloat(obj.getString("Business"));
            float entertainment = Float.parseFloat(obj.getString("Entertainment"));
            float fashion = Float.parseFloat(obj.getString("Fashion"));
            float finance = Float.parseFloat(obj.getString("Finance"));
            float food = Float.parseFloat(obj.getString("Food"));
            float global = Float.parseFloat(obj.getString("Global"));
            float health = Float.parseFloat(obj.getString("Health"));
            float home = Float.parseFloat(obj.getString("Home"));
            float men = Float.parseFloat(obj.getString("Men"));
            float parents = Float.parseFloat(obj.getString("Parents"));
            float sports = Float.parseFloat(obj.getString("Sports"));
            float technology = Float.parseFloat(obj.getString("Technology"));
            float us = Float.parseFloat(obj.getString("US"));
            float women = Float.parseFloat(obj.getString("Women"));
            //add to list of categories
            List<Float> list = new ArrayList<Float>();
            list.add(business);
            list.add(entertainment);
            list.add(fashion);
            list.add(finance);
            list.add(food);
            list.add(global);
            list.add(health);
            list.add(home);
            list.add(men);
            list.add(parents);
            list.add(sports);
            list.add(technology);
            list.add(us);
            list.add(women);
            //sort and get the highest match for the category
            float MAX = list.get(0);
            int counter = 0;

            for(int i=0; i<list.size(); i++)
            {
                if(MAX < list.get(i))
                {
                    if(list.get(i).toString().matches("E"))
                    {
                        continue;
                    }
                    else
                    {
                        MAX = list.get(i);
                        counter = i;
                    }
                }
            }
            //increment the score of the category by 1
            String category = Category.values()[counter].toString();
            DBHelper helper = new DBHelper(ctx);
            helper.addRecommendation(category);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}
