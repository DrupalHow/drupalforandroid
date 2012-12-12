package com.example.drupalformobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class GetNodeList extends ListActivity {
	public static final String EXTRA_MESSAGE = "empty";
	private SimpleAdapter itemAdapter;
	HttpContext localContext = new BasicHttpContext();
	HttpClient tweetClient = new DefaultHttpClient();
	/*
	 * For now i dont have appwide sessions..
	 * That's why i have to login each time..
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // get settings and set domain
    	SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
    	String domain = DrupalForMobileSettings.getString("domain", "emptytext");
    	
        setContentView(R.layout.activity_do_login_and_save);
        // Call the function to get the nodes
        new GetTweets().execute("http://"+domain+"/default/node.json"); 
	}
	public StatusLine getLoginCookie(){
		SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
		String domain = DrupalForMobileSettings.getString("domain", "emptytext");
		String username = DrupalForMobileSettings.getString("username", "emptytext");
		String password = DrupalForMobileSettings.getString("password", "emptytext");
				
    	String Myurl = "http://"+domain+"/default/user/login.json";
    	
    	HttpPost GetLogin = new HttpPost(Myurl);
    	
        List<NameValuePair> LoginValuePairs = new ArrayList<NameValuePair>();
        LoginValuePairs.add(new BasicNameValuePair("username", username));
        LoginValuePairs.add(new BasicNameValuePair("password", password));
        LoginValuePairs.add(new BasicNameValuePair("action", "login"));
        try {
        	GetLogin.setEntity(new UrlEncodedFormEntity(LoginValuePairs, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        HttpResponse tweetResponse1 = null;
		try {
			
			tweetResponse1 = tweetClient.execute(GetLogin, localContext);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        StatusLine searchStatus = tweetResponse1.getStatusLine();
        return searchStatus;
	}
	private class GetTweets extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... twitterURL) {		
			if( getLoginCookie().getStatusCode() == 200 || getLoginCookie().getStatusCode() == 406 ){
				StringBuilder tweetFeedBuilder = new StringBuilder();
				for (String searchURL : twitterURL) {
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
			return "failed";
		}
		protected void onPostExecute(String result) {
			List<Map<String, String>> itemArrey = new ArrayList<Map<String, String>>();
			
			itemArrey.clear();
			
			itemAdapter = new SimpleAdapter(GetNodeList.this, itemArrey, 
					R.layout.list_view_nodes, 
					new String[] {"title", "nid", "type"},
					new int[] {R.id.title,
					R.id.nodeid, R.id.type}); 
			setListAdapter(itemAdapter);
			
			StringBuilder tweetResultBuilder = new StringBuilder();
			try {
				result = result.replace("[", "{'results':[");
				result = result.replace("]", "]}");
				JSONObject resultObject = new JSONObject(result);
				JSONArray tweetArray = resultObject.getJSONArray("results");
				
				for (int t=0; t<tweetArray.length(); t++) {
					JSONObject tweetObject = tweetArray.getJSONObject(t);
					tweetResultBuilder.append(tweetObject.get("title")+" - - - ");
					Map<String, String> itemArrey1 = new HashMap<String, String>();
					itemArrey1.put("title", (String) tweetObject.get("title"));
					itemArrey1.put("nid", ""+(String) tweetObject.get("nid"));
					itemArrey1.put("type", ""+(String) tweetObject.get("type"));
					itemArrey.add(itemArrey1);
				}
			}
			catch (Exception e) {
			    e.printStackTrace();
			}
			
			if(tweetResultBuilder.length()>0)
				itemAdapter.notifyDataSetChanged();
			else
				itemAdapter.notifyDataSetChanged();
		}
	}
	@Override 
	public void onListItemClick(ListView l, View v, int position, long id) {			
		Intent intent = new Intent(this, ShowNode.class);
		
		TextView txt = (TextView) l.getChildAt(position).findViewById(R.id.nodeid);
		
		intent.putExtra(EXTRA_MESSAGE, txt.getText().toString());
				
		startActivity(intent);
	}
}