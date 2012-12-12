package com.example.drupalformobile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class GetTaxonomyList extends ListActivity {
	public static final String EXTRA_MESSAGE = "empty";
	private SimpleAdapter itemAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // get settings and set domain
    	SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
    	String domain = DrupalForMobileSettings.getString("domain", "emptytext");
		
		setContentView(R.layout.activity_get_taxonomy_list);

		// Call the function to get the Taxonomy terms
		new GetTaxonomy().execute("http://"+domain+"/default/taxonomy_vocabulary.json"); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_get_taxonomy_list, menu);
		return true;
	}
	private class GetTaxonomy extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... twitterURL) {		
			StringBuilder tweetFeedBuilder = new StringBuilder();
			for (String searchURL : twitterURL) {
				HttpClient tweetClient = new DefaultHttpClient();
				try {
					HttpGet tweetGet = new HttpGet(searchURL);
					HttpResponse tweetResponse = tweetClient.execute(tweetGet);
					StatusLine searchStatus = tweetResponse.getStatusLine();
					if (searchStatus.getStatusCode() == 200) {
						HttpEntity tweetEntity = tweetResponse.getEntity();
						InputStream tweetContent = tweetEntity.getContent();
						InputStreamReader tweetInput = new InputStreamReader(tweetContent);
						BufferedReader tweetReader = new BufferedReader(tweetInput);
						String lineIn;
						while ((lineIn = tweetReader.readLine()) != null) {
						    tweetFeedBuilder.append(lineIn);
						}
						return tweetFeedBuilder.toString();
					}
					else{
					}
				}
				catch(Exception e) {
				    e.printStackTrace();
				}
			}
			return tweetFeedBuilder.toString();
		}
		protected void onPostExecute(String result) {
			List<Map<String, String>> itemArrey = new ArrayList<Map<String, String>>();
			
			itemArrey.clear();
			
			itemAdapter = new SimpleAdapter(GetTaxonomyList.this, itemArrey, 
					R.layout.list_view_taxonomygroups, 
					new String[] {"name", "description", "vid"},
					new int[] {R.id.taxonomytitle,
					R.id.taxonomydescription, R.id.taxonomyvid}); 
			setListAdapter(itemAdapter);
			
			StringBuilder tweetResultBuilder = new StringBuilder();
			try {
				result = result.replace("[", "{'results2':[");
				result = result.replace("]", "]}");
				
				JSONObject resultObject = new JSONObject(result);
				JSONArray tweetArray = resultObject.getJSONArray("results2");
				for (int t=0; t<tweetArray.length(); t++) {
					JSONObject tweetObject = tweetArray.getJSONObject(t);
					tweetResultBuilder.append(tweetObject.get("name")+" - - - ");
					Map<String, String> itemArrey1 = new HashMap<String, String>();
					itemArrey1.put("name", (String) tweetObject.get("name"));					
					if (tweetObject.get("description").toString().equals("") || tweetObject.get("description").toString() == null){
						itemArrey1.put("description", (String) "No description available");
					}else{
						itemArrey1.put("description", (String) tweetObject.get("description"));
					}
					
					itemArrey1.put("vid", (String) tweetObject.get("vid"));
					itemArrey.add(itemArrey1);
				}
				
			}
			catch (Exception e) {
			    e.printStackTrace();
			}
			
			if(tweetResultBuilder.length()>0){
				itemAdapter.notifyDataSetChanged();
			}else{
				itemAdapter.notifyDataSetChanged();
			}
		}
	}
	@Override 
	public void onListItemClick(ListView l, View v, int position, long id) {
		
						
		Intent intent = new Intent(this, GetTaxonomyTerms.class);
		TextView txt = (TextView) l.getChildAt(position).findViewById(R.id.taxonomyvid);
		
		intent.putExtra(EXTRA_MESSAGE, txt.getText().toString());
				
		startActivity(intent);
	}
}
