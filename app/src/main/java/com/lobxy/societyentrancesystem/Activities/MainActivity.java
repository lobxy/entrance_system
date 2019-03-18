package com.lobxy.societyentrancesystem.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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

        showImage();

        Button button = findViewById(R.id.user_main_readCode);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ReaderActivity.class));

            }
        });
    }

    private void showImage() {
        //convert saved string into image and set it.

        String previouslyEncodedImage = sharedPreferences.getString("qrImage", null);

        if (previouslyEncodedImage.isEmpty()) {
            Toast.makeText(this, "Image not present", Toast.LENGTH_SHORT).show();
            return;
        } else {
            byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
            Log.i("User", "showImage: bytes: " + b.toString());
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);

            mQRImage.setImageBitmap(bitmap);
        }
    }
}
