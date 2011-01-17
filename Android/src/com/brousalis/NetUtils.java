package com.brousalis;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.util.Log;

public class NetUtils {
	/**
	 * Get the return data from a specific HTTP connection.
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
	
	/**
	 * Downloads a file from a url and places it in the data directory named the
	 * output filename. This function is intended to work with images.
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
}
