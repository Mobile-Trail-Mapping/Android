package com.brousalis;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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
}
