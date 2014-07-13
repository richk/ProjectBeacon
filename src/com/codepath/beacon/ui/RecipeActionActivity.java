package com.codepath.beacon.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.codepath.beacon.R;

public class RecipeActionActivity extends Activity {
	private static final String LOG_TAG = RecipeActionActivity.class.getSimpleName();

	private EditText etMessage;
	private EditText etPhn;
	private CheckBox cbSms;
	private CheckBox cbNotification;
	private RadioGroup rgTrigger;
	private RadioButton rbLeaving;
	private RadioButton rbApproaching;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_action);
		etMessage = (EditText) findViewById(R.id.et_message);
		etPhn = (EditText) findViewById(R.id.et_phone);
		cbSms = (CheckBox) findViewById(R.id.cb_sms);
		cbNotification = (CheckBox) findViewById(R.id.cb_notification);
		rgTrigger = (RadioGroup) findViewById(R.id.rg_triggers);
		rbLeaving = (RadioButton) findViewById(R.id.radio0);
		rbApproaching = (RadioButton) findViewById(R.id.radio1);
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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onNotify(View view) {
		String trigger;
		if (rbLeaving.isChecked()) {
			trigger = "leaving";
		} else {
			trigger = "approaching";
		}
		String message = etMessage.getText().toString();
		boolean isSms = cbSms.isChecked();
		boolean isNotification = cbNotification.isChecked();
		String phn = etPhn.getText().toString();
		Intent returnIntent = new Intent();
		returnIntent.putExtra("trigger", trigger);
		returnIntent.putExtra("message", message);
		returnIntent.putExtra("isSms", isSms);
		returnIntent.putExtra("isNotification", isNotification);
		returnIntent.putExtra("phone", phn);
		setResult(RESULT_OK,returnIntent);
		finish();
	}

}
