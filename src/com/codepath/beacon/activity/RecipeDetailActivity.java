package com.codepath.beacon.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
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
import com.codepath.beacon.recipe.RecipeManager;
import com.codepath.beacon.scan.BeaconListener;
import com.codepath.beacon.scan.BeaconManager;
import com.codepath.beacon.scan.BleDeviceInfo;
import com.codepath.beacon.scan.BleService.State;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class RecipeDetailActivity extends Activity implements BeaconListener, AnimationListener {
  private static final String LOG_TAG = RecipeDetailActivity.class
      .getSimpleName();

  private Recipe recipe;
  private String mRecipeId;
  private String mAction;

  private Recipe oldRecipe;

  BeaconManager beaconManager;

  private boolean createFlag = false;
  
  private Animation beaconAnimation1;
  private Animation beaconAnimation2;
  private Animation triggerAnimation1;
  private Animation triggerAnimation2;
  private boolean isBeaconPlusShowing = true;
  private NOTIFICATION_TYPE mLastNotificationType;
  private NOTIFICATION_TYPE mPendingNotificationType;
  
  TextView tvActivationDate;
  TextView tvSelectedBeacon;
  TextView tvSelectedAction;
  ImageView ibPlus1;
  ImageView ibPlus2;
  TextView tvTriggerandNotification;
  
  private static final int BEACON_SELECTION=0;
  private static final int ACTION_SELECTION=1;
  

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recipe_detail);
    tvActivationDate = (TextView) findViewById(R.id.tvActivationDate);
    tvSelectedBeacon = (TextView) findViewById(R.id.tvSelectedBeacon);
    tvSelectedAction = (TextView) findViewById(R.id.tvSelectedAction);
    ibPlus1 = (ImageView) findViewById(R.id.btn_beacon);
    ibPlus2 = (ImageView) findViewById(R.id.btn_notification);
    tvTriggerandNotification = (TextView) findViewById(R.id.tvTriggerandNotification);
	beaconAnimation1 = AnimationUtils.loadAnimation(this, R.anim.to_middle);
	beaconAnimation1.setAnimationListener(this);
	beaconAnimation2 = AnimationUtils.loadAnimation(this, R.anim.from_middle);
	beaconAnimation2.setAnimationListener(this);
	triggerAnimation1 = AnimationUtils.loadAnimation(this, R.anim.to_middle);
	triggerAnimation1.setAnimationListener(this);
	triggerAnimation2 = AnimationUtils.loadAnimation(this, R.anim.from_middle);
	triggerAnimation2.setAnimationListener(this);
    beaconManager = new BeaconManager(this, null);
    if (savedInstanceState != null) {
        mAction = savedInstanceState.getString(RecipeContracts.RECIPE_ACTION);
        recipe = savedInstanceState.getParcelable("recipe");
        mRecipeId = savedInstanceState.getString("recipeId");
    } else {
    	mAction = getIntent().getStringExtra(RecipeContracts.RECIPE_ACTION);
        recipe = getIntent().getParcelableExtra("recipe");
        mRecipeId = getIntent().getStringExtra("recipeId");
    }
    
    Log.d(LOG_TAG, "Creating a new recipe:" + mAction);
    if (mAction != null && RecipeContracts.RECIPE_ACTION_CREATE.equalsIgnoreCase(mAction)) {
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

  @Override
  protected void onSaveInstanceState(Bundle outState) {
	  // TODO Auto-generated method stub
	  super.onSaveInstanceState(outState);
	  if (mAction != null) {
	      outState.putString(RecipeContracts.RECIPE_ACTION, getIntent().getStringExtra(RecipeContracts.RECIPE_ACTION));
	  }
	  if (recipe != null) {
	      outState.putParcelable("recipe", recipe);
	  }
	  if (mRecipeId != null) {
	      outState.putString("recipeId", mRecipeId);
	  }
  }

  private void populateRecipeDetail() {
	  if (recipe == null) {
		  recipe = new Recipe();
	  } else {
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
	  showRecipe(-1);
  }

  private void showRecipe(int requestCode) {

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
        ibPlus1.setImageResource(R.drawable.plus2);
        ibPlus1.setBackgroundResource(R.drawable.dash_border);
      } else {
    	  if(requestCode == BEACON_SELECTION && isBeaconPlusShowing){
	    	  startAnimation(ibPlus1, beaconAnimation1);
    	  }else{
    		  ibPlus1.setImageResource(R.drawable.ic_launcher);
    		  ibPlus1.setBackgroundResource(R.drawable.image_border);
    		  displayBeaconName();
    	  }
      }
        if (recipe.getTriggerAction() == null){
          ibPlus2.setImageResource(R.drawable.plus2);
          ibPlus2.setBackgroundResource(R.drawable.dash_border);
        }else{
        	boolean isNotificationTypeChanged = false;
          if (mLastNotificationType == null || (mLastNotificationType != null && !recipe.getTriggerAction().getType().equalsIgnoreCase(mLastNotificationType.name()))) {
        	  isNotificationTypeChanged = true;
          }
          mLastNotificationType = Enum.valueOf(NOTIFICATION_TYPE.class, recipe.getTriggerAction().getType());
          if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.NOTIFICATION.toString())){
        	  mPendingNotificationType = NOTIFICATION_TYPE.NOTIFICATION;
          }else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.SMS.toString())){
        	  mPendingNotificationType = NOTIFICATION_TYPE.SMS;
          }else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.RINGER_SILENT.toString())){
        	  mPendingNotificationType = NOTIFICATION_TYPE.RINGER_SILENT;
          } else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.LIGHT.toString())){
        	  mPendingNotificationType = NOTIFICATION_TYPE.LIGHT;
          } else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.LAUNCH_APPS.toString())){
        	  mPendingNotificationType = NOTIFICATION_TYPE.LAUNCH_APPS;
          } else {
        	  if (recipe.getTriggerAction() != null) {
        	      Log.e(LOG_TAG, "Unsupported notification type:" + recipe.getTriggerAction().getType());
        	  } else {
        		  Log.e(LOG_TAG, "Unsupported notification type");
        	  }
          }
          
          if(requestCode == ACTION_SELECTION && isNotificationTypeChanged){
          	Log.d(LOG_TAG, "Starting Animation");
            startAnimation(ibPlus2, triggerAnimation1);        	  
          }else{
        	switch(mPendingNotificationType){
        	case NOTIFICATION:
              ibPlus2.setImageResource(R.drawable.notification2);
              break;
        	case SMS:
                ibPlus2.setImageResource(R.drawable.sms2);
        		break;
        	case RINGER_SILENT:
              ibPlus2.setImageResource(R.drawable.silent2);
        		break;
        	case LIGHT:
              ibPlus2.setImageResource(R.drawable.ic_light);
        		break;
        	case LAUNCH_APPS:
        		try {
					ApplicationInfo app = getPackageManager().getApplicationInfo(recipe.getTriggerAction().getMessage(), PackageManager.GET_META_DATA);
					ibPlus2.setImageDrawable(app.loadIcon(getPackageManager()));
				} catch (NameNotFoundException e) {
					Log.e(LOG_TAG, "Appinfo not found for app:" + recipe.getTriggerAction().getMessage());
					ibPlus2.setImageResource(R.drawable.apps);
				}
        		break;
        	default:
        		break;
        	}
            ibPlus2.setBackgroundResource(R.drawable.image_border);
            displayActionName();
          }
        }
    } else {
      ibPlus1.setImageResource(R.drawable.ic_launcher);
      ibPlus1.setBackgroundResource(R.drawable.image_border);
      displayBeaconName();
      if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.NOTIFICATION.toString())){
        ibPlus2.setImageResource(R.drawable.notification2);
        ibPlus2.setBackgroundResource(R.drawable.image_border);
      }else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.SMS.toString())){
        ibPlus2.setImageResource(R.drawable.sms2);
        ibPlus2.setBackgroundResource(R.drawable.image_border);
      }else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.RINGER_SILENT.toString())){
        ibPlus2.setImageResource(R.drawable.silent2);
        ibPlus2.setBackgroundResource(R.drawable.image_border);            
      } else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.LIGHT.toString())){
        ibPlus2.setImageResource(R.drawable.ic_light);
        ibPlus2.setBackgroundResource(R.drawable.image_border);
      } else if(recipe.getTriggerAction().getType().equals(NOTIFICATION_TYPE.LAUNCH_APPS.toString())){
    	  try {
    		  ApplicationInfo app = getPackageManager().getApplicationInfo(recipe.getTriggerAction().getMessage(), PackageManager.GET_META_DATA);
    		  ibPlus2.setImageDrawable(app.loadIcon(getPackageManager()));
    	  } catch (NameNotFoundException e) {
    		  Log.e(LOG_TAG, "Appinfo not found for app:" + recipe.getTriggerAction().getMessage());
    		  ibPlus2.setImageResource(R.drawable.apps);
    	  }
          ibPlus2.setBackgroundResource(R.drawable.image_border);
      } else {
    	  Log.e(LOG_TAG, "Invalid Notification Type");
      }
      displayActionName();
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
      if(notif.equalsIgnoreCase(NOTIFICATION_TYPE.LIGHT.toString()))
          notif = "Turn on lights ";
      tvTriggerandNotification.setText(notif
              + " when " + recipe.getTrigger().toLowerCase() + " " + recipe.getDisplayName());
    }
  }
  
  private void displayBeaconName() {
	  if (recipe.getBeacon() != null && recipe.getBeacon().getName() != null)
	      tvSelectedBeacon.setText(recipe.getBeacon().getName());	  
  }
  
  private void displayActionName() {
	  if (recipe.getTriggerAction() != null && recipe.getTrigger() != null){
	      String notif = recipe.getTriggerActionDisplayName();
	      if(notif.equalsIgnoreCase(NOTIFICATION_TYPE.NOTIFICATION.toString()))
	          notif = "Notification";
	      if(notif.equalsIgnoreCase(NOTIFICATION_TYPE.RINGER_SILENT.toString()))
	        notif = "Ringer Silent";
	      tvSelectedAction.setText(notif + " on " + recipe.getTrigger().toLowerCase());
	    }	  
  }
  
  private void startAnimation(ImageView img, Animation newAnimation) {
	  img.clearAnimation();
	  img.setAnimation(newAnimation);
	  img.startAnimation(newAnimation);	  
  }
  
  public void onScanBeacon(View view) {
	  Intent scanIntent = new Intent(this, BlePagerActivity.class);
	  startActivityForResult(scanIntent, 0);
//	  overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
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
          showRecipe(-1);

        } else {
          // something went wrong
        }
      }
    });
  }

  public void onSaveAction(MenuItem mi) {
	  Log.d(LOG_TAG, "Checking if recipe already exists");
	  Log.d(LOG_TAG, "Recipe exists:" + RecipeManager.getInstance().recipeExists(recipe));
	  Log.d(LOG_TAG, "Recipe being edited:" + recipe.isBeingEdited());
    if (RecipeManager.getInstance().recipeExists(recipe)
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
      RecipeManager.getInstance().addNewRecipe(recipe);
    } else {
    	action = RecipeContracts.RECIPE_ACTION_UPDATE;
    	RecipeManager.getInstance().deleteRecipe(oldRecipe);
    	RecipeManager.getInstance().addNewRecipe(recipe);
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
    RecipeManager.getInstance().deleteRecipe(recipe);
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
    if (requestCode == BEACON_SELECTION) {
      if (resultCode == RESULT_OK) {
        BleDeviceInfo deviceInfo = (BleDeviceInfo) data
            .getParcelableExtra("beacon");
        Log.d(LOG_TAG, "Beacon id:" + deviceInfo.getObjectId());
        if ((createFlag && recipe.getBeacon() != null) || (oldRecipe != null
            && !oldRecipe.getBeacon().equals(recipe.getBeacon()))) {
          recipe.setEditState(true);
        }
        recipe.setBeacon(deviceInfo);
        recipe.setDisplayName(deviceInfo.getName());
        deviceInfo.setEditState(false);
        showRecipe(requestCode);
      }
    } else if (requestCode == ACTION_SELECTION) {
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
        showRecipe(requestCode);
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
  
  @Override
	public void onAnimationEnd(Animation animation) {
	  if (animation==beaconAnimation1) {
			  if (isBeaconPlusShowing) {
					  ibPlus1.setImageResource(R.drawable.ic_launcher);
					  ibPlus1.setBackgroundResource(R.drawable.image_border);
					  startAnimation(ibPlus1, beaconAnimation2);
			  } 
	  } else if (animation == beaconAnimation2) {
		  isBeaconPlusShowing=!isBeaconPlusShowing;
		  displayBeaconName();
	  } else if (animation == triggerAnimation1) {
			  switch(mPendingNotificationType) {
			  case NOTIFICATION :
				  ibPlus2.setImageResource(R.drawable.notification2);
				  break;
			  case SMS :
				  ibPlus2.setImageResource(R.drawable.sms2);
				  break;
			  case RINGER_SILENT :
				  ibPlus2.setImageResource(R.drawable.silent2);
				  break;
			  case LIGHT : 
				  ibPlus2.setImageResource(R.drawable.ic_light);
				  break;
			  case LAUNCH_APPS :
				  try {
		    		  ApplicationInfo app = getPackageManager().getApplicationInfo(recipe.getTriggerAction().getMessage(), PackageManager.GET_META_DATA);
		    		  ibPlus2.setImageDrawable(app.loadIcon(getPackageManager()));
		    	  } catch (NameNotFoundException e) {
		    		  Log.e(LOG_TAG, "Appinfo not found for app:" + recipe.getTriggerAction().getMessage());
		    		  ibPlus2.setImageResource(R.drawable.apps);
		    	  }
			      break;
			  default :
			  }
			  ibPlus2.setBackgroundResource(R.drawable.image_border);
			  startAnimation(ibPlus2, triggerAnimation2);
	  } else if (animation == triggerAnimation2) {
		  displayActionName();
	  } else {
		  Log.e(LOG_TAG, "Invalid Animation type");
	  }
 	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}
}
