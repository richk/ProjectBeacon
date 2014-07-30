package com.codepath.beacon.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.beacon.R;
import com.codepath.beacon.fragments.RecipeAlertDialog;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

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
	
	public void onLogin(View view) {
//		Intent i = new Intent(this, SignUpActivity.class);
//		startActivity(i);
		if (!isNetworkAvailable()) {
			showNoNetwork();
			return;
		}
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
		//Toast.makeText(getApplicationContext(), "Hooray..the user can login", Toast.LENGTH_SHORT).show();
		Intent scanIntent = new Intent(this, MyRecipeActivity.class);
		startActivity(scanIntent);
		
	}

	private void handleInvalidUsernamePassword() {
		Toast.makeText(getApplicationContext(), "Invalid Username or password", Toast.LENGTH_SHORT).show();	
	}
	
	  public void showNoNetwork() {
		  RecipeAlertDialog alertDialog = new RecipeAlertDialog();
		  Bundle args = new Bundle();
		  args.putString("message", getResources().getString(R.string.network_error_message));
		  alertDialog.setArguments(args);
		  alertDialog.show(getFragmentManager(), null);
		  return;
	  }
	  
	  public boolean isNetworkAvailable() {
		    ConnectivityManager connectivityManager 
		          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
		}
}
