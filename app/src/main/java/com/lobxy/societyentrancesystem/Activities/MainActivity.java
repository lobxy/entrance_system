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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.lobxy.societyentrancesystem.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main Screen";

    public static final String QrImagePrefs = "UserData";

    private ImageView mQRImage;

    private FirebaseAuth mAuth;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        sharedPreferences = getSharedPreferences(QrImagePrefs, Context.MODE_PRIVATE);

        mQRImage = findViewById(R.id.user_main_qrImage);

        Button button = findViewById(R.id.user_main_show_image);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage();
            }
        });
    }

    private void showImage() {
        String previouslyEncodedImage = sharedPreferences.getString("qrImage", null);

        if (previouslyEncodedImage == null) {
            Log.i(TAG, "showImage: Image not present");

            //Maybe user data was deleted by admin,maybe image not downloaded properly,etc....
            //Check auth (Won't be able to login again)and check for image again(image will be properly downloaded).

            mAuth.signOut();

            Toast.makeText(this, "Login again", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            //convert saved image (string format) to the image and show it.

            byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
            Log.i("User", "showImage: bytes: " + b.toString());
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            mQRImage.setVisibility(View.VISIBLE);
            mQRImage.setImageBitmap(bitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_admin_main_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    //    @Override
//    protected void onRestart() {
//        super.onRestart();
//        mQRImage.setVisibility(View.INVISIBLE);
//        //getPin();
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        mQRImage.setVisibility(View.INVISIBLE);
//    }

    //  private void getPin() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Enter Pin");
//
//        final EditText input = new EditText(MainActivity.this);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        input.setLayoutParams(lp);
//
//        builder.setView(input);
//
//        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                showImage();
//               // String pin = input.getText().toString().trim();
//                if (pin.isEmpty()) {
//                    Toast.makeText(MainActivity.this, "Enter Pin", Toast.LENGTH_SHORT).show();
//                } else {
//                    dialogInterface.dismiss();
//                    //checkPin(pin);
//                }
//            }
//        });
//
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//    }

//    private void checkPin(String inputPin) {
//        String savedPin = sharedPreferences.getString("Pin", "");
//        if (!savedPin.isEmpty()) {
//
//            if (savedPin.equals(inputPin)) showImage();
//            else Toast.makeText(this, "Invalid Pin", Toast.LENGTH_SHORT).show();
//
//        } else Toast.makeText(this, "Pin not found!", Toast.LENGTH_SHORT).show();
//
//    }

    //EOC
}
