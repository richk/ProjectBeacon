package com.codepath.beacon.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.codepath.beacon.R;
import com.codepath.beacon.adapter.PackageItem;
import com.codepath.beacon.contracts.IntentTransferContracts;
import com.codepath.beacon.contracts.RecipeContracts;
import com.codepath.beacon.contracts.RecipeContracts.TRIGGERS;
import com.codepath.beacon.fragments.SelectedAppsFragment;
import com.codepath.beacon.models.TriggerAction;
import com.codepath.beacon.models.TriggerAction.NOTIFICATION_TYPE;

public class RecipeActionActivity1 extends Activity {
  private static final String LOG_TAG = RecipeActionActivity1.class
      .getSimpleName();
  public static final int APPS_REQUEST_CODE = 0;

  private EditText etMessage;
  private EditText etPhn;
  private RadioButton rbLeaving;
  private RadioButton rbApproaching;
  private ImageView ivNotification;
  private ImageView ivSms;
  private ImageView ivSilent;
  private ImageView ivLight;
  private ImageView ivApps;
  private TextView tvPhone;
  private TextView tvMessage;
  private TextView tvNotificationDesc;
  private TextView tvSmsDesc;
  private TextView tvSilentDesc;
  private TextView tvLightDesc;
  private TextView tvAppsDesc;
  
  private SelectedAppsFragment mAppList = new SelectedAppsFragment();
  
  private Context mContext;

