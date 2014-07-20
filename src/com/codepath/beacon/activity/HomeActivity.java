package com.codepath.beacon.activity;

import com.codepath.beacon.R;
import com.codepath.beacon.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class HomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
	}
	
	public void onSignin(View view) {
		Intent i = new Intent(this, LoginActivity.class);
		startActivity(i);
	}
	
	public void onSignup(View view) {
		Intent i = new Intent(this, SignUpActivity.class);
		startActivity(i);
	}
}
