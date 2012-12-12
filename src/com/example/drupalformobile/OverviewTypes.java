package com.example.drupalformobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class OverviewTypes extends ListActivity {
	private SimpleAdapter itemAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_overview_types);
		
		new SetTypesMenu().execute();
	}
	private class SetTypesMenu extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... twitterURL) {
			return "kay";
		}
		protected void onPostExecute(String result) {
			List<Map<String, String>> itemArrey = new ArrayList<Map<String, String>>();
			
			itemArrey.clear();
			
			itemAdapter = new SimpleAdapter(OverviewTypes.this, itemArrey, 
					R.layout.list_view_types, 
					new String[] {"title", "desc"},
					new int[] {R.id.settypetitle,
					R.id.desc}); 
			
			Map<String, String> itemArrey1 = new HashMap<String, String>();
			itemArrey1.put("title", "Nodes");
			itemArrey1.put("desc", "View your site nodes");
			itemArrey.add(itemArrey1);
			Map<String, String> itemArrey2 = new HashMap<String, String>();
			itemArrey2.put("title", "Taxonomy");
			itemArrey2.put("desc", "View your site Taxonomy Vocabularies");
			itemArrey.add(itemArrey2);
			Map<String, String> itemArrey3 = new HashMap<String, String>();
			itemArrey3.put("title", "Site Settings");
			itemArrey3.put("desc", "Manage site settings (sitename, slogan, 404 page etc..)");
			itemArrey.add(itemArrey3);
			Map<String, String> itemArrey4 = new HashMap<String, String>();
			itemArrey4.put("title", "Maintenance Mode Settings");
			itemArrey4.put("desc", "Manage Maintenance Mode (enable/disable, message)");
			itemArrey.add(itemArrey4);
			
			setListAdapter(itemAdapter);
		}
	}
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		Intent intent = null;
		
		TextView txt = (TextView) l.getChildAt(position).findViewById(R.id.settypetitle);
		
		String action = txt.getText().toString();
		if( action == "Nodes" ){
			intent = new Intent(this, GetNodeList.class);
			Toast.makeText(this, "Loading Node...", Toast.LENGTH_LONG).show();
		}else if ( action == "Taxonomy" ){
			intent = new Intent(this, GetTaxonomyList.class);
			Toast.makeText(this, "Loading Taxonomy...", Toast.LENGTH_LONG).show();
		}else if ( action == "Site Settings" ){
			intent = new Intent(this, DrupalSiteSettings.class);
		}else if ( action == "Maintenance Mode Settings" ){
			intent = new Intent(this, DrupalMaintenanceSettings.class);
		}else{
			intent = new Intent(this, GetNodeList.class);
			Toast.makeText(this, "Loading Failed :(..."+action, Toast.LENGTH_LONG).show();
		}
		
		
		startActivity(intent);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_overview_types, menu);
		return true;
	}

}
