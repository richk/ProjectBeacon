package com.codepath.beacon.fragments;

import com.codepath.beacon.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class RecipeAlertDialog extends DialogFragment {
	private static final String LOG_TAG = RecipeAlertDialog.class.getSimpleName();
	
	public RecipeAlertDialog(){}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        String message = args.getString("message");
        if (message == null) {
        	message = getString(R.string.defualt_recipe_alert_message);
        }
        builder.setMessage(message)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.dismiss();
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
