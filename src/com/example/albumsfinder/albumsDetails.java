package com.example.albumsfinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.GetChars;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class albumsDetails extends Activity{
	private static String key="075658a26831746573eeddd00af499b1";
	private final static String baseURL = "http://ws.audioscrobbler.com/2.0/";
	String artist = "";
	String album="";
	
	
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.secondact);
		
		artist = getIntent().getStringExtra("artistSelected");
		album = getIntent().getStringExtra("selectedAlbum");
				
        new DownloadImage().execute(artist,album);
        new DownloadTracksTask().execute(artist,album);
	}
	

	public static Bitmap getImage(String url){
		Bitmap image = null;
		InputStream in = null;		
		try{			
				in = artistsAlbums.openHttpConnection(url);
				image = BitmapFactory.decodeStream(in);
				in.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			return image;		
	       }

	
	//album's cover
	public Bitmap getAlbumCover(String artist, String album){
		InputStream in = null; 
		Bitmap bitmap = null;
			try{
					in = artistsAlbums.openHttpConnection(baseURL + "?method=album.getinfo&artist=" + URLEncoder.encode(artist, "UTF-8") + "&album=" + URLEncoder.encode(album, "UTF-8") + "&api_key=" + key);
					Document doc = null;
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db;
					
				try{	
					db = dbf.newDocumentBuilder();
					doc = db.parse(in);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr;
		 
		try{
			 expr = xpath.compile("/lfm/album/image[@size=\"medium\"]/text()");		 	
			 NodeList nodes = (NodeList) expr.evaluate(doc,XPathConstants.NODESET);	
			 int n = nodes.getLength();
			 for(int i=0; i<n; i++){
				 String val = nodes.item(i).getNodeValue();
				 bitmap = getImage(val);
			 }
			 
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 return bitmap;
	  }
	
	
	
	
	//album's tracks
	public List<String>getTracks(String artist, String album){
		List<String>tracks = new ArrayList<String>();
		InputStream is = null;	
		Document doc = null;
		try{
								
			String url = baseURL + "?method=album.getinfo&artist=" + URLEncoder.encode(artist, "UTF-8") + "&album=" + URLEncoder.encode(album, "UTF-8") + "&api_key=" + key;
			Log.d("url", url);
			is = artistsAlbums.openHttpConnection(url);			
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;			
			
			  try {
				    db = dbf.newDocumentBuilder();
					doc = db.parse(is);
			  		}catch(ParserConfigurationException pce){
					pce.printStackTrace();
					}catch(SAXException se){
					se.printStackTrace();
					}			
					
					XPath xpath = XPathFactory.newInstance().newXPath();
					XPathExpression expr;
					
					try {
						expr = xpath.compile("/lfm/album/tracks/track/name/text()");
						NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
						int n = nl.getLength();
						for(int i=0; i<nl.getLength(); i++){
							String val = nl.item(i).getNodeValue();
							tracks.add(val);
					}
					}catch (XPathExpressionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		return tracks;
		}
	
	
	
	
	private class DownloadImage extends AsyncTask<String, Void, Bitmap>{
		@Override
		protected Bitmap doInBackground(String... params) {			
			return getAlbumCover(params[0], params[1]);
		}		
		
		@Override
		protected void onPostExecute(Bitmap result){
			if(result == null)
				Toast.makeText(albumsDetails.this, "No cover found", Toast.LENGTH_LONG).show();
			else{
			ImageView imgv = (ImageView) findViewById(R.id.iv);
			imgv.setImageBitmap(result);
			}
		}		
	}
	
	
	
	private class DownloadTracksTask extends AsyncTask<String, Void, List<String>>{
		@Override
		protected List<String> doInBackground(String... params) {
			return getTracks(params[0], params[1]);
		}		
		@Override
		protected void onPostExecute(List<String>result){
		if(result.isEmpty()){
			Toast.makeText(albumsDetails.this, "No tracks found", Toast.LENGTH_LONG).show();
		}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(albumsDetails.this, android.R.layout.simple_list_item_1, result);
			ListView lv = (ListView) findViewById(R.id.lvTracks);
			lv.setAdapter(adapter);
		}		
	}
	
	
	
}