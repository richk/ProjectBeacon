package com.codepath.beacon.ui;

import com.codepath.beacon.R;
import com.codepath.beacon.R.id;
import com.codepath.beacon.R.layout;
import com.codepath.beacon.R.menu;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Build;

public class RecipeActionActivity extends Activity {
	private static final String LOG_TAG = RecipeActionActivity.class.getSimpleName();

	private EditText etMessage;
	private EditText etPhn;
	private CheckBox cbSms;
	private CheckBox cbNotification;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_action);
		etMessage = (EditText) findViewById(R.id.et_message);
		etPhn = (EditText) findViewById(R.id.et_phone);
		cbSms = (CheckBox) findViewById(R.id.cb_sms);
		cbNotification = (CheckBox) findViewById(R.id.cb_notification);
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
		if(cbNotification.isChecked()) {
			sendNotification();
		} else
			sendSMS();
	}

	public void sendNotification() {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.notification_icon)
		        .setContentTitle("Beacon Magic")
		        .setContentText(etMessage.getText().toString());
		Intent resultIntent = new Intent(this, RecipeActionActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(RecipeActionActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, mBuilder.build());
	}

	public void sendSMS() {
		SmsManager sms = SmsManager.getDefault();
	       sms.sendTextMessage(etPhn.getText().toString(), null, etMessage.getText().toString(), null, null);
	}
}
