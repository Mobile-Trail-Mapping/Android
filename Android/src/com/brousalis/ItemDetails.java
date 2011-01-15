package com.brousalis;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ItemDetails extends Activity {

	private Bundle _extras;
	private static final String DATA_PATH = "/data/data/com.brousalis/files/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.item_details);
		_extras = this.getIntent().getExtras();
		_extras.get("title");
		_extras.get("summary");
		TextView title = (TextView) this.findViewById(R.id.detail_title);
		TextView summary = (TextView) this.findViewById(R.id.detail_summary);
		// TextView condition =
		// (TextView)this.findViewById(R.id.detail_condition);

		title.setText(_extras.get("title").toString());
		summary.setText(_extras.get("summary").toString());

		
		Gallery g = (Gallery) findViewById(R.id.gallery);
	    
		
		// TODO: Conditions aren't implemented for a trail scale in the XML yet.
		// Do that, then this
		// Line gets uncommented.
		// condition.setText(_extras.get("title").toString());

	    String webFolder = "http://www.fernferret.com/mtm/images/";
		String[] toImage = {"forest.png", "city.png", "desert.png", "island.png"};
		String dataFolder = "/data/data/com.brousalis/files/";
		
		ImageView image = (ImageView) findViewById(R.id.imview);
		
		for(int i = 0; i < toImage.length; i++) {
			File imageFile = new File(dataFolder+toImage[i]);
			if(!imageFile.exists()) {
				DownloadFromUrl(webFolder + toImage[i], toImage[i], DATA_PATH);
			}
		}
		Bitmap bMap = BitmapFactory.decodeFile(dataFolder + toImage[0]);
		image.setImageBitmap(bMap);
		
		g.setAdapter(new ImageAdapter(this, dataFolder, toImage));

	    g.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView parent, View v, int position, long id) {
	            Toast.makeText(ItemDetails.this, "" + position, Toast.LENGTH_SHORT).show();
	        }
	    });
		
	}

	/**
	 * Downloads a file from a url and places it in the data directory named the
	 * output filename. This function is intended to work with images.
	 * 
	 * @param imageURL
	 *            The Url of the file to download
	 * @param fileName
	 *            Output name
	 */
	private void DownloadFromUrl(String fileURL, String outputFileName, String dataPath) {
		try {
			URL url = new URL(fileURL);
			File file = new File(outputFileName);

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
