package com.flhs.utils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
/*
import com.flhs.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
*/
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * Created by Theo Grossberndt on 5/26/17.
 */

public abstract class AccountInfo {
    public static String NOT_FOUND = null;

    public static String INFO_LOC = "account_info";
    public static String EMAIL_LOC = "email";
    public static String DISP_NAME_LOC = "dispName";
    public static String STUDENT_ID_LOC = "studentID";
    public static String IS_SIGNED_IN_LOC = "isSignedIn";


    public static boolean isSignedIn(Context context){
        GoogleSignInAccount previousAccount = GoogleSignIn.getLastSignedInAccount(context);
        return previousAccount != null;
//        return getPreferences(context).getBoolean(IS_SIGNED_IN_LOC, false);
    }

    public static void signIn(Context context, String email, String dispName, int studentID, Uri picLocation){
        save(context, IS_SIGNED_IN_LOC, true);
        saveEmail(context, email);
        saveDispName(context, dispName);
        saveStudentID(context, studentID);
        saveProfilePicture(context, picLocation);
    }

    public static String getEmail(Context context){
        return getPreferences(context).getString(EMAIL_LOC, NOT_FOUND);
    }

    public static void saveEmail(Context context, String email){
        save(context, EMAIL_LOC, email);
    }

    public static String getDispName(Context context){
        return getPreferences(context).getString(DISP_NAME_LOC, NOT_FOUND);
    }

    public static void saveDispName(Context context, String dispName){
        save(context, DISP_NAME_LOC, dispName);
    }

    public static Bitmap getProfilePicture(Context context){
        try {
            InputStream is = new FileInputStream(new File(getPictureLoc(context)));
            return BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e){
            Toast.makeText(context, "Image not found", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return null;
        }
    }

    public static void saveProfilePicture(Context context, Uri picLocation){
        if (picLocation == null) {
            try {
                URL url = new URL("https://lh3.googleusercontent.com/H9yAIsZYqbIOh_E1ON90chVhO6SYSD6ucV-" +
                        "XirZXkMFDqLRjGoztobaxx1XS9CB4lfg=w300");
                Uri.Builder builder = new Uri.Builder()
                        .scheme(url.getProtocol())
                        .authority(url.getAuthority())
                        .appendPath(url.getPath());
                picLocation = builder.build();
            } catch (MalformedURLException e){
                e.printStackTrace();
            }
        }
        new DownloadUriTask(context).execute(picLocation);
    }

    public static int getStudentID(Context context){
        return getPreferences(context).getInt(STUDENT_ID_LOC, -1);
    }

    public static void saveStudentID(Context context, int studentID){
        save(context, STUDENT_ID_LOC, studentID);
    }

    /*
    public static LinearLayout getBarcode(Context context) {
        if (getStudentID(context) == -1)
            return null;
        String barcodeContent = Integer.toString(getStudentID(context));
        LinearLayout barcodeSubPan = new LinearLayout(context);
        barcodeSubPan.setOrientation(LinearLayout.VERTICAL);

        ImageView barcode = new ImageView(context);
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(barcodeContent);
        if (encoding != null) {
            hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = null;
        try {
            result = writer.encode(barcodeContent, BarcodeFormat.CODE_128, 600, 300, hints);
        } catch (WriterException e){
            e.printStackTrace();
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++)
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
        }

        Bitmap barcodeSource = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        barcodeSource.setPixels(pixels, 0, width, 0, 0, width, height);
        barcode.setImageBitmap(barcodeSource);

        TextView barcodeContentDisplay = new TextView(context);
        barcodeContentDisplay.setText(barcodeContent);
        barcodeContentDisplay.setGravity(Gravity.CENTER_HORIZONTAL);

        barcodeSubPan.addView(barcode);
        barcodeSubPan.addView(barcodeContentDisplay);
        return barcodeSubPan;
    }
    */

    private static String guessAppropriateEncoding(CharSequence contents) {
        for (int c = 0; c < contents.length(); c++)
            if (contents.charAt(c) > 0xFF)
                return "UTF-8";
        return null;
    }

    public static String getPictureLoc(Context context){
        return context.getFilesDir() + "/profile_image.png";
    }

    public static SharedPreferences getPreferences(Context context){
        return context.getSharedPreferences(INFO_LOC, 0);
    }

    public static SharedPreferences.Editor getEditor(Context context){
        return getPreferences(context).edit();
    }

    public static void save(Context context, String tag, String content){
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(tag, content);
        editor.apply();
    }

    public static void save(Context context, String tag, int content){
        SharedPreferences.Editor editor = getEditor(context);
        editor.putInt(tag, content);
        editor.apply();
    }

    public static void save(Context context, String tag, boolean content){
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(tag, content);
        editor.apply();
    }
}

//This cannot be an inner class because saveProfilePicture is in a static context
class DownloadUriTask extends AsyncTask<Uri, Integer, Long> {
    private Context context;

    public DownloadUriTask(Context context){
        this.context = context;
    }

    protected Long doInBackground(Uri... uris){
        if (uris.length != 1)
            return new Long(0);
        Bitmap pic = null;
        try {
            URL url = new URL(uris[0].toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            pic = BitmapFactory.decodeStream(input);
        } catch (IOException e){
            e.printStackTrace();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(AccountInfo.getPictureLoc(context));
            pic.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Long(0);
    }

    protected void onProgressUpdate(Integer... progress){}
    protected void onPostExecute(Long res){
        Toast.makeText(context, "User image has been downloaded", Toast.LENGTH_LONG).show();
    }
}