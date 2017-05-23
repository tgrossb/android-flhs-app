package com.flhs.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Theo Grossberndt on 5/18/17.
 */

public class BadSignInFragment extends DialogFragment {
    private SignInCallback callback;
    public interface SignInCallback {
        void signInWithGoogle();
        void trySignInAgain();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Incorrect login")
                .setMessage("Incorrect email or password.\nMake sure to include '@student.bcsdny.org' in your email.")
                .setPositiveButton("Sign in with Google", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.signInWithGoogle();
                    }
                })
                .setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.trySignInAgain();
                    }
                });
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
