package com.lobxy.societyentrancesystem.User;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lobxy.societyentrancesystem.Model.User;
import com.lobxy.societyentrancesystem.R;

public class AuthActivity extends AppCompatActivity {
    //get fingerprint auth done here.
    //if fingerprint can't be done, show them to put the pincode here.
    //return response and handle it accordingly in the main activity.

    private EditText mEditPassword;
    private String mPassword = "", mCorrectPassword = "";

    private FingerprintManager mFingerPrintManager;
    private KeyguardManager mKeyguardManager;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mFingerPrintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        Button cancel = findViewById(R.id.auth_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cancel function and return.
                returnToPreviousActivity("cancelled", false);
            }
        });

        mEditPassword = findViewById(R.id.auth_password);
        mEditPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    //call function
                    authenticateByPassword();
                    handled = true;
                }
                return handled;
            }
        });

        checkConditions();
    }


    public void checkConditions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!mFingerPrintManager.isHardwareDetected()) {
                Toast.makeText(AuthActivity.this, "Finger print scanner not detected", Toast.LENGTH_SHORT).show();
                authenticateByPassword();

            } else if (ContextCompat.checkSelfPermission(AuthActivity.this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AuthActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                authenticateByPassword();

            } else if (!mKeyguardManager.isKeyguardSecure()) {
                Toast.makeText(AuthActivity.this, "Add lock to your phone", Toast.LENGTH_SHORT).show();
                authenticateByPassword();

            } else if (!mFingerPrintManager.hasEnrolledFingerprints()) {
                Toast.makeText(AuthActivity.this, "Fingerprint not added", Toast.LENGTH_SHORT).show();
                authenticateByPassword();

            } else {
                //access fingerprint.
                accessFingerPrint();
            }

        } else {
            //if android version is less than marshmallow
            authenticateByPassword();
        }
    }

    private void accessFingerPrint() {
        FingerprintHandler fingerprintHandler = new FingerprintHandler(this);
        fingerprintHandler.startAuth(mFingerPrintManager, null);
    }

    private void authenticateByPassword() {
        mPassword = mEditPassword.getText().toString().trim();
        if (mPassword.isEmpty()) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
        } else {
            //To minimize internet usage, save the data on first request to the database and
            //later check for the correctness of the entered password with the saved password

            if (mCorrectPassword.isEmpty()) {
                //check for correctness and if true, pass SUCCESS else pass FAILED.
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("userData");
                reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            mCorrectPassword = user.getPincode();

                            if (mCorrectPassword.equals(mPassword)) {
                                returnToPreviousActivity("Success", false);
                            } else {
                                Toast.makeText(AuthActivity.this, "Password incorrect", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //no user data present.login again.
                            startActivity(new Intent(AuthActivity.this, LoginActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        returnToPreviousActivity(databaseError.getMessage(), true);
                    }
                });

            } else {
                if (mCorrectPassword.equals(mPassword)) {
                    returnToPreviousActivity("Success", false);
                } else {
                    Toast.makeText(AuthActivity.this, "Password incorrect", Toast.LENGTH_SHORT).show();
                }
            }

        }


    }

    private void returnToPreviousActivity(String message, Boolean errorPresent) {
        Intent intent = getIntent();
        intent.putExtra("Errors", errorPresent);
        intent.putExtra("message", message);
        setResult(RESULT_OK, intent);
        finish();
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @TargetApi(Build.VERSION_CODES.M)
    class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
        Context context;

        public FingerprintHandler(Context context) {
            this.context = context;
        }

        public void startAuth(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {
            CancellationSignal cancellationSignal = new CancellationSignal();
            fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            returnToPreviousActivity("Error: " + errString, true);
        }


        @Override
        public void onAuthenticationFailed() {
            returnToPreviousActivity("Auth Failed", true);
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            returnToPreviousActivity(helpString.toString(), true);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            returnToPreviousActivity("Success", false);
        }

        //EOC
    }

    //EOMC
}
