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
import org.apache.http.entity.StringEntity;
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
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ShowNode extends Activity {
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
		String getnodeid = intent.getStringExtra(GetNodeList.EXTRA_MESSAGE);
		
		// Set loading icon
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		// Deze url is nodig: http://dev.easycom.be/default/node.json?parameters[nid]=20482
        // get settings and set domain
    	SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
    	String domain = DrupalForMobileSettings.getString("domain", "emptytext");
		
    	setContentView(R.layout.activity_show_node);

    	setProgressBarIndeterminateVisibility(true);
    	
    	new GetNodes().execute("http://"+domain+"/default/node/"+getnodeid+".json"); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_show_node, menu);
		return true;
	}
	public void savenodedata(View view) throws ClientProtocolException, IOException{
        // 200 = ok 406 = allreadyloggedin
        if( getLoginCookie().getStatusCode() == 200 || getLoginCookie().getStatusCode() == 406 ){
        	HttpResponse tweetResponse = null;
        	
        	EditText node_edit_title = (EditText) findViewById(R.id.editnodetitle);
        	//EditText node_edit_body  = (EditText) findViewById(R.id.editnodebody);
        	EditText node_edit_body  = (EditText) findViewById(R.id.editnodebody);
        	
        	final CheckBox node_edit_promote = (CheckBox) findViewById(R.id.fppromote);
        	final CheckBox node_edit_sticky = (CheckBox) findViewById(R.id.stickynode);
        	final CheckBox node_edit_publish = (CheckBox) findViewById(R.id.nodepublished);
        	
    		Intent intent = getIntent();
    		
    		String getnodeid = intent.getStringExtra(GetNodeList.EXTRA_MESSAGE);
    		SharedPreferences DrupalForMobileSettings = getSharedPreferences("DrupalForMobileSettings", MODE_PRIVATE);
        	String MyUpdateUrl = "http://"+DrupalForMobileSettings.getString("domain", "emptytext")+"/default/node/"+getnodeid;
        	
        	/*List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        	nameValuePairs.add(new BasicNameValuePair("title", node_edit_title.getText().toString()));
        	nameValuePairs.add(new BasicNameValuePair("body[und][0][value]", node_edit_body.getText().toString()));*/
        	
        	String promote = null;
        	String sticky = null;
        	String status = null;
        	String title = node_edit_title.getText().toString();
        	String body = node_edit_body.getText().toString();
        	body = body.replaceAll("\r\n", "");
        	body = body.replaceAll("\n", "");
        	if(node_edit_promote.isChecked()){
        		promote = "1";
        	}else{
        		promote = "0";
        	}
        	if(node_edit_sticky.isChecked()){
        		sticky = "1";
        	}else{
        		sticky = "0";
        	}
        	if(node_edit_publish.isChecked()){
        		status = "1";
        	}else{
        		status = "0";
        	} 
        	
        	String JsonPut = "{\"title\":\""+title+"\",\"status\":\""+status+"\",\"sticky\":\""+sticky+"\",\"promote\":\""+promote+"\",\"body\":{\"und\":[{\"value\":\""+body+"\"}]}}";
        	
			/*nameValuePairs.add(new BasicNameValuePair("promote", promote));
			nameValuePairs.add(new BasicNameValuePair("sticky", sticky));
			nameValuePairs.add(new BasicNameValuePair("status", status));*/
        	
        	HttpPut tweetGet = new HttpPut(MyUpdateUrl);
        	StringEntity entity = new StringEntity(JsonPut, HTTP.UTF_8);
        	entity.setContentType("application/json");
        	tweetGet.setEntity(entity);
        	//tweetGet.setEntity(new UrlEncodedFormEntity(JsonPut));
        	
        	tweetResponse = tweetClient.execute(tweetGet, localContext);
        	StatusLine searchStatus1 = tweetResponse.getStatusLine();
        	if ( searchStatus1.getStatusCode() == 200) {
        		Toast.makeText(this, "Node update complete!"+searchStatus1.getStatusCode(), Toast.LENGTH_LONG).show();
        	}else{
        		Toast.makeText(this, "Error updating node. Please try again..."+searchStatus1.getStatusCode()+JsonPut, Toast.LENGTH_LONG).show();
        	}
        }
	}
	private class GetNodes extends AsyncTask<String, Void, String> {
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
			TextView titlefield = (TextView) findViewById( R.id.editnodetitle );
			TextView bodyfield = (TextView) findViewById( R.id.editnodebody );
			// init strings
			int promotetofront = 0;
			int nodepublished = 0;
			int promotetofrontsticky = 0;
			StringBuilder tweetResultBuilder = new StringBuilder();
			try {
				/*
				 * We must wrap our result becouse
				 * otherwise it wont work for the loop..
				 */
				result = "{\"results\":["+result+"]}";
				JSONObject resultObject = new JSONObject(result);
				JSONArray tweetArray = resultObject.getJSONArray("results");
				for (int t=0; t<tweetArray.length(); t++) {
					JSONObject tweetObject = tweetArray.getJSONObject(t);
					
					tweetResultBuilder.append(tweetObject.get("title")+" - - - ");
					
					titlefield.setText((String) tweetObject.get("title"));
					
					/*
					 * The following hump of code is just
					 * to get the node body.. Crazy right?
					 */
					JSONObject phone  = tweetObject.getJSONObject("body");
					JSONArray  phonee = phone.getJSONArray("und");
					for (int g=0; g<phonee.length(); g++) {
						JSONObject phone3 = phonee.getJSONObject(g);
						bodyfield.setText((String) phone3.get("safe_value"));
					}
					
					promotetofront = Integer.parseInt(tweetObject.get("promote").toString());
					nodepublished = Integer.parseInt(tweetObject.get("status").toString());
					promotetofrontsticky = Integer.parseInt(tweetObject.get("sticky").toString());
					
				}
			}
			catch (Exception e) {
			    e.printStackTrace();
			}
			
			if(tweetResultBuilder.length()<0){
				titlefield.setText((String) "Something went wrong :(");
			}else{
				setProgressBarIndeterminateVisibility(false);
				if ( promotetofront != 0 ){
			        final CheckBox promotetofp = (CheckBox) findViewById(R.id.fppromote);
			        promotetofp.setChecked(true);
				}					
				if ( nodepublished != 0 ){
			        final CheckBox nodepublish = (CheckBox) findViewById(R.id.nodepublished);
			        nodepublish.setChecked(true);
				}				
				if ( promotetofrontsticky != 0 ){
			        final CheckBox promotetofpsticky = (CheckBox) findViewById(R.id.stickynode);
			        promotetofpsticky.setChecked(true);
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
