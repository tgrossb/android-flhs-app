package com.flhs.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.flhs.FLHSActivity;


/**
 * Created by Theo Grossberndt on 5/18/17.
 */

public class BadSignInFragment extends DialogFragment {
    public static int BAD_GOOGLE_SIGN_IN = 0;
    private SignInCallback callback;
    public interface SignInCallback {
        void signInWithGoogle();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstancesState){
        int cause = getArguments().getInt("cause", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //First check that the user is connected to the internet
        if (!FLHSActivity.isOnline(getActivity())){
            builder.setTitle("Login Error")
                    .setMessage("You are not connected to the internet.\nCheck your internet connection settings and " +
                            "try again.")
                    .setPositiveButton("Check Internet Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    })
                    .setNegativeButton("Sign in with Google", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i){
                            callback.signInWithGoogle();
                        }
                    });
            return builder.create();
        }
        if (cause == BAD_GOOGLE_SIGN_IN){
            builder.setTitle("Login Error")
                    .setMessage("Google sign in has encountered a problem.  Please try again and if the problem" +
                            " persists please contact our team.")
                    .setPositiveButton("Sign in with Google", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            callback.signInWithGoogle();
                        }
                    });
        }
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (SignInCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SignInCallback");
        }
    }
}

