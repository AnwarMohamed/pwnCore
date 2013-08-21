package com.anwarelmakrahy.pwncore.TerminalView;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;
import android.widget.TextView;

public class TerminalView extends View {
	
    private final static String TAG = "TerminalView";
    private final static boolean LOG_KEY_EVENTS = false;
    private final static boolean LOG_IME = false;
    
    private Paint pBackground = new Paint();
    private int pBackgroundColor = Color.BLACK;
    
    private float viewWidth;
    private float viewHeight;
    
    private Context viewContext;
    
    public TerminalView(Context context) {
        super(context);
    }
    
    public TerminalView(Context context, AttributeSet attrs) {     
    	super(context, attrs);
    	viewContext = context; 
        initVariables(context);
    }
    
    private void initVariables(Context context) { 	   	
    	pBackground.setColor(pBackgroundColor);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
    }
    
    @Override
    public void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
    	
    	canvas.drawRect(0, 0, viewWidth, viewHeight, pBackground);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;

            case MotionEvent.ACTION_UP:
                return true;
        }
        
        requestFocus();
        return false;
    }
    
}
