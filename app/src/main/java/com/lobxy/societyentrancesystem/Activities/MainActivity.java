package com.lobxy.societyentrancesystem.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.lobxy.societyentrancesystem.R;

public class MainActivity extends AppCompatActivity {

    public static final String QrImagePrefs = "QrImage";

    /*
     * Or every time a user logs in, check the shared pref of the image which is saved by the name of
      user id, if uid(old) and new are same, show same qr image, if different, get new image and save qr image.
     * */

    private ImageView mQRImage;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(QrImagePrefs, Context.MODE_PRIVATE);

        Button btn_showImage = findViewById(R.id.user_main_showQR);
        btn_showImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage();
            }
        });
    }

    private void showImage() {
        //get and set image here.


        //convert string into image.
        String previouslyEncodedImage = sharedPreferences.getString("qrImage", null);

        if (!previouslyEncodedImage.equals("")) {
            byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            mQRImage.setImageBitmap(bitmap);
        }
    }
}
