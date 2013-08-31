package com.anwarelmakrahy.pwncore;

import com.anwarelmakrahy.pwncore.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);    
        
        long splashDelay = 1000;
		new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            	Intent main = new Intent(getApplicationContext(), MainActivity.class);
            	main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); 
                startActivity(main);
                finish();
            }
        }, splashDelay );
	}

}
