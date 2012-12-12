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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class DrupalSiteSettings extends Activity {
	HttpContext localContext = new BasicHttpContext();
	HttpClient tweetClient = new DefaultHttpClient();
	/*
	 * For now i dont have appwide sessions..
	 * That's why i have to login each time..
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_drupal_site_settings);		
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> SpinnerAdapter = ArrayAdapter.createFromResource(this,
		        R.array.node_amounts_frontpage, android.R.layout.simple_spinner_item);
		SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(SpinnerAdapter);
		
		new GetNodes().execute("nothingactually");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_drupal_site_settings, menu);
		return true;
	}
	private class GetNodes extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... twitterURL) {
			return "yup";
		}
		protected void onPostExecute(String result) {
			if( getLoginCookie().getStatusCode() == 200 || getLoginCookie().getStatusCode() == 406 ){
			
				// Build types array
			    String [] aStopLightColors;  
			    aStopLightColors = new String[7];  
			    aStopLightColors[0] = new String("site_name");
			    aStopLightColors[1] = new String("site_slogan");  
			    aStopLightColors[2] = new String("site_mail");  
			    aStopLightColors[3] = new String("site_frontpage");  
			    aStopLightColors[4] = new String("site_403");  
			    aStopLightColors[5] = new String("site_404"); 
			    aStopLightColors[6] = new String("default_nodes_main");  
			    
			    StringBuilder tweetFeedBuilder = null;
			    
	    		SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
	        	String MyGetStringUrl = "http://"+DrupalForMobileSettings.getString("domain", "emptytext")+"/default/system/get_variable.json";
	        	HttpPost GetVariable = new HttpPost(MyGetStringUrl);
				for ( int t=0; t<aStopLightColors.length; t++ ){
					String sendstring = aStopLightColors[t];
					String myresult = null;
					List<NameValuePair> LoginValuePairs = new ArrayList<NameValuePair>();
					LoginValuePairs.add(new BasicNameValuePair("name", sendstring));
					try {
						GetVariable.setEntity(new UrlEncodedFormEntity(LoginValuePairs, HTTP.UTF_8));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					HttpResponse tweetResponse1 = null;
					try {
						tweetFeedBuilder = new StringBuilder();
						tweetResponse1 = tweetClient.execute(GetVariable, localContext);
						HttpEntity tweetEntity = tweetResponse1.getEntity();
						InputStream tweetContent = tweetEntity.getContent();
						InputStreamReader tweetInput = new InputStreamReader(tweetContent);
						BufferedReader tweetReader = new BufferedReader(tweetInput);
						String lineIn;
						while ((lineIn = tweetReader.readLine()) != null) {
						    tweetFeedBuilder.append(lineIn);
						}
						myresult = tweetFeedBuilder.toString();
						myresult = myresult.replace("\"", "");
						myresult = myresult.replace("[", "");
						myresult = myresult.replace("]", "");
						if(sendstring.toString().equals("site_name")){
							EditText settings_site_name = (EditText) findViewById(R.id.settings_sitename);
							settings_site_name.setText(myresult);
						}else if(sendstring.toString().equals("site_slogan")){
							EditText settings_site_slogan = (EditText) findViewById(R.id.settings_siteslogan);
							settings_site_slogan.setText(myresult);
						}else if(sendstring.toString().equals("site_mail")){
							EditText settings_site_slogan = (EditText) findViewById(R.id.settings_site_mail);
							settings_site_slogan.setText(myresult);
						}else if(sendstring.toString().equals("site_frontpage")){
							EditText settings_site_slogan = (EditText) findViewById(R.id.settings_frontpage);
							settings_site_slogan.setText(myresult);
						}else if(sendstring.toString().equals("site_403")){
							EditText settings_site_slogan = (EditText) findViewById(R.id.settings_403);
							settings_site_slogan.setText(myresult);
						}else if(sendstring.toString().equals("site_404")){
							EditText settings_site_slogan = (EditText) findViewById(R.id.settings_404);
							settings_site_slogan.setText(myresult);
						}else if(sendstring.toString().equals("default_nodes_main")){
							// Set the value of the spinner
							Spinner spinner = (Spinner) findViewById(R.id.spinner1);
							ArrayAdapter<CharSequence> SpinnerAdapter = ArrayAdapter.createFromResource(DrupalSiteSettings.this,
							        R.array.node_amounts_frontpage, android.R.layout.simple_spinner_item);
							int position = SpinnerAdapter.getPosition(myresult);
							SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);							
							spinner.setSelection(position);
						}
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
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

}
