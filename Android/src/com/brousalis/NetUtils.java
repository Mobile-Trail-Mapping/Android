package com.brousalis;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;

//import org.apache.http.entity.mime.*;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Utility class that provides multiple network communication methods
 * 
 * @author ericstokes
 * 
 */
public class NetUtils {
	/**
	 * Location to store files at where no other applications can get to it.
	 */
	private static final String DATA_FOLDER = "/data/data/com.brousalis/files/";
	
	/**
	 * Get the return data from a specific HTTP connection.
	 * 
	 * @param url The url to request data from.
	 * @return The resulting string from the request.
	 */
	public static String getHTTPData(String url) {
		URLConnection connection;
		String httpResult = "";
		try {
			connection = new URL(url).openConnection();
			connection.connect();
			InputStream inputStream = connection.getInputStream();
			StringBuffer buffer = new StringBuffer();
			byte[] b = new byte[4096];
			for (int n; (n = inputStream.read(b)) != -1;) {
				buffer.append(new String(b, 0, n));
			}
			httpResult = buffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return httpResult;
	}
	
	public static String hashStringSHA(String str) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
			digest.update(str.getBytes());
			byte messageDigest[] = digest.digest();
			
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
	        for (int i = 0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "HASH ERROR";
	}
	
	/**
	 * Provides the file location used when selecting an image
	 * 
	 * @param contentUri The URI provided by the image pick activity
	 * @return File path to pass to the uploader
	 */
	public static String getRealPathFromURI(Uri contentUri, Activity a) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = a.managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
	public static boolean postHTTPImage(HashMap<String, String> items, String url, String imageFileName) {
		
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			File imageFile = new File(imageFileName);

			FileBody image = new FileBody(imageFile);
			MultipartEntity uploadEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			uploadEntity.addPart("file", image);
			
			for (Map.Entry<String, String> s : items.entrySet()) {
				uploadEntity.addPart(s.getKey(), new StringBody(s.getValue()));
			}
			httpPost.setEntity(uploadEntity);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = httpClient.execute(httpPost, responseHandler);
			Log.w(ShowMap.MTM, response);
			
		} catch (Exception e) {
			
		}
		
		
		return false;
	}
	
	public static String postHTTPData(HashMap<String, String> items, String url) {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(items.size());
			for (Map.Entry<String, String> s : items.entrySet()) {
				nameValuePairs.add(new BasicNameValuePair(s.getKey(), s.getValue()));
			}
			
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			// Execute HTTP Post Request
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httppost, responseHandler);
			Log.w("MTM", responseBody);
			return responseBody;
		} catch (ClientProtocolException e) {
			Log.w(ShowMap.MTM, "ClientProtocolException");
		} catch (IOException e) {
			Log.w(ShowMap.MTM, "IOException");
		}
		return "ERROR fetching data";
	}
	
	/**
	 * Downloads a file from a url and places it in the data directory named the output filename. This function is intended to work with images.
	 * 
	 * @param fileURL The Url of the file to download
	 * @param outputFileName Name of the file to output. Cannot contain ANY '/'s!!!
	 * @param dataPath Path to the folder where the file will be stored
	 */
	public static void DownloadFromUrl(String fileURL, String outputFileName, String dataPath) {
		Log.w(ShowMap.MTM, "Logging Function call: DownloadFromUrl(" + fileURL + ", " + outputFileName + ", " + dataPath + ");");
		try {
			URL url = new URL(fileURL);
			File file = new File(outputFileName);
			
			// Build the directory structure
			File fileDirectory = new File(dataPath);
			fileDirectory.mkdirs();
			
			URLConnection urlConnect = url.openConnection();
			
			InputStream inputStream = urlConnect.getInputStream();
			BufferedInputStream bInputStream = new BufferedInputStream(inputStream);
			
			/* Read the buffered input to a ByteArrayBuffer */
			ByteArrayBuffer bArrayBuffer = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bInputStream.read()) != -1) {
				bArrayBuffer.append((byte) current);
			}
			
			/* Save the file to disk in our private directory */
			FileOutputStream output = new FileOutputStream(dataPath + file);
			output.write(bArrayBuffer.toByteArray());
			output.close();
			
		} catch (IOException e) {
			Log.d("ImageManager", "Error: " + e);
		}
		
	}
	
	/**
	 * Deletes a folder from the local filesystem.
	 * 
	 * @param id The folder to delete. Folders are simply numbers.
	 */
	public static void deleteFolder(int id) {
		File fileDirectory = new File(DATA_FOLDER + id + "/");
		if (fileDirectory.exists()) {
			File[] files = fileDirectory.listFiles();
			
			for (File f : files) {
				f.delete();
			}
			
			fileDirectory.delete();
		}
	}
}
