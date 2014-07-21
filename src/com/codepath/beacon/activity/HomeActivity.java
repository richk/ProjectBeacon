package com.codepath.beacon.activity;

import com.codepath.beacon.R;
import com.codepath.beacon.R.layout;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class HomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
		  handleSuccessfulLogin();
		}
	}

	protected void handleSuccessfulLogin() {
		Toast.makeText(getApplicationContext(), "Hooray..the user can login", Toast.LENGTH_SHORT).show();
		Intent scanIntent = new Intent(this, MyRecipeActivity.class);
		startActivity(scanIntent);
		
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
