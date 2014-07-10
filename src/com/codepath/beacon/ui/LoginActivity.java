package com.codepath.beacon.ui;

import com.codepath.beacon.R;
import com.codepath.beacon.R.id;
import com.codepath.beacon.R.layout;
import com.codepath.beacon.R.menu;
import com.codepath.beacon.activity.MyRecipeActivity;
import com.codepath.beacon.scan.BleActivity;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Build;

public class LoginActivity extends Activity {
	private static final String LOG_TAG = LoginActivity.class.getSimpleName();
	
	private EditText etUserName;
	private EditText etPwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		etUserName = (EditText) findViewById(R.id.etEmail);
		etPwd = (EditText) findViewById(R.id.etPwd);
		if (savedInstanceState != null) {
			String uname = savedInstanceState.getString("unameField");
			String pwd = savedInstanceState.getString("pwdField");
			etUserName.setText(uname);
			etPwd.setText(pwd);
		}
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
		  handleSuccessfulLogin();
		}
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
	
	public void onSigningUp(View view) {
		Intent i = new Intent(this, SignUpActivity.class);
		startActivity(i);
	}
	
	public void onLogin(View view) {
//		Intent i = new Intent(this, SignUpActivity.class);
//		startActivity(i);
		ParseUser.logInInBackground(etUserName.getText().toString(), etPwd.getText().toString(), new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException exception) {
				if (user != null && exception == null) {
					handleSuccessfulLogin();
				} else if (user == null) {
					handleInvalidUsernamePassword();
				} else {
					handleUnknownLoginError();
				}

			}
		});
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		String uname = etUserName.getText().toString();
		String pwd = etPwd.getText().toString();
		if (!uname.isEmpty()) {
		    outState.putString("unameField", uname);
		}
		if (!pwd.isEmpty()) {
			outState.putString("pwdField", pwd);
		}
	}

	protected void handleUnknownLoginError() {
		Toast.makeText(getApplicationContext(), "Unknown error while logging in. Please try again later", Toast.LENGTH_SHORT).show();
		
	}

	protected void handleSuccessfulLogin() {
		Toast.makeText(getApplicationContext(), "Hooray..the user can login", Toast.LENGTH_SHORT).show();
		Intent scanIntent = new Intent(this, MyRecipeActivity.class);
		startActivity(scanIntent);
		
	}

	private void handleInvalidUsernamePassword() {
		Toast.makeText(getApplicationContext(), "Invalid Username or password", Toast.LENGTH_SHORT).show();	
	}
}
