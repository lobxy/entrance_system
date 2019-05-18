package com.lobxy.societyentrancesystem.Reader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.lobxy.societyentrancesystem.Activities.LoginActivity;
import com.lobxy.societyentrancesystem.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ReaderActivity extends AppCompatActivity {

    private static final String TAG = "Reader";
    private DatabaseReference mReference;
    private FirebaseAuth mAuth;

    private IntentIntegrator mQrScanner;

    private TextView text_data;

    private String mTime, mDate, mName, mProfileUrl, mEmail;

    private ProgressDialog mProgressDialog;

    private boolean mMode = true; // false means exit and true means entry.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Working...");
        mProgressDialog.setInverseBackgroundForced(false);
        mProgressDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference("Register_Global");

        setContentView(R.layout.activity_reader);
        mQrScanner = new IntentIntegrator(this);

        Button readCode = findViewById(R.id.reader_button_readCode);
        readCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start to read the code.
                mQrScanner.initiateScan();
            }
        });

        final Button changeMode = findViewById(R.id.reader_button_mode);

        changeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMode) {
                    mMode = false;
                    changeMode.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    changeMode.setText("Exit Mode");

                } else {
                    mMode = true;
                    changeMode.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    changeMode.setText("Entry Mode");
                }
            }
        });

        text_data = findViewById(R.id.reader_text);

    }

    //Get the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data

                try {

                    //set text_value to textView
                    String contents = result.getContents();

                    //decode data
                    byte[] decodeValue = Base64.decode(contents, Base64.DEFAULT);
                    String newData = new String(decodeValue);

                    JSONObject jsonObject = new JSONObject(newData);

                    mName = jsonObject.getString("name");
                    mProfileUrl = jsonObject.getString("uid");
                    mEmail = jsonObject.getString("email");

                    text_data.setText("name: " + mName + "\nuid: " + mProfileUrl);
                    Log.i(TAG, "onActivityResult: data captured");
                    saveGlobalData();

                } catch (JSONException e) {
                    Log.i("READER", "onActivityResult: Error occurred " + e.getLocalizedMessage());
                    e.printStackTrace();

                    //if json isn't created, show the contents in a toast.
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();


                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void saveGlobalData() {
        mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        mTime = DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());

        //save data.
        final HashMap<String, String> data = new HashMap<>();

        //get value of mode and set value acc.
        if (mMode) {
            data.put(mName, "Entry");
        } else {
            data.put(mName, "Exit");
        }

        mProgressDialog.show();

        mReference.child(mDate).child(mTime).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(ReaderActivity.this, "Task  complete", Toast.LENGTH_SHORT).show();
                    saveUserData(data);
                } else {
                    Log.i("Reader-saveGlobalData", "saveGlobalData error: " + task.getException().getMessage());
                }
            }
        });

    }

    private void saveUserData(final HashMap<String, String> data) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Register_Users");

        mProgressDialog.show();
        reference.child(mEmail).child(mDate).child(mTime).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(ReaderActivity.this, "User data saved", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("Reader-saveGlobalData", "saveUserData error: " + task.getException().getMessage());
                }
            }
        });
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
            return false;
        } else return super.onOptionsItemSelected(item);
    }
}
