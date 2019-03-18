package com.lobxy.societyentrancesystem.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lobxy.societyentrancesystem.Admin.AdminMainActivity;
import com.lobxy.societyentrancesystem.R;
import com.lobxy.societyentrancesystem.Utils.Connection;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Connection connection = new Connection(this);

        if (connection.check()) {
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                if (user.getEmail().equals("admin@a.com")) {
                    startActivity(new Intent(this, AdminMainActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

        } else {
            Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT).show();
        }


    }
}
