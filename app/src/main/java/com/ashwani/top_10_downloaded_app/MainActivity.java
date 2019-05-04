package com.ashwani.top_10_downloaded_app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps= findViewById(R.id.xmlListView);
        downloadUrl("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        String feedUrl;

        switch (id){
            case R.id.mnuFree:
                feedUrl="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml";
                break;
            case R.id.mnuPaid:
                feedUrl="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=10/xml";
                break;
            case R.id.mnuSongs:
                feedUrl="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=10/xml";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        downloadUrl(feedUrl);
        return true;
    }


    private void downloadUrl(String feedUrl){
        Log.d(TAG, "downloadUrl: starting AsyncTask");
        DownloadData downloadData=new DownloadData();
        downloadData.execute(feedUrl);
        Log.d(TAG, "downloadUrl: Done");

    }


    private class DownloadData extends AsyncTask<String,Void,String>{
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is "+s);
            ParseApplications parseApplications=new ParseApplications();
            parseApplications.parse(s);

//            ArrayAdapter<FeedEntry> arrayAdapter=new ArrayAdapter<FeedEntry>(
//                    MainActivity.this, R.layout.list_item, parseApplications.getApplications());
//            listApps.setAdapter(arrayAdapter);
            FeedAdapter feedAdapter=new FeedAdapter(MainActivity.this, R.layout.list_record,
                    parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with "+strings[0]);
            String rssFeed = downloadXML(strings[0]);
            if (rssFeed==null){
                Log.e(TAG, "doInBackground: Error Downloading");
            }
            return rssFeed;
        }

        private String downloadXML(String urlPath){
            StringBuilder xmlResult= new StringBuilder();

            try{
                URL url=new URL(urlPath);
                HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                int response=connection.getResponseCode();
                Log.d(TAG, "downloadXML: The Response code was " + response);
//                InputStream inputStream=connection.getInputStream();
//                InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
//                BufferedReader reader=new BufferedReader(inputStreamReader);
                BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charRead;
                char[] inputBuffer=new char[500];

                while (true){
                    charRead=reader.read(inputBuffer);
                    if(charRead<0){
                        break;
                    }
                    if(charRead>0){
                        xmlResult.append(String.copyValueOf(inputBuffer,0,charRead));
                    }
                }
                reader.close();

                return xmlResult.toString();
            }catch (MalformedURLException e){
                Log.e(TAG, "downloadXML: Invalid URL "+ e.getMessage());
            }catch (IOException e){
                Log.e(TAG, "downloadXML: IO Exception reading data "+ e.getMessage());
            } catch (SecurityException e){
                Log.e(TAG, "downloadXML: Security Exception. Need Internet Permission " + e.getMessage());
            }
            return null;
        }
    }
}








