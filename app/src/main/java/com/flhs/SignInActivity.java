package com.flhs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Created by Theo Grossberndt on 5/17/17.
 */

public class SignInActivity extends Activity {
    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        makeStudentIDField();
        findViewById(R.id.signIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int studentID = readStudentID();
                String email = ((EditText)findViewById(R.id.email)).getText().toString();
                String password = ((EditText)findViewById(R.id.password)).getText().toString();
                //Handle sign in
                boolean goodSignin = studentID == 16154;
                if (!goodSignin){
                    //Create alert diolog for wrong password
                    Log.i("SignIn", "Bad sign in");
                } else {
                    Log.i("SignIn","Good sign in");
                    SharedPreferences.Editor editor = getSharedPreferences(
                            getResources().getString(R.string.signed_in_loc), 0).edit();
                    editor.putBoolean("success", true);
                    editor.apply();

                    SharedPreferences.Editor saveData = getSharedPreferences(
                            getResources().getString(R.string.student_info_loc), 0).edit();
                    saveData.putString("studentEmail", email);
                    saveData.putString("studentPassword", password);
                    saveData.putInt("studentID", studentID);
                    saveData.apply();

                    Intent goHome = new Intent(SignInActivity.this, HomeActivity.class);
                    goHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(goHome);
                }
            }
        });
    }

    public int readStudentID(){
        String studentIDRaw = "";
        for (int c=1; c<6; c++)
            studentIDRaw += ((EditText) findViewById(ids[c-1])).getText().toString();
        return Integer.parseInt(studentIDRaw);
    }

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
                    if(cont.length() == 1 && finalC < 4)
                        digits[finalC+1].requestFocus();
                    else if (cont.length() == 0 && before == 0 && finalC > 0)
                        digits[finalC-1].requestFocus();
                    else if (cont.length() == 0 && before != 0)
                        digits[finalC].setText("");
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after){}
                public void afterTextChanged(Editable s){}
            });
        }
    }

    int[] ids = {R.id.student_digit_6, R.id.student_digit_2, R.id.student_digit_3,
                R.id.student_digit_4, R.id.student_digit_5};
}
