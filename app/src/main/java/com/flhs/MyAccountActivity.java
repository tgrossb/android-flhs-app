package com.flhs;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flhs.utils.AccountInfo;
import com.flhs.utils.CircleImageView;

/**
 * Created by Theo Grossberndt on 5/26/17.
 */

public class MyAccountActivity extends FLHSActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        SetupNavDrawer();

        ((CircleImageView) findViewById(R.id.user_profile_photo)).setImageBitmap(AccountInfo.getProfilePicture(this));
        ((TextView) findViewById(R.id.user_profile_name)).setText(AccountInfo.getDispName(this));
        ((TextView) findViewById(R.id.user_profile_eamil)).setText(AccountInfo.getEmail(this));
        ((LinearLayout) findViewById(R.id.cardHolder)).addView(makeCard());
    }

    public LinearLayout makeCard(){
        if (AccountInfo.getStudentID(this) != -1)
            return AccountInfo.getBarcode(this);
        LinearLayout options = new LinearLayout(this);
        options.setOrientation(LinearLayout.VERTICAL);
        Button inputID = new Button(this);
        inputID.setText("Input Student ID");
        Button scanID = new Button(this);
        scanID.setText("Scan Student ID Card");
        inputID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        scanID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        options.addView(inputID);
        options.addView(scanID);
        return null;
    }
}
