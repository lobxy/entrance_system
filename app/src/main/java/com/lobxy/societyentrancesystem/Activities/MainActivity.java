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

    private ImageView mQRImage;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(QrImagePrefs, Context.MODE_PRIVATE);

        mQRImage = findViewById(R.id.user_main_qrImage);

        Button btn_showImage = findViewById(R.id.user_main_showQR);
        btn_showImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage();
            }
        });
    }

    private void showImage() {
        //convert saved string into image and set image.

        String previouslyEncodedImage = sharedPreferences.getString("qrImage", null);

        if (!previouslyEncodedImage.equals("")) {
            byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);

            mQRImage.setImageBitmap(bitmap);
        }
    }
}
