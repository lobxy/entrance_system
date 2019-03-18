package com.lobxy.societyentrancesystem.Activities;

import android.app.ProgressDialog;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lobxy.societyentrancesystem.Admin.AdminMainActivity;
import com.lobxy.societyentrancesystem.Model.User;
import com.lobxy.societyentrancesystem.R;
import com.lobxy.societyentrancesystem.Utils.Connection;

import java.io.ByteArrayOutputStream;

public class LoginActivity extends AppCompatActivity {

    /*
     * Every time a user logs in, check the shared pref of the image which is saved by the name of
      user id, if uid(old) and new are same, show same qr image, if different, replace old with new image.
     * */

    public static final String QrImagePrefs = "QrImage";

    private EditText et_email, et_password;

    private String mEmail, mPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference mReference;

    SharedPreferences sharedpreferences;

    private ProgressDialog progressDialog;

    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        connection = new Connection(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Working...");
        progressDialog.setInverseBackgroundForced(false);
        progressDialog.setCancelable(false);

        sharedpreferences = getSharedPreferences(QrImagePrefs, Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference("userData");

        et_email = findViewById(R.id.login_email);
        et_password = findViewById(R.id.login_password);

        Button submit = findViewById(R.id.login_proceed);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connection.check()) validate();
                else {
                    Toast.makeText(LoginActivity.this, "Not connected to internet", Toast.LENGTH_SHORT).show();
                }
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
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    if (mEmail.equals("admin@a.com")) {
                        startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                        finish();
                    } else {
                    }
                    getImageFromDatabase();

                }
                else Log.i("User login", "onComplete: Failed: " + task.getException().getMessage());

            }
        });
    }

    private void getImageFromDatabase() {

        progressDialog.show();

        final String oldQrImage = sharedpreferences.getString("qrImage", null);

        if (oldQrImage == null) {
            //get image and save it.
            saveImage();
        } else {
            //image present, compare between images.

            String uid = mAuth.getCurrentUser().getUid();

            mReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    progressDialog.dismiss();
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);

                        //image download url
                        String newQrImage = user.getQrImageURL();

                        //compare between images.
                        compareImages(oldQrImage, newQrImage);

                    } else {
                        Toast.makeText(LoginActivity.this, "Data missing", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("getQrImage", "onCancelled: databaseError: " + databaseError.getMessage());
                }
            });
        }

    }

    private void compareImages(String oldQrImage, String newQrImage) {

        if (newQrImage.equals(oldQrImage)) {
            //same user logged in.
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();

        } else {
            //delete old image.
            sharedpreferences.edit().remove("qrImage").commit();
            //get and save new image.
            saveImage();
        }
    }

    private void saveImage() {
        //Download image and convert it into string and save it into prefs.

        //todo: convert firebase image into bitmap.

        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference filepath = storageRef.child("QRImages").child(mEmail);

        final long ONE_MEGABYTE = 1024 * 1024;

        filepath.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                //converting image into encoded string.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] b = baos.toByteArray();

                String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

                Log.i("Image2String", "onSuccess: encodedImage: " + encodedImage);

                //saving image into shared preferences.
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("qrImage", encodedImage);
                edit.apply();

                progressDialog.dismiss();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });

    }

    //EOC
}