  NOTIFICATION_TYPE mNotificationType;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recipe_action1);
    mContext = this;
    getActionBar().setDisplayHomeAsUpEnabled(true);
    etMessage = (EditText) findViewById(R.id.et_message);
    etPhn = (EditText) findViewById(R.id.et_phone);
    rbLeaving = (RadioButton) findViewById(R.id.rb_leaving);
    rbApproaching = (RadioButton) findViewById(R.id.rb_approaching);
    tvMessage = (TextView)findViewById(R.id.tvMessage);
    tvPhone = (TextView)findViewById(R.id.tvPhone);

    ivNotification = (ImageView) findViewById(R.id.ivNotification);
    ivSms = (ImageView) findViewById(R.id.ivSms);
    ivSilent = (ImageView)findViewById(R.id.ivSilentMode);
    ivLight = (ImageView)findViewById(R.id.ivLight);
    ivApps = (ImageView) findViewById(R.id.ivLaunchApps);

    tvNotificationDesc = (TextView) findViewById(R.id.tvNotificationDesc);
    tvSmsDesc = (TextView) findViewById(R.id.tvSmsDesc);
    tvSilentDesc = (TextView) findViewById(R.id.tvSilentDesc);
    tvLightDesc = (TextView) findViewById(R.id.tvLightDesc);
    tvAppsDesc = (TextView) findViewById(R.id.tvAppsDesc);
    
    if(savedInstanceState != null){
      int noti = savedInstanceState.getInt("notification_type");
      if(noti != 0){
        mNotificationType = NOTIFICATION_TYPE.values()[noti];
      }
      
      rbLeaving.setChecked(savedInstanceState.getBoolean("leaving"));
      rbApproaching.setChecked(savedInstanceState.getBoolean("approaching"));
    }


    ivNotification.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        ivNotification.setBackgroundResource(R.drawable.image_border);
        etMessage.setText("");
        etPhn.setText("");
        etMessage.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.VISIBLE);
        etPhn.setVisibility(View.INVISIBLE);
        tvPhone.setVisibility(View.INVISIBLE);

        ivSms.setBackground(null);
        ivSilent.setBackground(null);
        ivLight.setBackground(null);
        ivApps.setBackground(null);
        if (mAppList.isVisible()) {
        	FragmentManager fm = getFragmentManager();  
            fm.beginTransaction().remove(mAppList).commit();	
        }
        
        tvNotificationDesc.setVisibility(View.VISIBLE);
        tvSmsDesc.setVisibility(View.INVISIBLE);
        tvSilentDesc.setVisibility(View.INVISIBLE);
        tvLightDesc.setVisibility(View.INVISIBLE);
        tvAppsDesc.setVisibility(View.INVISIBLE);
        mNotificationType = NOTIFICATION_TYPE.NOTIFICATION;

      }
    });

    ivSms.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        ivSms.setBackgroundResource(R.drawable.image_border);
        etMessage.setText("");
        etMessage.setVisibility(View.VISIBLE);
        etPhn.setText("");
        etPhn.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.VISIBLE);
        tvPhone.setVisibility(View.VISIBLE);
        
        ivSilent.setBackground(null);
        ivNotification.setBackground(null);
        ivLight.setBackground(null);
        ivApps.setBackground(null);
        if (mAppList.isVisible()) {
        	FragmentManager fm = getFragmentManager();  
            fm.beginTransaction().remove(mAppList).commit();	
        }
        tvAppsDesc.setVisibility(View.INVISIBLE);

        tvNotificationDesc.setVisibility(View.INVISIBLE);
        tvSmsDesc.setVisibility(View.VISIBLE);
        tvSilentDesc.setVisibility(View.INVISIBLE);
        tvLightDesc.setVisibility(View.INVISIBLE);
        mNotificationType = NOTIFICATION_TYPE.SMS;
      }
    });
    
    ivSilent.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        ivSilent.setBackgroundResource(R.drawable.image_border);
        etPhn.setVisibility(View.INVISIBLE);
        tvPhone.setVisibility(View.INVISIBLE);
        tvMessage.setVisibility(View.INVISIBLE);
        etMessage.setVisibility(View.INVISIBLE);
        ivSms.setBackground(null);
        ivNotification.setBackground(null);
        ivLight.setBackground(null);
        ivApps.setBackground(null);
        if (mAppList.isVisible()) {
        	FragmentManager fm = getFragmentManager();  
            fm.beginTransaction().remove(mAppList).commit();	
        }
        tvAppsDesc.setVisibility(View.INVISIBLE);
        tvNotificationDesc.setVisibility(View.INVISIBLE);
        tvSmsDesc.setVisibility(View.INVISIBLE);
        tvSilentDesc.setVisibility(View.VISIBLE);
        tvLightDesc.setVisibility(View.INVISIBLE);
        mNotificationType = NOTIFICATION_TYPE.RINGER_SILENT;

      }
    });
    
    ivLight.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        ivLight.setBackgroundResource(R.drawable.image_border);
        etPhn.setVisibility(View.INVISIBLE);
        tvPhone.setVisibility(View.INVISIBLE);
        tvMessage.setVisibility(View.INVISIBLE);
        etMessage.setVisibility(View.INVISIBLE);
        ivSms.setBackground(null);
        ivNotification.setBackground(null);
        ivSilent.setBackground(null);
        ivApps.setBackground(null);
        if (mAppList.isVisible()) {
        	FragmentManager fm = getFragmentManager();  
            fm.beginTransaction().remove(mAppList).commit();	
        }
        tvAppsDesc.setVisibility(View.INVISIBLE);
        tvNotificationDesc.setVisibility(View.INVISIBLE);
        tvSmsDesc.setVisibility(View.INVISIBLE);
        tvSilentDesc.setVisibility(View.INVISIBLE);
        tvLightDesc.setVisibility(View.VISIBLE);

        mNotificationType = NOTIFICATION_TYPE.LIGHT;

      }
    });
    
    ivApps.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			ivApps.setBackgroundResource(R.drawable.image_border);
			
	        etPhn.setVisibility(View.GONE);
	        tvPhone.setVisibility(View.GONE);
	        tvMessage.setVisibility(View.GONE);
	        etMessage.setVisibility(View.GONE);
	        
	        ivSms.setBackground(null);
	        ivNotification.setBackground(null);
	        ivSilent.setBackground(null);
	        ivLight.setBackground(null);
	        
	        tvAppsDesc.setVisibility(View.VISIBLE);
	        tvNotificationDesc.setVisibility(View.INVISIBLE);
	        tvSmsDesc.setVisibility(View.INVISIBLE);
	        tvSilentDesc.setVisibility(View.INVISIBLE);
	        tvLightDesc.setVisibility(View.INVISIBLE);
	        
	        mNotificationType = NOTIFICATION_TYPE.LAUNCH_APPS;
	        
	        FragmentManager fm = getFragmentManager();  
	        fm.beginTransaction().replace(R.id.flAppListSmall, mAppList).commit();

			Intent launchAppsIntent = new Intent(mContext, AppListActivity.class);
			startActivityForResult(launchAppsIntent, APPS_REQUEST_CODE);
		}
	});


    populateTriggerAndAction();
  }
    
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putInt("notification_type", mNotificationType.ordinal());
    outState.putBoolean("leaving", rbLeaving.isChecked());
    outState.putBoolean("approaching", rbApproaching.isChecked());
    super.onSaveInstanceState(outState);
  }

  private void populateTriggerAndAction() {
	  String trigger = getIntent().getStringExtra(RecipeContracts.TRIGGER);
	  if (trigger != null) {
		  if (rbLeaving.getText().toString().equalsIgnoreCase(trigger)) {
			  rbLeaving.setChecked(true);
		  } else {
			  rbApproaching.setChecked(true);
		  }
	  }
	  TriggerAction notification = getIntent().getParcelableExtra(
			  RecipeContracts.TRIGGERACTION);
	  if (notification != null) {
		  mNotificationType = Enum.valueOf(NOTIFICATION_TYPE.class, notification.getType());
		  Log.d(LOG_TAG, "Populating with notification type:" + mNotificationType);
		  switch(mNotificationType) {
		  case SMS :
			  ivSms.setBackgroundResource(R.drawable.image_border);
			  etMessage.setVisibility(View.VISIBLE);
			  etPhn.setVisibility(View.VISIBLE);
			  tvMessage.setVisibility(View.VISIBLE);
			  tvPhone.setVisibility(View.VISIBLE);
			  tvNotificationDesc.setVisibility(View.INVISIBLE);
			  tvSmsDesc.setVisibility(View.VISIBLE);
			  tvSilentDesc.setVisibility(View.INVISIBLE);
			  tvLightDesc.setVisibility(View.INVISIBLE);
			  break;
		  case NOTIFICATION:
			  ivNotification.setBackgroundResource(R.drawable.image_border);
			  etMessage.setVisibility(View.VISIBLE);
			  tvMessage.setVisibility(View.VISIBLE);
			  tvNotificationDesc.setVisibility(View.VISIBLE);
			  tvSmsDesc.setVisibility(View.INVISIBLE);
			  tvSilentDesc.setVisibility(View.INVISIBLE);
			  tvLightDesc.setVisibility(View.INVISIBLE);
			  break;
		  case RINGER_SILENT :
			  ivSilent.setBackgroundResource(R.drawable.image_border);
			  tvNotificationDesc.setVisibility(View.INVISIBLE);
			  tvSmsDesc.setVisibility(View.INVISIBLE);
			  tvSilentDesc.setVisibility(View.VISIBLE);
			  tvLightDesc.setVisibility(View.INVISIBLE);
			  break;
		  case LIGHT :
			  ivLight.setBackgroundResource(R.drawable.image_border);
			  tvNotificationDesc.setVisibility(View.INVISIBLE);
			  tvSmsDesc.setVisibility(View.INVISIBLE);
			  tvSilentDesc.setVisibility(View.INVISIBLE);
			  tvLightDesc.setVisibility(View.VISIBLE);
			  break;
		  case LAUNCH_APPS :
			  ivApps.setBackgroundResource(R.drawable.image_border);
			  if (notification.getMessage() != null) {
				  populateSelectedAppView(notification.getMessage());
			  }
			  FragmentManager fm = getFragmentManager();  
			  fm.beginTransaction().replace(R.id.flAppListSmall, mAppList).commit();
			  break;
			  default:
				  Log.e(LOG_TAG, "Unsupprted Action Type:" + mNotificationType);
				  mNotificationType = NOTIFICATION_TYPE.NOTIFICATION;
		  }
		  if (notification.getMessage() != null) {
			  etMessage.setText(notification.getMessage());
		  }
		  if (notification.getExtra() != null) {
			  etPhn.setText(notification.getExtra());
		  }
	  } 	
	  else {
		  ivNotification.setBackgroundResource(R.drawable.image_border);
		  etMessage.setVisibility(View.VISIBLE);
		  tvMessage.setVisibility(View.VISIBLE);
		  tvNotificationDesc.setVisibility(View.VISIBLE);
		  tvSmsDesc.setVisibility(View.INVISIBLE);
		  tvSilentDesc.setVisibility(View.INVISIBLE);
		  tvLightDesc.setVisibility(View.INVISIBLE);
		  mNotificationType = TriggerAction.NOTIFICATION_TYPE.NOTIFICATION;
	  }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.recipe_action, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.recipeSave) {
      onSaveRecipe();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void onSaveRecipe() {
    String trigger;
    if (rbLeaving.isChecked()) {
      trigger = TRIGGERS.LEAVING.name();
    } else {
      trigger = TRIGGERS.APPROACHING.name();
    }
    String message = etMessage.getText().toString();
    if (mNotificationType == NOTIFICATION_TYPE.LAUNCH_APPS) {
    	message = mAppList.getSelectedApp().getPackageName();
    }
    String phn = etPhn.getText().toString();
    TriggerAction triggerAction = new TriggerAction();
    if (message != null) {
        triggerAction.setMessage(message);
    }
    Log.d(LOG_TAG, "Returning Notification Type:" + mNotificationType);
    triggerAction.setType(mNotificationType.name());
    if (phn != null) {
    	triggerAction.setExtra(phn);	
    }
    Intent returnIntent = new Intent();
    returnIntent.putExtra("trigger", trigger);
    returnIntent.putExtra(RecipeContracts.TRIGGERACTION, triggerAction);
    setResult(RESULT_OK, returnIntent);
    finish();
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  Log.d(LOG_TAG, "onActivityResult. Request Code:" + requestCode + ", resultCode:" + resultCode);
	  if (requestCode == APPS_REQUEST_CODE && resultCode == RESULT_OK) {
		  String packageName = data.getStringExtra(IntentTransferContracts.SELECTED_APPS_STRING);
		  populateSelectedAppView(packageName);
	  }
  }
  
  private void populateSelectedAppView(String packageName) {
	  Log.d(LOG_TAG, "Finding appInfo for package:" + packageName);
	  try {
		  ApplicationInfo app = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
		  PackageItem item = new PackageItem();
		  item.setName(getPackageManager().getApplicationLabel(app).toString());
		  item.setPackageName(app.packageName);
		  item.setIcon(app.loadIcon(getPackageManager()));
		  Log.d(LOG_TAG, "SelectedApp - Name:" + item.getName());
		  mAppList.setSelectedApp(item);
	  } catch (NameNotFoundException e) {
		  Log.e(LOG_TAG, "Application name not found", e);
	  }  
  }
}
