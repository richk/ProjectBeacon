package com.codepath.beacon;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class NotificationBubble extends Service {

  private WindowManager windowManager;
  private View notifBubble;
  private ImageView ivCancel;
  private TextView tvBubble;
  private String message;
  private static final String LOG_TAG = NotificationBubble.class.getSimpleName();

  @Override
  public IBinder onBind(Intent intent) {
    // Not used
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    message = intent.getExtras().getString("message");
    tvBubble.setText(message);
    return super.onStartCommand(intent, flags, startId);
  }
  
  @Override
  public void onCreate() {
    super.onCreate();

    windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

    LayoutInflater inflator = LayoutInflater.from(this);
    notifBubble = inflator.inflate(R.layout.notif, null);
    ivCancel = (ImageView)notifBubble.findViewById(R.id.ivCancel);
    tvBubble = (TextView)notifBubble.findViewById(R.id.tvBubble);
            
    final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

    params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
//    params.x = 0;
//    params.y = 100;

    windowManager.addView(notifBubble, params);
    
    notifBubble.setOnTouchListener(new View.OnTouchListener() {
      private int initialX;
      private int initialY;
      private float initialTouchX;
      private float initialTouchY;

      @Override public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            initialX = params.x;
            initialY = params.y;
            initialTouchX = event.getRawX();
            initialTouchY = event.getRawY();
            return true;
          case MotionEvent.ACTION_UP:
            return true;
          case MotionEvent.ACTION_MOVE:
            params.x = initialX + (int) (event.getRawX() - initialTouchX);
            params.y = initialY + (int) (event.getRawY() - initialTouchY);
            windowManager.updateViewLayout(notifBubble, params);
            return true;
        }
        return false;
      }
    });
    
    ivCancel.setOnClickListener(new OnClickListener() {
      
      @Override
      public void onClick(View v) {
        stopSelf();
      }
    });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (notifBubble != null)
      windowManager.removeView(notifBubble);
  }
}
