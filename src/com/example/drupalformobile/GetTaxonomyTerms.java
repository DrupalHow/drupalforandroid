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
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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

public class GetTaxonomyTerms extends ListActivity {
	public static final String EXTRA_MESSAGE = "empty";
	private SimpleAdapter itemAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		String MyTid = intent.getStringExtra(GetNodeList.EXTRA_MESSAGE);
		
        // get settings and set domain
    	SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
    	String domain = DrupalForMobileSettings.getString("domain", "emptytext");
		
		
		setContentView(R.layout.activity_get_taxonomy_terms);

		Object ParamArray[] = {"http://"+domain+"/default/taxonomy_vocabulary/getTree.json", MyTid};  
		
		new GetTaxonomyTerm().execute(ParamArray); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_get_taxonomy_terms, menu);
		return true;
	}
	private class GetTaxonomyTerm extends AsyncTask<Object, Void, String> {
		@Override
		protected String doInBackground(Object... Params) {		
			StringBuilder tweetFeedBuilder = new StringBuilder();
			String Myurl = (String) Params[0]; // Set url 
			String Mytype= Params[1].toString(); // set parameter
			HttpResponse tweetResponse = null;
			HttpClient tweetClient = new DefaultHttpClient();
			try {
				HttpPost tweetGet = new HttpPost(Myurl);
				try {
			        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			        nameValuePairs.add(new BasicNameValuePair("vid", Mytype));
			        tweetGet.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			        // Execute HTTP Post Request
			        tweetResponse = tweetClient.execute(tweetGet);
				} catch(Exception e) {
					
				}
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
			return tweetFeedBuilder.toString();
		}
		protected void onPostExecute(String result) {
			List<Map<String, String>> itemArrey = new ArrayList<Map<String, String>>();
			
			itemArrey.clear();
			
			itemAdapter = new SimpleAdapter(GetTaxonomyTerms.this, itemArrey, 
					R.layout.list_view_taxonomyterms, 
					new String[] {"name", "description", "tid"},
					new int[] {R.id.taxonomytermtitle,
					R.id.taxonomytermdescription, R.id.taxonomytid}); 
			setListAdapter(itemAdapter);
			
			StringBuilder tweetResultBuilder = new StringBuilder();
			try {
				result = result.replace("[", "{'results':[");
				result = result.replace("]", "]}");		
				/*
				 * Fix for nullresults in taxonomy terms
				 */
				result = result.replace("null", "'null'");		
				
				// grab the data!
				JSONObject resultObject = new JSONObject(result);
				JSONArray tweetArray = resultObject.getJSONArray("results");
				for (int t=0; t<tweetArray.length(); t++) {
					JSONObject tweetObject = tweetArray.getJSONObject(t);
					tweetResultBuilder.append(tweetObject.get("name")+" - - - ");
					Map<String, String> itemArrey1 = new HashMap<String, String>();
					itemArrey1.put("name", (String) tweetObject.get("name"));
					if (tweetObject.get("description").toString().equals("null")){
						itemArrey1.put("description", (String) "No description available");
					}else{
						itemArrey1.put("description", (String) tweetObject.get("description"));
					}
					
					itemArrey1.put("tid", (String) tweetObject.get("tid"));
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
		
						
		Intent intent = new Intent(this, ShowTaxonomyTerm.class);
		TextView txt = (TextView) l.getChildAt(position).findViewById(R.id.taxonomytid);
		
		intent.putExtra(EXTRA_MESSAGE, txt.getText().toString());
				
		startActivity(intent);
	}
}
