package com.anwarelmakrahy.pwncore.plugins;

import com.anwarelmakrahy.pwncore.R;
import com.matabii.dev.scaleimageview.ScaleImageView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

public class ImageViewerActivity extends Activity {

	private String path, type;
	private ScaleImageView image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if (!intent.hasExtra("path")
				|| !intent.hasExtra("type")
				|| (type = intent.getStringExtra("type")) == null
				|| (path = intent.getStringExtra("path")) == null) {
			finish();
			return;
		}
		
		setContentView(R.layout.activity_imageviewer);
		image = (ScaleImageView) findViewById(R.id.image);
		
		getShot(type, path);
	}

	private void getShot(String type, String path) {
		if (type.equals("screenshot")) {
			setTitle("Get Screenshot");

		    Bitmap bitmap = BitmapFactory.decodeFile(path);
		    if (bitmap != null)
		    	image.setImageBitmap(bitmap);
		    else finish();
		}
		
		else if (type.equals("webcam_snap")) {
			setTitle("Get Webcam snap");

		    Bitmap bitmap = BitmapFactory.decodeFile(path);
		    if (bitmap != null)
		    	image.setImageBitmap(bitmap);
		    else finish();			
		}
		else
			finish();
	}
}
