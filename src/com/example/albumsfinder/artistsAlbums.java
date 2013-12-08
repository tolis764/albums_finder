package com.example.albumsfinder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class artistsAlbums  extends Activity {
	private String key="075658a26831746573eeddd00af499b1";
	private final String baseURL = "http://ws.audioscrobbler.com/2.0/";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ListView lv = (ListView) findViewById(R.id.lv);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> list, View arg1, int pos,
					long arg3) {
				EditText et = (EditText) findViewById(R.id.et);		
				String selectedAlbum = 	(String) list.getAdapter().getItem(pos);
				String artistSelected = et.getText().toString();
				
				Intent i = new Intent(artistsAlbums.this, albumsDetails.class);
				i.putExtra("artistSelected", artistSelected);
				i.putExtra("selectedAlbum", selectedAlbum);
				startActivity(i);
			}
			
		});
		
		
		Button b1 = (Button) findViewById(R.id.b1);
		b1.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				EditText et = (EditText) findViewById(R.id.et);		
				new DownloadAlbumTask().execute(et.getText().toString());
			}
		});
	}

	
	public List<String> getTopAlbumsByArtist(String artist){
		List<String> albums = new ArrayList<String>();
		
		InputStream in = null;
		Document doc = null;		
		try{
			in = openHttpConnection(baseURL + "?method=artist.gettopalbums&artist=" + URLEncoder.encode(artist, "UTF-8") + "&api_key=" + key);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			try{
				db = dbf.newDocumentBuilder();
				doc = db.parse(in);
			}catch(ParserConfigurationException e){
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();			}
		}catch(SAXException e){
			e.printStackTrace();
			} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr;
		 try{
			 expr = xpath.compile("/lfm/topalbums/album/name/text()");
			 NodeList nodes = (NodeList) expr.evaluate(doc,XPathConstants.NODESET);	
			 int n = nodes.getLength();
			 for( n=0; n<nodes.getLength(); n++){
				 String val = nodes.item(n).getNodeValue();
				 albums.add(val);
			 }
		 }catch(XPathExpressionException e){
			 e.printStackTrace();
		 }
			return albums; 
		}
	

	public static InputStream openHttpConnection(String urlString) throws IOException{		
		InputStream is = null;
		int response = -1;
		
		URL url = new URL(urlString);
		Log.i("HTTP", urlString);
		URLConnection c = url.openConnection();
		
		if(!(c instanceof HttpURLConnection)){
			Log.e("HTTP", "WRONG URL");
			throw new IOException();
		}else{
			try{
					HttpURLConnection httpConn = (HttpURLConnection) c;
					httpConn.setAllowUserInteraction(false);
					httpConn.setInstanceFollowRedirects(true);
					httpConn.setRequestMethod("GET");
					httpConn.connect();
					response = httpConn.getResponseCode();					
					if(response==HttpURLConnection.HTTP_OK){
						is= httpConn.getInputStream();
					}
			}catch (Exception ex){
				Log.d("Networking", ex.getLocalizedMessage());
				throw new IOException("Error connecting");
			}
		}		
		return is;
	 }
	
	
	
	private class DownloadAlbumTask extends AsyncTask<String, Void, List<String>>{
		@Override
		protected List<String> doInBackground(String... params) {
			return getTopAlbumsByArtist(params[0]);
		}		
		@Override
		protected void onPostExecute(List<String>result){
			if(result.isEmpty()){
				Toast.makeText(artistsAlbums.this, "No albums found", Toast.LENGTH_LONG).show();
			}else{
				
			ListView lv = (ListView) findViewById(R.id.lv);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(artistsAlbums.this, android.R.layout.simple_list_item_1, result);
			lv.setAdapter(adapter);
			}			
		  }		
		}
	
	
}