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

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class DrupalMaintenanceSettings extends Activity {
	HttpContext localContext = new BasicHttpContext();
	HttpClient tweetClient = new DefaultHttpClient();
	/*
	 * For now i dont have appwide sessions..
	 * That's why i have to login each time..
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drupal_maintenance_settings);
		new GetMaintenanceSettings().execute("nothingactually");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_drupal_maintenance_settings,
				menu);
		return true;
	}
	public void SaveMaintenanceSettings(View view) throws ClientProtocolException, IOException{
		EditText settings_site_name = (EditText) findViewById(R.id.maintenance_mssage);
		final CheckBox SaveSettings = (CheckBox) findViewById(R.id.enablemaintenance);
		String setmaintenancemode;
		if(SaveSettings.isChecked()){
			setmaintenancemode = "1";
		}else{
			setmaintenancemode = "0";
		}
		String maintenancemodemessage = settings_site_name.getText().toString();
		
	    String [] aStopLightColors;  
	    aStopLightColors = new String[2];  
	    aStopLightColors[0] = new String("maintenance_mode");
	    aStopLightColors[1] = new String("maintenance_mode_message");  
	    
		SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
    	String MyGetStringUrl = "http://"+DrupalForMobileSettings.getString("domain", "emptytext")+"/default/system/set_variable.json";
    	HttpPost GetVariable = new HttpPost(MyGetStringUrl);
    	for ( int t=0; t<aStopLightColors.length; t++ ){
			String sendstring = aStopLightColors[t];
			List<NameValuePair> LoginValuePairs = new ArrayList<NameValuePair>();
			LoginValuePairs.add(new BasicNameValuePair("name", sendstring));
			if(sendstring.equals("maintenance_mode")){
				LoginValuePairs.add(new BasicNameValuePair("value", setmaintenancemode));
			}else if(sendstring.equals("maintenance_mode_message")){
				LoginValuePairs.add(new BasicNameValuePair("value", TextUtils.htmlEncode(maintenancemodemessage)));
			}
			HttpResponse tweetResponse1 = null;
	
			GetVariable.setEntity(new UrlEncodedFormEntity(LoginValuePairs, HTTP.UTF_8));
			tweetResponse1 = tweetClient.execute(GetVariable, localContext);
			
			StatusLine searchStatus = tweetResponse1.getStatusLine();
			if( searchStatus.getStatusCode() == 200 ){ 
			}else{
				Toast.makeText(this, "Error updating settings. Please try again..."+searchStatus.getStatusCode(), Toast.LENGTH_LONG).show();
			}
    	}
	}
	private class GetMaintenanceSettings extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... twitterURL) {
			return "yup";
		}
		protected void onPostExecute(String result) {
			if( getLoginCookie().getStatusCode() == 200 || getLoginCookie().getStatusCode() == 406 ){
			
				// Build types array
			    String [] aStopLightColors;  
			    aStopLightColors = new String[2];  
			    aStopLightColors[0] = new String("maintenance_mode");
			    aStopLightColors[1] = new String("maintenance_mode_message");  
			    
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
						myresult = myresult.replace("\r\n", "\n");
						if(sendstring.toString().equals("maintenance_mode_message")){
							EditText settings_site_name = (EditText) findViewById(R.id.maintenance_mssage);
							settings_site_name.setText("\""+myresult+"\"");
						}else if(sendstring.toString().equals("maintenance_mode")){
							final CheckBox SaveSettings = (CheckBox) findViewById(R.id.enablemaintenance);
							if(myresult.equals("1")){
								SaveSettings.setChecked(true);
							}
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
