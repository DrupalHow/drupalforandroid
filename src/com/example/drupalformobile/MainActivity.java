package com.example.drupalformobile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	HttpContext localContext = new BasicHttpContext();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get field values form storage
        SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
    	String username = DrupalForMobileSettings.getString("username", "");
    	String password = DrupalForMobileSettings.getString("password", "");
    	String domain = DrupalForMobileSettings.getString("domain", "");
    	// load content
        setContentView(R.layout.activity_main);
        
        // Make checkbox checked
        final CheckBox SaveSettings = (CheckBox) findViewById(R.id.savesettings);
        SaveSettings.setChecked(true);
    	// set field values
    	TextView userfield = (TextView) findViewById( R.id.username );
    	userfield.setText(username);
    	TextView passwordfield = (TextView) findViewById( R.id.password );
    	passwordfield.setText(password);
    	TextView domainfield = (TextView) findViewById( R.id.domain );
    	domainfield.setText(domain);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    private class DoLogin extends AsyncTask<String, Void, String> {
    	@Override
		protected String doInBackground(String... twitterURL) {	
    		return "kay";
    	}
    	protected void onPostExecute(String result) {
        	String Myurl = "http://dev.easycom.be/default/user/login.json";
        	HttpClient tweetClient = new DefaultHttpClient();
        	HttpPost tweetGet = new HttpPost(Myurl);
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair("username", "admin"));
	        nameValuePairs.add(new BasicNameValuePair("password", "95a73qz"));
	        nameValuePairs.add(new BasicNameValuePair("action", "login"));
	        try {
				tweetGet.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        HttpResponse tweetResponse1 = null;
			try {
				tweetResponse1 = tweetClient.execute(tweetGet, localContext);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        StatusLine searchStatus = tweetResponse1.getStatusLine();
	        
	        if( searchStatus.getStatusCode() == 200 ){
		        Toast.makeText(MainActivity.this, "Login okay!", Toast.LENGTH_LONG).show();
	        }
    	}
    }
    public void dologin(View view) {
    	// ...
        EditText usernamefield = (EditText) findViewById(R.id.username);
        EditText passwordfield = (EditText) findViewById(R.id.password);
        EditText domainfield = (EditText) findViewById(R.id.domain);
        
        
        if( usernamefield.getText().toString().length() == 0 || passwordfield.getText().toString().length() == 0 || domainfield.getText().toString().length() == 0 ){
        	Toast.makeText(this, "Please fill in the fields", Toast.LENGTH_SHORT).show();
        }else{
            final CheckBox SaveSettings = (CheckBox) findViewById(R.id.savesettings);
            
            if(SaveSettings.isChecked()){
            	SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
            	SharedPreferences.Editor e = DrupalForMobileSettings.edit();
            	
            	e.putString("username", usernamefield.getText().toString()); // add or overwrite someValue
            	e.putString("password", passwordfield.getText().toString()); // add or overwrite someValue
            	e.putString("domain", domainfield.getText().toString()); // add or overwrite someValue
            	e.commit(); // this saves to disk and notifies observers
            }else{
            	SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
            	SharedPreferences.Editor e = DrupalForMobileSettings.edit();
            	
            	e.putString("username", ""); // add or overwrite someValue
            	e.putString("password", ""); // add or overwrite someValue
            	e.putString("domain", ""); // add or overwrite someValue
            	e.commit(); // this saves to disk and notifies observers
            }
            new DoLogin().execute("test");
            Intent intent = new Intent(this, OverviewTypes.class);
            startActivity(intent);
        }
        
    }
}
