package com.codepath.beacon.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.R;
import com.codepath.beacon.contracts.RecipeContracts;
import com.codepath.beacon.contracts.RecipeContracts.TRIGGERS;
import com.codepath.beacon.fragments.RecipeAlertDialog;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.models.TriggerAction;
import com.codepath.beacon.models.TriggerAction.NOTIFICATION_TYPE;
import com.codepath.beacon.scan.BeaconListener;
import com.codepath.beacon.scan.BeaconManager;
import com.codepath.beacon.scan.BleActivity;
import com.codepath.beacon.scan.BleDeviceInfo;
import com.codepath.beacon.scan.BleService.State;
import com.codepath.beacon.ui.RecipeActionActivity1;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class RecipeDetailActivity extends Activity implements BeaconListener {
  private static final String LOG_TAG = RecipeDetailActivity.class
      .getSimpleName();

  private Recipe recipe;
  private String mRecipeId;

  private Recipe oldRecipe;

  BeaconManager beaconManager;

  private boolean createFlag = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recipe_detail);
    beaconManager = new BeaconManager(this, null);
    String action = getIntent().getStringExtra(RecipeContracts.RECIPE_ACTION);
    Log.d(LOG_TAG, "Creating a new recipe:" + action);
    if (action != null && RecipeContracts.RECIPE_ACTION_CREATE.equalsIgnoreCase(action)) {
    	Log.d(LOG_TAG, "Setting createFlag to true");
    	createFlag = true;
    }
    ActionBar ab = getActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
    populateRecipeDetail();
  }

  @Override
  protected void onStop() {
    beaconManager.stopListenening();
    super.onStop();
  }

  @Override
  protected void onStart() {
    super.onStart();
    beaconManager.startListening();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.recipe_detail, menu);
    return true;
  }

  private void populateRecipeDetail() {
	  if (createFlag) {
		  recipe = new Recipe();
	  } else {
		  recipe = getIntent().getParcelableExtra("recipe");
		  mRecipeId = getIntent().getStringExtra("recipeId");
		  Log.d(LOG_TAG, "Recipe object id:" + mRecipeId);
		  oldRecipe = new Recipe();
		  if (recipe != null) {
			  oldRecipe.setBeacon(recipe.getBeacon());
			  oldRecipe.setDisplayName(recipe.getDisplayName());
			  oldRecipe.setTriggerAction(recipe.getTriggerAction());
			  oldRecipe.setTriggerActionDisplayName(recipe.getTriggerActionDisplayName());
			  oldRecipe.setActivationDate(recipe.getActivationDate());
			  oldRecipe.setTrigger(recipe.getTrigger());
			  oldRecipe.setStatus(recipe.isStatus());
			  oldRecipe.setUserID(recipe.getUserID());
		  }
	  }
	  showRecipe();
  }

  private void showRecipe() {
    TextView tvActivationDate = (TextView) findViewById(R.id.tvActivationDate);
    TextView tvSelectedBeacon = (TextView) findViewById(R.id.tvSelectedBeacon);
    TextView tvSelectedAction = (TextView) findViewById(R.id.tvSelectedAction);
    ImageView ibPlus1 = (ImageView) findViewById(R.id.btn_beacon);
    ImageView ibPlus2 = (ImageView) findViewById(R.id.btn_notification);
    TextView tvTriggerandNotification = (TextView) findViewById(R.id.tvTriggerandNotification);

    if (!createFlag) {
      if (recipe.getActivationDate() != null) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
        String reportDate = sdf.format(recipe.getActivationDate());
        tvActivationDate.setText("Activated on " + reportDate);
      }
    } else {
      tvActivationDate.setVisibility(View.GONE);
      tvTriggerandNotification.setVisibility(View.GONE);
    }

    // Image button control
    if (createFlag) {
      if (recipe.getBeacon() == null) {
        ibPlus1.setImageResource(R.drawable.plus1);
        ibPlus2.setImageResource(R.drawable.plus1);
      } else {
        ibPlus1.setImageResource(R.drawable.antenna);
        ibPlus1.setBackgroundResource(R.drawable.image_border);
        if (recipe.getTriggerAction() == null)
          ibPlus2.setImageResource(R.drawable.plus1);
        else{
          if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.NOTIFICATION.toString())){
            ibPlus2.setImageResource(R.drawable.notification1);
            ibPlus2.setBackgroundResource(R.drawable.image_border);
          }else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.SMS.toString())){
            ibPlus2.setImageResource(R.drawable.smsgreen1);
            ibPlus2.setBackgroundResource(R.drawable.image_border);
          }else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.RINGER_SILENT.toString())){
            ibPlus2.setImageResource(R.drawable.silent);
            ibPlus2.setBackgroundResource(R.drawable.image_border);            
          }
        }
      }
    } else {
      ibPlus1.setImageResource(R.drawable.antenna);
      ibPlus1.setBackgroundResource(R.drawable.image_border);
      if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.NOTIFICATION.toString())){
        ibPlus2.setImageResource(R.drawable.notification1);
        ibPlus2.setBackgroundResource(R.drawable.image_border);
      }else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.SMS.toString())){
        ibPlus2.setImageResource(R.drawable.smsgreen1);
        ibPlus2.setBackgroundResource(R.drawable.image_border);
      }else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.RINGER_SILENT.toString())){
        ibPlus2.setImageResource(R.drawable.silent);
        ibPlus2.setBackgroundResource(R.drawable.image_border);            
      }
    }

    if (recipe.getBeacon() != null && recipe.getBeacon().getName() != null)
      tvSelectedBeacon.setText(recipe.getBeacon().getName());

    if (recipe.getTriggerAction() != null && recipe.getTrigger() != null){
      String notif = recipe.getTriggerActionDisplayName();
      if(notif.equalsIgnoreCase(NOTIFICATION_TYPE.NOTIFICATION.toString()))
          notif = "Notification";
      if(notif.equalsIgnoreCase(NOTIFICATION_TYPE.RINGER_SILENT.toString()))
        notif = "Ringer Silent";
      tvSelectedAction.setText(notif + " on " + recipe.getTrigger().toLowerCase());
    }

    if (recipe.getDisplayName() != null
        && recipe.getTriggerActionDisplayName() != null){
      String notif = recipe.getTriggerActionDisplayName();
      if(notif.equalsIgnoreCase(NOTIFICATION_TYPE.NOTIFICATION.toString()))
          notif = "Send Notification ";
      if(notif.equalsIgnoreCase(NOTIFICATION_TYPE.SMS.toString()))
        notif = "Send SMS ";
      if(notif.equalsIgnoreCase(NOTIFICATION_TYPE.RINGER_SILENT.toString()))
        notif = "Make ringer silent ";
      tvTriggerandNotification.setText(notif
              + " when " + recipe.getTrigger().toLowerCase() + " " + recipe.getDisplayName());
    }
  }
  
  public void onScanBeacon(View view) {
    Intent scanIntent = new Intent(this, BleActivity.class);
    startActivityForResult(scanIntent, 0);
  }

  public void findRecipeInBackground(final String recipeID) {
    ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
    query.getInBackground(recipeID, new GetCallback<Recipe>() {
      public void done(final Recipe recipe, ParseException e) {
        if (e == null) {
          recipe.getParseObject(RecipeContracts.BEACON)
              .fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                  if (e == null) {
                    recipe.setBeacon((BleDeviceInfo) object);
                  } else {
                    Log.e(LOG_TAG, "ParseException", e);
                  }
                }
              });
          recipe.getParseObject(RecipeContracts.TRIGGERACTION)
              .fetchIfNeededInBackground(new GetCallback<ParseObject>() {

                @Override
                public void done(ParseObject noticationObject,
                    ParseException done) {
                  if (done == null) {
                    recipe.setTriggerAction((TriggerAction) noticationObject);
                  } else {
                    Log.e(LOG_TAG, "ParseException", done);
                  }
                }
              });
          showRecipe();

        } else {
          // something went wrong
        }
      }
    });
  }

  public void onSaveAction(MenuItem mi) {
	  Log.d(LOG_TAG, "Checking if recipe already exists");
	  Log.d(LOG_TAG, "Recipe exists:" + BeaconApplication.getApplication().recipeExists(recipe));
	  Log.d(LOG_TAG, "Recipe being edited:" + recipe.isBeingEdited());
    if (BeaconApplication.getApplication().recipeExists(recipe)
        && recipe.isBeingEdited()) {
    	Log.d(LOG_TAG, "Recipe already exists");
      RecipeAlertDialog alert = new RecipeAlertDialog();
      Bundle args = new Bundle();
      args.putString(
          "message",
          "Recipe on that beacon and trigger already exists. Create recipe with another beacon or trigger.");
      alert.setArguments(args);
      alert.show(getFragmentManager(), null);
      return;
    } else {
    	Log.d(LOG_TAG, "Recipe does not exist. Allowing create/edit to it.");
    }
    if (recipe.getBeacon() == null || recipe.getTrigger() == null || recipe.getTriggerAction() == null) {
    	RecipeAlertDialog alert = new RecipeAlertDialog();
        Bundle args = new Bundle();
        args.putString(
            "message",
            "Recipe does not have beacon, trigger or trigger action defined. Please check and try again");
        alert.setArguments(args);
        alert.show(getFragmentManager(), null);
        return;
    }
    String action;
    if (createFlag) {
      String userID = ParseUser.getCurrentUser().getObjectId();
      recipe.setUserID(userID);
      // set default values for recipe
      recipe.setStatus(true);
      recipe.setActivationDate(new Date());
      recipe.setTriggeredCount(0);
      action = RecipeContracts.RECIPE_ACTION_CREATE;
      BeaconApplication.getApplication().addNewRecipe(recipe);
    } else {
    	action = RecipeContracts.RECIPE_ACTION_UPDATE;
    	BeaconApplication.getApplication().deleteRecipe(oldRecipe);
    	BeaconApplication.getApplication().addNewRecipe(recipe);
    }
    if (TRIGGERS.APPROACHING.name().equalsIgnoreCase(recipe.getTrigger())) {
    	beaconManager.monitorDeviceEntry(recipe.getBeacon(), recipe.getTriggerAction());
    } else if (TRIGGERS.LEAVING.name().equalsIgnoreCase(
        recipe.getTrigger())) {
    	beaconManager.monitorDeviceExit(recipe.getBeacon(), recipe.getTriggerAction());
    }
    returnToMyRecipe(action);
  }

  public void onSetAction(View view) {
    Intent scanIntent = new Intent(this, RecipeActionActivity1.class);
    scanIntent.putExtra(RecipeContracts.TRIGGERACTION,
        recipe.getTriggerAction());
    scanIntent.putExtra(RecipeContracts.TRIGGER, recipe.getTrigger());
    startActivityForResult(scanIntent, 1);
  }

  public void onDeleteAction(MenuItem mi) {
	  Log.d(LOG_TAG, "Deleting recipe with object id:" + recipe.getObjectId());
    if(createFlag){
      finish();
      return;
    }
    recipe.deleteRecipe(mRecipeId);
    BeaconApplication.getApplication().deleteRecipe(recipe);
    returnToMyRecipe(RecipeContracts.RECIPE_ACTION_DELETE);
  }

  public void returnToMyRecipe(String recipeAction) {
    Intent data = new Intent();
    data.putExtra("recipe", recipe);
    data.putExtra("oldRecipe", oldRecipe);
    data.putExtra("recipeId", mRecipeId);
    data.putExtra(RecipeContracts.RECIPE_ACTION, recipeAction);
    setResult(RESULT_OK, data);
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0) {
      if (resultCode == RESULT_OK) {
        BleDeviceInfo deviceInfo = (BleDeviceInfo) data
            .getParcelableExtra("beacon");
        if ((createFlag && recipe.getBeacon() != null) || (oldRecipe != null
            && !oldRecipe.getBeacon().equals(recipe.getBeacon()))) {
          recipe.setEditState(true);
        }
        recipe.setBeacon(deviceInfo);
        recipe.setDisplayName(deviceInfo.getName());
        deviceInfo.setEditState(false);
        showRecipe();
      }
    } else if (requestCode == 1) {
      if (resultCode == RESULT_OK) {
        String trigger = data.getStringExtra("trigger");
        TriggerAction notification = (TriggerAction)data.
            getParcelableExtra(RecipeContracts.TRIGGERACTION);
        recipe.setTriggerAction(notification);
        recipe.setTriggerActionDisplayName(notification.getType());
        recipe.setTrigger(trigger);
        if ((createFlag && recipe.getTrigger() != null) || (oldRecipe != null
            && !oldRecipe.getTrigger().equalsIgnoreCase(recipe.getTrigger()))) {
          recipe.setEditState(true);
        }
        showRecipe();
      }
    } else {
      Log.e("RecipeDetailActivity", "Invalid request code:" + requestCode);
    }
  }

  @Override
  public void onStateChanged(State newState) {
  }

  @Override
  public void onNewDeviceDiscovered(BleDeviceInfo[] devices) {
  }

  @Override
  public void onDeviceLost(BleDeviceInfo[] device) {
//    Toast.makeText(BeaconApplication.getApplication(),
//        "Lost a device..." + device[0], Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onDeviceFound(BleDeviceInfo[] device) {
//    Toast.makeText(BeaconApplication.getApplication(),
//        "Found a device..." + device[0], Toast.LENGTH_SHORT).show();
  }
}
