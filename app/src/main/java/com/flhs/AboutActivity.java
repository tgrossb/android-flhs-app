/* Written by Drew Gregory: 2/20/2014
*/
package com.flhs;

import android.os.Bundle;
import android.widget.Button;

import com.flhs.activity.FLHSActivity;

public class AboutActivity extends FLHSActivity {
    Button b1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        SetupNavDrawer();

    }
}
