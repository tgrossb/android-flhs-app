
package com.flhs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.flhs.utils.AccountInfo;
import com.flhs.utils.BadSignInFragment;
import com.flhs.home.HomeActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Created by Theo Grossberndt on 5/17/17
 */

public class SignInActivity extends AppCompatActivity implements BadSignInFragment.SignInCallback {
    private GoogleApiClient googleClient;
    private GoogleSignInClient signInClient;

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppCompatTheme);
        setContentView(R.layout.activity_sign_in);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestProfile()
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        handleSignIn(account);
/*        googleClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getApplicationContext(), "No connection, sign in failed", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
  */      findViewById(R.id.signInWithGoogle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
    }

    public void signInWithGoogle(){
        if (!isConnectedToInternet(this)){
            BadSignInFragment badSignInFragment = new BadSignInFragment();
            Bundle args = new Bundle();
            args.putInt("cause", BadSignInFragment.BAD_GOOGLE_SIGN_IN);
            badSignInFragment.setArguments(args);
            Toast.makeText(this, "Not connected, showing frag", Toast.LENGTH_LONG).show();
            badSignInFragment.show(getFragmentManager(), "BadSignIn");
        } else {
//            googleClient.clearDefaultAccountAndReconnect();
//            Intent startSignIn = Auth.GoogleSignInApi.getSignInIntent(googleClient);
            Intent startSignIn = signInClient.getSignInIntent();
            startActivityForResult(startSignIn, 1738 /* Ay, I'm like hey whats up hello */);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1738) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            recievedSignIn(task);
            /*
            if (resultCode == RESULT_OK) {
//                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                GoogleSignInAccount account = task.getResult(ApiException.class);
                GoogleSignInResult res = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                GoogleSignInAccount account = res.getSignInAccount();
                String email = "this should not be shown";
                String dispName = "nor should this";
                Uri picURI = null;
                try {
                    email = account.getEmail();
//                    dispName = account.getDisplayName();
//                    picURI = account.getPhotoUrl();
                } catch (NullPointerException e) {
                    Log.i("Uh", "Null pointer getting email, disp name, and picURI.  Here's trace:");
                    e.printStackTrace();
                }
                handleSignIn(res.isSuccess(), BadSignInFragment.BAD_GOOGLE_SIGN_IN, email, dispName, picURI);
//                googleClient.clearDefaultAccountAndReconnect();
            } else {
                Toast.makeText(getApplicationContext(), "Sign in fragment not successful, " + resultCode, Toast.LENGTH_LONG).show();
            }
*/
        }
    }

    public void trySignInAgain() {
        ((EditText) findViewById(R.id.password)).setText("");
    }

    public void recievedSignIn(Task<GoogleSignInAccount> recieved){
        try {
            GoogleSignInAccount account = recieved.getResult(ApiException.class);
            handleSignIn(account);
        } catch (ApiException e){
            e.printStackTrace();
            BadSignInFragment badSignIn = new BadSignInFragment();
            Bundle args = new Bundle();
            args.putInt("cause", e.getStatusCode());
            badSignIn.setArguments(args);
            badSignIn.show(getFragmentManager(), "BadSignIn");
        }
    }

    public void handleSignIn(GoogleSignInAccount account) {
        AccountInfo.signIn(this, account.getEmail(), account.getDisplayName(), 16154, account.getPhotoUrl());

        Intent goHome = new Intent(SignInActivity.this, HomeActivity.class);
        goHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goHome);
    }



    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connectivityManager .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return (wifi.isAvailable() && wifi.isConnectedOrConnecting() ||
                (mobile.isAvailable() && mobile.isConnectedOrConnecting()));
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