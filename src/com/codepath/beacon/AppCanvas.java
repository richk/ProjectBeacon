package com.codepath.beacon;

import com.codepath.beacon.models.TriggerAction.NOTIFICATION_TYPE;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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

public class AppCanvas extends Service {

  private WindowManager windowManager;
  private View appCanvas;
  private ImageView ivCancel;
  private ImageView ivNotif;
  private TextView tvAppName;
  private String appName;
  private String triggerType;
  private static final String LOG_TAG = AppCanvas.class.getSimpleName();

  @Override
  public IBinder onBind(Intent intent) {
    // Not used
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
	  Log.d(LOG_TAG, "onStartCommand");
	  triggerType = intent.getExtras().getString("triggerType");
	  appName = intent.getExtras().getString("appName");
	  Log.d(LOG_TAG, "Trigger type:" + triggerType + ", appName:" + appName);
	  if (triggerType != null && NOTIFICATION_TYPE.LAUNCH_APPS.name().equalsIgnoreCase(triggerType)) {
		  try {
			ApplicationInfo app = getPackageManager().getApplicationInfo(appName, PackageManager.GET_META_DATA);
			tvAppName.setText(getPackageManager().getApplicationLabel(app).toString());
			ivNotif.setImageDrawable(app.loadIcon(getPackageManager()));
			ivNotif.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(appName);
					stopSelf();
					startActivity(LaunchIntent);
				}
			});
		} catch (NameNotFoundException e) {
			tvAppName.setText(appName);
		}	  
	  } else {
		  tvAppName.setText(appName);
	  }
    return super.onStartCommand(intent, flags, startId);
  }
  
  @Override
  public void onCreate() {
    super.onCreate();
    
    Log.d(LOG_TAG, "onCreate");

    windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

    LayoutInflater inflator = LayoutInflater.from(this);
    appCanvas = inflator.inflate(R.layout.app_canvas, null);
    ivCancel = (ImageView)appCanvas.findViewById(R.id.ivAppCancel);
    ivNotif = (ImageView) appCanvas.findViewById(R.id.ivAppNotif);
    tvAppName = (TextView)appCanvas.findViewById(R.id.tvAppName);
            
    final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

    params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
//    params.x = 0;
//    params.y = 100;

    windowManager.addView(appCanvas, params);
    
    appCanvas.setOnTouchListener(new View.OnTouchListener() {
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
            params.gravity = Gravity.NO_GRAVITY;
            windowManager.updateViewLayout(appCanvas, params);
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
    if (appCanvas != null)
      windowManager.removeView(appCanvas);
  }
}
