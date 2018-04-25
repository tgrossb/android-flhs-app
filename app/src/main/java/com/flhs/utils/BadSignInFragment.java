package com.flhs.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.flhs.SignInActivity;

/**
 * Created by Theo Grossberndt on 5/18/17.
 */

public class BadSignInFragment extends DialogFragment {
    public static int BAD_GOOGLE_SIGN_IN = 0;
    public static int BAD_EMAIL_SIGN_IN = 1;
    private int cause;
    private SignInCallback callback;
    public interface SignInCallback {
        void signInWithGoogle();
        void trySignInAgain();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstancesState){
        cause = getArguments().getInt("cause", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //First check that the user is connected to the internet
        if (!SignInActivity.isConnectedToInternet(getActivity())){
            String signInMethod = cause == 0 ? "Google" : "Email";
            builder.setTitle("Login Error")
                    .setMessage("You are not connected to the internet.\nCheck your internet connection settings and " +
                            "try again.")
                    .setPositiveButton("Check Internet Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    })
                    .setNegativeButton("Sign in with " + signInMethod, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i){
                            if (cause == 0)
                                callback.signInWithGoogle();
                            if (cause == 1)
                                callback.trySignInAgain();
                        }
                    });
            return builder.create();
        }
        if (cause == BAD_EMAIL_SIGN_IN) {
            builder.setTitle("Login Error")
                    .setMessage("Incorrect email or password.\nMake sure to include '@student.bcsdny.org' in your email.")
                    .setPositiveButton("Sign in with Google", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            callback.signInWithGoogle();
                        }
                    })
                    .setNegativeButton("Sign in with Email", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            callback.trySignInAgain();
                        }
                    });
        } else if (cause == BAD_GOOGLE_SIGN_IN){
            builder.setTitle("Login Error")
                    .setMessage("Google sign in has encoutered a problem.  Please try again and if the problem" +
                            " persistes try signing in with your email.")
                    .setPositiveButton("Sign in with Google", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            callback.signInWithGoogle();
                        }
                    })
                    .setNegativeButton("Sign in with Email", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            callback.trySignInAgain();
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

