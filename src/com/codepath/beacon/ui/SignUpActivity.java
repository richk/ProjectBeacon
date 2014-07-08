package com.codepath.beacon.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.beacon.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends Activity {
	private static final String LOG_TAG = SignUpActivity.class.getSimpleName();
	
	private EditText etUserName;
	private EditText etPwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		etUserName = (EditText) findViewById(R.id.etSignUpEmail);
		etPwd = (EditText) findViewById(R.id.etSignUpPwd);
		if (savedInstanceState != null) {
			String uname = savedInstanceState.getString("unameField");
			String pwd = savedInstanceState.getString("pwdField");
			etUserName.setText(uname);
			etPwd.setText(pwd);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	      // Respond to the action bar's Up/Home button
	      case android.R.id.home:
	        Intent intent = NavUtils.getParentActivityIntent(this); 
	        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP); 
	        NavUtils.navigateUpTo(this, intent);
	        return true;        
	    }

	    return super.onOptionsItemSelected(item);
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

	
	public void onSignup(View view) {
		Toast.makeText(this, "Signing in", Toast.LENGTH_SHORT).show();
		String username = etUserName.getText().toString();
		String pwd = etPwd.getText().toString();
		ParseUser user = new ParseUser();
		user.setUsername(username);
		user.setPassword(pwd);
		user.signUpInBackground(new SignUpCallback() {
			@Override
			public void done(ParseException exception) {
				if (exception == null) {
					handleSuccessfulSignup();
				} else {
					handleUnsuccessfulSignup(exception);
				}
			}
		});
	}

	protected void handleUnsuccessfulSignup(ParseException exception) {
		Toast.makeText(this, "Unsuccessful signup attempt:" + exception.getMessage(), Toast.LENGTH_SHORT).show();
	}

	protected void handleSuccessfulSignup() {
		Toast.makeText(this, "Signup Successful. Login to use the app now", Toast.LENGTH_SHORT).show();
	}
}
