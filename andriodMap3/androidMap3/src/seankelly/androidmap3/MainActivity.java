package seankelly.androidmap3;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.google.android.maps.MapActivity;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	// reference for this HTTP GET from
	// http://hmkcode.com/android-internet-connection-using-http-get-httpclient/

	private GoogleMap myMap;
	private int mapType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// setContentView(R.layout.map_activity);
		
		
		myMap = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        LatLng sydney = new LatLng(-33.867, 151.206);

        myMap.setMyLocationEnabled(true);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

        myMap.addMarker(new MarkerOptions()
                .title("Sydney")
                .snippet("The most populous city in Australia.")
                .position(sydney));
        
        mapType = 0;
        toggleMapType();
        
        //setMapType(GoogleMap.MAP_TYPE_SATELLITE);
	}
	
	public void toggleOnClick(View v)
	{
		toggleMapType();
	}
	
	private void toggleMapType()
	{
		mapType++;
		mapType %= 4; //range of values 0 - 3
		
		Button b = (Button) findViewById(R.id.toggleButton);
		
		switch(mapType)
		{
		case 0:
			myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			b.setText("View: Hybrid");
			break;
		case 1:
			myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			b.setText("View: Normal");
			break;
		case 2:
			myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			b.setText("View: Satellite");
			break;
		case 3:
			myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			b.setText("View: Terrain");
			break;
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void clearPrompt(View v) {
		EditText input = (EditText) findViewById(R.id.enterAddress);
		if (input.getText().toString().equals("Enter an Address")) {
			input.setText("");
		}
	}

	public void searchAddress(View v) {

		EditText input = (EditText) findViewById(R.id.enterAddress);
		String address = input.getText().toString();
		String[] addressChunks = address.split("[ ]+");
		String addressQuery = "https://maps.googleapis.com/maps/api/geocode/json?address=";

		if (addressChunks.length > 0) {
			addressQuery += addressChunks[0];
		}

		for (int i = 1; i < addressChunks.length; i++) {
			addressQuery += "+";
			addressQuery += addressChunks[i];
		}

		addressQuery += "&key=AIzaSyCRP8acNPFERUdMPouoFU_cM0sTfdT6tww";

		// ((EditText) findViewById(R.id.enterAddress)).setText(addressQuery);

		new HttpAsyncTask(this).execute(addressQuery);

	}

	private static String readStream(InputStream is) throws IOException {

		StringBuilder ans = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		// ^^^ provide a size of the buffer????

		for (String currentLine = br.readLine(); currentLine != null; currentLine = br
				.readLine()) {
			ans.append(currentLine);
		}

		is.close();

		return ans.toString();
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {

		private MainActivity mainAc;

		public HttpAsyncTask(MainActivity ma) {
			mainAc = ma;
		}

		@Override
		protected String doInBackground(String... urls) {

			return GET(urls[0]);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {

			Double lat = (double) 0;
			Double lon = (double) 0;
			JSONObject respJSON = null;
			try {
				respJSON = new JSONObject(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			try {
				lat = (Double) respJSON.getJSONArray("results")
						.getJSONObject(0).getJSONObject("geometry")
						.getJSONObject("location").get("lat");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			try {
				lon = (Double) respJSON.getJSONArray("results")
						.getJSONObject(0).getJSONObject("geometry")
						.getJSONObject("location").get("lng");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

			LatLng destination = new LatLng(lat, lon);

			myMap.setMyLocationEnabled(true);
			myMap.animateCamera(CameraUpdateFactory.zoomTo(15));
			myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 13));

			EditText input = (EditText) findViewById(R.id.enterAddress);
			String address = input.getText().toString();

			myMap.addMarker(new MarkerOptions().title("Query:")
					.snippet(address).position(destination));
			
			String postalCode = "Postal Code: ";
			
			try {
				postalCode += 
						respJSON.getJSONArray("results").getJSONObject(0)
						.getJSONArray("address_components").getJSONObject(7).getString("long_name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				postalCode += "N/A";
			}
			
			((TextView) findViewById(R.id.postalDisplay)).setText(postalCode);
		}
	}

	public static String GET(String url) {
		InputStream inputStream = null;
		String result = "";
		try {

			// create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// make GET request to the given URL
			HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

			// receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// convert inputstream to string
			if (inputStream != null)
				result = readStream(inputStream);
			else
				result = "Did not work!";

		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}

		return result;
	}

	public boolean isConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else
			return false;
	}
}
