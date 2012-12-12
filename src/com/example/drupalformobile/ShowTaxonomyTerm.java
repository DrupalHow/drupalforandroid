package com.example.drupalformobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ShowTaxonomyTerm extends Activity {
	HttpContext localContext = new BasicHttpContext();
	HttpClient tweetClient = new DefaultHttpClient();
	/*
	 * For now i dont have appwide sessions..
	 * That's why i have to login each time..
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		
		String gettermid = intent.getStringExtra(GetNodeList.EXTRA_MESSAGE);
		
    	SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
    	String domain = DrupalForMobileSettings.getString("domain", "emptytext");
		
		setContentView(R.layout.activity_show_taxonomy_term);
		
		new GetNodes().execute("http://"+domain+"/default/taxonomy_term.json?parameters[tid]="+gettermid); 
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
	public void saveTaxonomyTerm(View view) throws ClientProtocolException, IOException {
        // 200 = ok 406 = allreadyloggedin
        if( getLoginCookie().getStatusCode() == 200 || getLoginCookie().getStatusCode() == 406 ){
        	HttpResponse tweetResponse = null;
        	
        	EditText termnamefield = (EditText) findViewById(R.id.termname);
        	TextView termvid = (TextView) findViewById(R.id.termvid);
        	EditText termdescriptionfield = (EditText) findViewById(R.id.taxonomydescription);
        	
    		Intent intent = getIntent();
    		
    		String gettermid = intent.getStringExtra(GetNodeList.EXTRA_MESSAGE);
    		SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
        	String MyUpdateUrl = "http://"+DrupalForMobileSettings.getString("domain", "emptytext")+"/default/taxonomy_term/"+gettermid;
        	
        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        	nameValuePairs.add(new BasicNameValuePair("name", termnamefield.getText().toString()));
        	nameValuePairs.add(new BasicNameValuePair("vid", termvid.getText().toString()));
        	nameValuePairs.add(new BasicNameValuePair("description", termdescriptionfield.getText().toString()));
        	
        	HttpPut tweetGet = new HttpPut(MyUpdateUrl);
        	
        	tweetGet.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        	
        	tweetResponse = tweetClient.execute(tweetGet, localContext);
        	StatusLine searchStatus1 = tweetResponse.getStatusLine();
        	if ( searchStatus1.getStatusCode() == 200) {
        		Toast.makeText(this, "Update complete", Toast.LENGTH_LONG).show();
        	}else{
        		Toast.makeText(this, "Update failed. Please try again or report error", Toast.LENGTH_LONG).show();
        	}
        }
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_show_taxonomy_term, menu);
		return true;
	}
	private class GetNodes extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... twitterURL) {		
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
		protected void onPostExecute(String result) {

			TextView termname = (TextView) findViewById( R.id.termname );
			TextView termvid = (TextView) findViewById( R.id.termvid );
			TextView taxonomydescription = (TextView) findViewById( R.id.taxonomydescription );

			StringBuilder tweetResultBuilder = new StringBuilder();
			try {
				result = result.replace("[", "{'results':[");
				result = result.replace("]", "]}");
				result = result.replace("null", "'null'");	
				JSONObject resultObject = new JSONObject(result);
				JSONArray tweetArray = resultObject.getJSONArray("results");
				
				for (int t=0; t<tweetArray.length(); t++) {
					JSONObject tweetObject = tweetArray.getJSONObject(t);
					tweetResultBuilder.append(tweetObject.get("name")+" - - - ");
					
					termname.setText((String) tweetObject.get("name"));
					if(tweetObject.get("description").equals("null")){
						taxonomydescription.setText("");			
					}else{
						taxonomydescription.setText((String) tweetObject.get("description"));			
					}
							
					
					termvid.setText((String) tweetObject.get("vid"));
				}
			}
			catch (Exception e) {
			    e.printStackTrace();
			}
			
			if(tweetResultBuilder.length()<0){
				termname.setText((String) "Something went wrong :(");
			}else{
			}
		}
	}
}
