package com.lobxy.societyentrancesystem.Admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.lobxy.societyentrancesystem.User.LoginActivity;
import com.lobxy.societyentrancesystem.Model.User;
import com.lobxy.societyentrancesystem.R;
import com.lobxy.societyentrancesystem.Utils.Connection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class AddUserActivity extends AppCompatActivity {

    private static final int WIDTH = 700;
    private static final int HEIGHT = 700;
    private static final String TAG = "AdminAddUser";

    private EditText edit_name, edit_contact, edit_email, edit_password, edit_flat, edit_block, edit_pincode;

    private String mName, mContact, mEmail, mPassword, mFlat, mBlock, mQRImageUrl, mUid, mPincode;

    private DatabaseReference mReference;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    private ProgressBar bar;

    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        connection = new Connection(this);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference("userData");

        Button submit = findViewById(R.id.addUser_submit);

        edit_name = findViewById(R.id.addUser_name);
        edit_contact = findViewById(R.id.addUser_contact);
        edit_email = findViewById(R.id.addUser_email);
        edit_password = findViewById(R.id.addUser_password);
        edit_flat = findViewById(R.id.addUser_flat);
        edit_block = findViewById(R.id.addUser_block);
        edit_pincode = findViewById(R.id.addUser_pin);

        bar = findViewById(R.id.addUser_bar);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });

    }

    private void validate() {

        mName = edit_name.getText().toString().trim();
        mContact = edit_contact.getText().toString().trim();
        mEmail = edit_email.getText().toString().trim();
        mPassword = edit_password.getText().toString().trim();
        mFlat = edit_flat.getText().toString().trim();
        mBlock = edit_block.getText().toString().trim();
        mPincode = edit_pincode.getText().toString().trim();

        if (mName.isEmpty() || mContact.isEmpty() || mEmail.isEmpty() || mPassword.isEmpty() || mFlat.isEmpty() || mBlock.isEmpty()) {

            Toast.makeText(this, "Field empty", Toast.LENGTH_SHORT).show();

        } else if (mPassword.length() < 6) {

            Toast.makeText(this, "Password size less than 6 characters", Toast.LENGTH_SHORT).show();

        } else if (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {

            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();

        } else if (mPincode.length() < 4) {

            Toast.makeText(this, "Pincode must be 4 digit long", Toast.LENGTH_SHORT).show();

        } else if (!connection.check()) {

            Toast.makeText(this, "Internet service unavailable", Toast.LENGTH_SHORT).show();

        } else {
            Log.i("Process", "validated");
            signInUser();
        }

    }

    private void signInUser() {

        bar.setVisibility(View.VISIBLE);

        FirebaseAuth mAuth2;

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://societyentrancesystem.firebaseio.com/")
                .setApiKey("AIzaSyC85St-pvg1WrSI00DdAtC1bF0FB65FfWo")
                .setApplicationId("societyentrancesystem").build();

        try {
            FirebaseApp myApp = FirebaseApp.initializeApp(getApplicationContext(), firebaseOptions, "SocietyEntranceSystem");
            mAuth2 = FirebaseAuth.getInstance(myApp);
        } catch (IllegalStateException e) {
            mAuth2 = FirebaseAuth.getInstance(FirebaseApp.getInstance("SocietyEntranceSystem"));
        }

        mAuth2.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                bar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    Log.i("Process", "User Created");

                    mUid = task.getResult().getUser().getUid();

                    createJsonData();
                } else {
                    Toast.makeText(AddUserActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        mAuth2.signOut();
    }

    public void createJsonData() {

        bar.setVisibility(View.VISIBLE);

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("uid", mUid);
            jsonObject.put("name", mName);
            jsonObject.put("contact", mContact);
            jsonObject.put("email", mEmail);
            jsonObject.put("flat", mFlat);
            jsonObject.put("block", mBlock);

            bar.setVisibility(View.INVISIBLE);
            Log.i("Process", "json data created");

            createQrCode(jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createQrCode(String jsonData) {
        bar.setVisibility(View.VISIBLE);
        //encode data.
        byte[] encodeValue = Base64.encode(jsonData.getBytes(), Base64.DEFAULT);
        String data = new String(encodeValue);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {

            BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            //Preparing data for firebase storage
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] img = baos.toByteArray();
            bar.setVisibility(View.INVISIBLE);

            Log.i("QR Generator", "Qr code Created");

            uploadImg(img);


        } catch (WriterException e) {
            bar.setVisibility(View.INVISIBLE);

            e.printStackTrace();
        }

    }

    private void uploadImg(byte[] img) {
        bar.setVisibility(View.VISIBLE);

        final StorageReference filepath = mStorageRef.child("QRImages").child(mEmail);

        filepath.putBytes(img).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                bar.setVisibility(View.INVISIBLE);

                Log.i("Save Image", "onSuccess: Upload success");

                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mQRImageUrl = uri.toString();
                        Log.i("getDownloadImage", "download url: " + mQRImageUrl);

                        saveUserData();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("getDownloadImage", "onFailure: Download url error: " + e.getLocalizedMessage());
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                bar.setVisibility(View.INVISIBLE);
                Log.i("saveImage", "onFailure: error: " + e.getMessage());

                //ROLLBACK
                rollback_deleteUser();
            }
        });
    }

    private void saveUserData() {
        bar.setVisibility(View.VISIBLE);

        User user = new User(mUid, mName, mEmail, mPassword, mContact, mFlat, mBlock, mQRImageUrl, mPincode);

        mReference.child(mUid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                bar.setVisibility(View.INVISIBLE);

                if (task.isSuccessful()) {
                    Log.d("Process", "User data saved");

                    Toast.makeText(AddUserActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddUserActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                    Log.i("saveUserData", "Error: " + task.getException().getMessage());

                    //ROLLBACK
                    rollback_deleteImage();
                    rollback_deleteUser();
                }
            }
        });

    }

    private void rollback_deleteImage() {

        StorageReference ref = mStorageRef.child("QrImages").child(mEmail);

        // Delete the file
        ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity(new Intent(AddUserActivity.this, LoginActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }

    private void rollback_deleteUser() {

        FirebaseAuth mAuth2;

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://societyentrancesystem.firebaseio.com/")
                .setApiKey("AIzaSyC85St-pvg1WrSI00DdAtC1bF0FB65FfWo")
                .setApplicationId("societyentrancesystem").build();

        try {
            FirebaseApp myApp = FirebaseApp.initializeApp(getApplicationContext(), firebaseOptions, "SocietyEntranceSystem");
            mAuth2 = FirebaseAuth.getInstance(myApp);
        } catch (IllegalStateException e) {
            mAuth2 = FirebaseAuth.getInstance(FirebaseApp.getInstance("SocietyEntranceSystem"));
        }

        FirebaseUser currentUser = mAuth2.getCurrentUser();

        AuthCredential authCredential = EmailAuthProvider.getCredential(mEmail, mPassword);
        currentUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "onComplete: User deleted ");
                } else {
                    Log.i(TAG, "onComplete: user deleting error: " + task.getException().getLocalizedMessage());
                    Toast.makeText(AddUserActivity.this, "Error Occured while deleting user", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}