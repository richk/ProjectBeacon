package com.codepath.beacon.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.codepath.beacon.R;
import com.codepath.beacon.contracts.RecipeContracts;
import com.codepath.beacon.contracts.RecipeContracts.TRIGGERS;
import com.codepath.beacon.models.TriggerAction;
import com.codepath.beacon.models.TriggerAction.NOTIFICATION_TYPE;

public class RecipeActionActivity1 extends Activity {
  private static final String LOG_TAG = RecipeActionActivity1.class
      .getSimpleName();

  private EditText etMessage;
  private EditText etPhn;
  private RadioButton rbLeaving;
  private RadioButton rbApproaching;
  private ImageView ivNotification;
  private ImageView ivSms;
  private TextView tvPhone;
  private TextView tvMessage;

  NOTIFICATION_TYPE notificationType = NOTIFICATION_TYPE.NONE;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recipe_action1);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    etMessage = (EditText) findViewById(R.id.et_message);
    etPhn = (EditText) findViewById(R.id.et_phone);
    rbLeaving = (RadioButton) findViewById(R.id.rb_leaving);
    rbApproaching = (RadioButton) findViewById(R.id.rb_approaching);
    tvMessage = (TextView)findViewById(R.id.tvMessage);
    tvPhone = (TextView)findViewById(R.id.tvPhone);

    ivNotification = (ImageView) findViewById(R.id.ivNotification);
    ivSms = (ImageView) findViewById(R.id.ivSms);
    
    if(savedInstanceState != null){
      int noti = savedInstanceState.getInt("notification_type");
      if(noti != 0){
        notificationType = NOTIFICATION_TYPE.values()[noti];
      }
      
      rbLeaving.setChecked(savedInstanceState.getBoolean("leaving"));
      rbApproaching.setChecked(savedInstanceState.getBoolean("approaching"));
    }


    ivNotification.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        ivNotification.setBackgroundResource(R.drawable.image_border);
        etMessage.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.VISIBLE);
        if (notificationType == NOTIFICATION_TYPE.SMS) {
          etPhn.setVisibility(View.INVISIBLE);
          tvPhone.setVisibility(View.INVISIBLE);
          ivSms.setBackground(null);
        }
        notificationType = NOTIFICATION_TYPE.NOTIFICATION;

      }
    });

    ivSms.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        ivSms.setBackgroundResource(R.drawable.image_border);
        etMessage.setVisibility(View.VISIBLE);
        etPhn.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.VISIBLE);
        tvPhone.setVisibility(View.VISIBLE);
        
        if (notificationType == NOTIFICATION_TYPE.NOTIFICATION) {
          ivNotification.setBackground(null);
        }

        notificationType = NOTIFICATION_TYPE.SMS;
      }
    });

    populateTriggerAndAction();
  }
    
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putInt("notification_type", notificationType.ordinal());
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
        RecipeContracts.RECIPE_ACTION);
    if (notification != null) {
      if (TriggerAction.NOTIFICATION_TYPE.SMS.name().equalsIgnoreCase(
          notification.getType())) {
        ivSms.setBackgroundResource(R.drawable.image_border);
        etMessage.setVisibility(View.VISIBLE);
        etPhn.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.VISIBLE);
        tvPhone.setVisibility(View.VISIBLE);
        notificationType = TriggerAction.NOTIFICATION_TYPE.SMS;
      } else {
        ivNotification.setBackgroundResource(R.drawable.image_border);
        etMessage.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.VISIBLE);
        notificationType = TriggerAction.NOTIFICATION_TYPE.NOTIFICATION;
      }
      if (notification.getMessage() != null) {
        etMessage.setText(notification.getMessage());
      }
      if (notification.getExtra() != null) {
        etPhn.setText(notification.getExtra());
      }
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
    boolean isSms = (notificationType==NOTIFICATION_TYPE.SMS)?true:false;
    boolean isNotification = (notificationType==NOTIFICATION_TYPE.NOTIFICATION)?true:false;
    String phn = etPhn.getText().toString();
    Intent returnIntent = new Intent();
    returnIntent.putExtra("trigger", trigger);
    returnIntent.putExtra("message", message);
    returnIntent.putExtra("isSms", isSms);
    returnIntent.putExtra("isNotification", isNotification);
    returnIntent.putExtra("phone", phn);
    setResult(RESULT_OK, returnIntent);
    finish();
  }

}
