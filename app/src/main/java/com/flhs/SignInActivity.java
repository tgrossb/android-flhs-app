package com.flhs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.flhs.utils.BadSignInFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Theo Grossberndt on 5/17/17.
 */

public class SignInActivity extends Activity implements BadSignInFragment.SignInCallback {
    private GoogleApiClient googleClient;

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
//        setTheme(R.style.AppCompatTheme);
        setContentView(R.layout.sign_in);
        findViewById(R.id.signIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = ((EditText) findViewById(R.id.email)).getText().toString();
                String password = ((EditText) findViewById(R.id.password)).getText().toString();
                handleSignIn(email.equals("no") && password.equals("pass"), email);
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
               .requestEmail()
               .build();
        googleClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//                        Log.i("Connection", "No connection");
//                    }
//                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        findViewById(R.id.signInWithGoogle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
    }

    public void signInWithGoogle(){
        Intent startSignIn = Auth.GoogleSignInApi.getSignInIntent(googleClient);
        startActivityForResult(startSignIn, 1738 /* Ay, I'm like hey whats up hello */);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1738){
            GoogleSignInResult res = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount account = res.getSignInAccount();
            handleSignIn(res.isSuccess(), account.getEmail());
        }
    }

    public void trySignInAgain() {
        ((EditText) findViewById(R.id.password)).setText("");
    }

    public void handleSignIn(boolean goodSignIn, String email) {
        //Handle sign in
        if (!goodSignIn) {
            BadSignInFragment badSignIn = new BadSignInFragment();
            badSignIn.show(getFragmentManager(), "BadSignIn");
        } else {
            Log.i("AHHHHHHH", "Good sign in with an email " + email);
            SharedPreferences.Editor editor = getSharedPreferences(
                    getResources().getString(R.string.signed_in_loc), 0).edit();
            editor.putBoolean("success", true);
            editor.apply();

            SharedPreferences.Editor saveData = getSharedPreferences(
                    getResources().getString(R.string.student_info_loc), 0).edit();
            saveData.putString("studentEmail", email);
            saveData.apply();

            Intent goHome = new Intent(SignInActivity.this, HomeActivity.class);
            goHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(goHome);
        }
    }
/**
    Unfortunately, this is not really needed now. But it's so beautiful that I want to keep it.

    public void makeStudentIDField(){
        final LinearLayout idHolder = (LinearLayout) findViewById(R.id.idHolder);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final EditText[] digits = new EditText[5];
        for (int c=1; c<6; c++) {
            final EditText digit = new EditText(this);
            digit.setInputType(InputType.TYPE_CLASS_NUMBER);
            digit.setLayoutParams(params);
            digit.setId(ids[c-1]);
            digits[c-1] = digit;
            idHolder.addView(digit);
        }
        for (int c=0; c<5; c++){
            final int finalC = c;
            digits[c].addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start,int before, int count){
                    String cont = digits[finalC].getText().toString();
                    if(cont.length() >= 1 && finalC < 4)
                        digits[finalC+1].requestFocus();
                    else if (cont.length() == 0 && before != 0) {
                        digits[finalC].setText("");
                        Log.i("yo", "delete with before  = " + before + " (should be 0) from c=" + finalC);
                    }
                    else if (cont.length() == 0 && before == 0 && finalC > 0) {
                        digits[finalC-1].requestFocus();
                        Log.i("yo", "delete with before = " + before + " (shouldnt be 0) from c=" + finalC);
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after){}
                public void afterTextChanged(Editable s){}
            });
        }
    }

    int[] ids = {R.id.student_digit_6, R.id.student_digit_2, R.id.student_digit_3,
                R.id.student_digit_4, R.id.student_digit_5};
*/
 }
