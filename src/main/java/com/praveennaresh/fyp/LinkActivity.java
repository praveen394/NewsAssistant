package com.praveennaresh.fyp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Praveen Naresh
 * Created on 23-Feb-16
 * Activity which performs the saved links and its options
 */
public class LinkActivity extends Activity {
    private ListView listView;
    private Button home;
    private TextToSpeech speech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        listView = (ListView)findViewById(R.id.linkListView);
        home = (Button)findViewById(R.id.btnBack);

        //initialize speech to text
        speech = new TextToSpeech(LinkActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    speech.setLanguage(Locale.US);
                }
            }
        });

        //get saved links
        DBHelper helper = new DBHelper(this);
        final ArrayList<SavedLinks> arrayList = helper.getSavedLinks();
        //add to the listview
        ArrayAdapter<SavedLinks> adapter = new ArrayAdapter<SavedLinks>(
                this,android.R.layout.simple_list_item_1,arrayList);

        listView.setAdapter(adapter);
        //news options for saved links
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int linkId = arrayList.get(position).getId();
                final Uri url = Uri.parse(arrayList.get(position).getLink());
                final String title = arrayList.get(position).getTitle().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(LinkActivity.this);
                builder.setTitle("Choose action");
                builder.setItems(new CharSequence[]
                                {"Share", "Delete", "View", "Listen"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0://share option
                                        Intent share = new Intent(Intent.ACTION_SEND);
                                        share.setType("text/plain");
                                        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                        share.putExtra(Intent.EXTRA_SUBJECT, "News Link");
                                        share.putExtra(Intent.EXTRA_TEXT, url.toString());
                                        LinkActivity.this.startActivity(Intent.createChooser(share, "Share via"));
                                        break;
                                    case 1://delete option
                                        AlertDialog.Builder del = new AlertDialog.Builder(LinkActivity.this);
                                        del.setMessage("Are you sure you want to delete?");
                                        del.setCancelable(false);
                                        del.setPositiveButton(
                                                "Delete",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        DBHelper helper = new DBHelper(LinkActivity.this);
                                                        helper.deleteLink(linkId);
                                                        Intent i = new Intent(LinkActivity.this, LinkActivity.class);
                                                        startActivity(i);
                                                        Toast.makeText(LinkActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                        );
                                        del.setNegativeButton(
                                                "Cancel",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }
                                        );
                                        del.create().show();
                                        break;
                                    case 2://view option
                                        Intent browser = new Intent(Intent.ACTION_VIEW);
                                        browser.setData(url);
                                        LinkActivity.this.startActivity(browser);
                                        break;
                                    case 3://listen option
                                        speakout(title);
                                        break;
                                }
                            }
                        }
                );
                builder.create().show();
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(LinkActivity.this,MainActivity.class);
                startActivity(home);
                finish();
            }
        });

    }

    private void speakout(final String text)
    {
        speech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void onPause()
    {
        if(speech != null)
        {
            speech.stop();
            speech.shutdown();
        }
        super.onPause();
    }

    public void onBackPressed ()
    {
        Intent back = new Intent(LinkActivity.this,MainActivity.class);
        startActivity(back);
        finish();
    }
}
