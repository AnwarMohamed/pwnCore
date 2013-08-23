package com.anwarelmakrahy.pwncore;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;

public class PopupWindowService extends Service {

	@Override 
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override 
	public void onCreate() {
		super.onCreate();

		
		initiatePopupWindow();
	}

	 private PopupWindow pwindo;
	    private void initiatePopupWindow() {
		    try {

		    	WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		    	Display display = wm.getDefaultDisplay();
		    	Point size = new Point();
		    	display.getSize(size);
		    	
			    LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			    View layout = inflater.inflate(R.layout.layout_console, null);
			    pwindo = new PopupWindow(layout, size.x - 30, (int)(size.y * 0.75), true);
			    
			    //pwindo.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#eeeeee")));
			    pwindo.setOutsideTouchable(true);
			    
			    pwindo.setTouchable(true);
			    pwindo.setFocusable(true);
			    pwindo.showAtLocation(layout, Gravity.BOTTOM, 0, 15);
			
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
	    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}