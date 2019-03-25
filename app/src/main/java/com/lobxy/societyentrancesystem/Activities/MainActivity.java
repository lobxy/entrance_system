package com.lobxy.societyentrancesystem.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

        getPin();
        showImage();

        Button button = findViewById(R.id.user_main_readCode);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ReaderActivity.class));
            }
        });
    }


    private void getPin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Pin")
                .setView(R.layout.alert_get_pin);

        final EditText edit_pin = findViewById(R.id.alert_edit_pin);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String pin = edit_pin.getText().toString().trim();
                if (pin.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter Pin", Toast.LENGTH_SHORT).show();
                } else {
                    dialogInterface.dismiss();
                    checkPin(pin);
                }

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void checkPin(String inputPin) {
        String savedPin = sharedPreferences.getString("Pin", null);
        if (savedPin != null) {

            if (savedPin.equals(inputPin)) showImage();
            else Toast.makeText(this, "Invalid Pin", Toast.LENGTH_SHORT).show();

        } else Toast.makeText(this, "Pin not found!", Toast.LENGTH_SHORT).show();

    }

    private void showImage() {
        //convert saved string into image and set it.

        String previouslyEncodedImage = sharedPreferences.getString("qrImage", null);

        if (previouslyEncodedImage == null) {
            Toast.makeText(this, "Image not present", Toast.LENGTH_SHORT).show();
            return;
        } else {
            byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
            Log.i("User", "showImage: bytes: " + b.toString());
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            mQRImage.setVisibility(View.VISIBLE);
            mQRImage.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mQRImage.setVisibility(View.INVISIBLE);
        getPin();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mQRImage.setVisibility(View.INVISIBLE);
    }

    //EOC
}
