package com.lobxy.societyentrancesystem.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.societyentrancesystem.Model.User;
import com.lobxy.societyentrancesystem.R;

import java.io.ByteArrayOutputStream;

public class LoginActivity extends AppCompatActivity {

    public static final String QrImagePrefs = "QrImage";

    private EditText et_email, et_password;

    private String mEmail, mPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference mReference;

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        sharedpreferences = getSharedPreferences(QrImagePrefs, Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference("users");

        et_email = findViewById(R.id.login_email);
        et_password = findViewById(R.id.login_password);

        Button submit = findViewById(R.id.login_proceed);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });

    }

    private void validate() {
        mEmail = et_email.getText().toString().trim();
        mPassword = et_password.getText().toString().trim();

        if (mEmail.isEmpty()) {
            Toast.makeText(this, "Field is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mPassword.isEmpty()) {
            Toast.makeText(this, "Field is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        //login user.
        mAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    checkPreferences();


//                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                    finish();

                } else {
                    Log.i("User login", "onComplete: Failed: " + task.getException().getMessage());
                }

            }
        });
    }

    private void checkPreferences() {

        String qrImage = sharedpreferences.getString("qrImage", null);
        if (qrImage != null) {
            checkQrImage();
        } else {
            //save it into preferences.
            getQrImage();
        }

    }

    //Todo: figure things out here.

    private void checkQrImage() {

    }

    private void getQrImage() {
        //get image from firebase and save it into the device using shared preferences.

        final String oldQrImage = sharedpreferences.getString("qrImage", null);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userData");

        String uid = mAuth.getCurrentUser().getUid();

        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    //imageUrl
                    String newQrImage = user.getQrImageURL();
                    if (newQrImage.equals(oldQrImage)) {
                        //same user logged in.
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

                    } else {
                        //delete old image.
                        sharedpreferences.edit().remove("qrImage").commit();

                        //save new one.
                        //convert image into string.
                        //todo: convert firebase image into bitmap.

                        Bitmap image = BitmapFactory.decodeStream(stream);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] b = baos.toByteArray();

                        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

                        SharedPreferences.Editor edit = sharedpreferences.edit();
                        edit.putString("image_data", encodedImage);
                        edit.apply();

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("getQrImage", "onCancelled: databaseError: " + databaseError.getMessage());
            }
        });

    }

}
