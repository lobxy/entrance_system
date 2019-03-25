package com.lobxy.societyentrancesystem.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
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

    private DatabaseReference mReference;

    private IntentIntegrator mQrScanner;

    private TextView text_data;

    private String mTime, mDate, mName, mProfileUrl;

    //todo:1-get register's last Parent node value i.e last date.
    //todo:2-if the date is today's,put data under it.
    //todo:3-get if it's in Entry or Exit mode.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReference = FirebaseDatabase.getInstance().getReference("Register_Global");

        setContentView(R.layout.activity_reader);
        mQrScanner = new IntentIntegrator(this);

        Button readCode = findViewById(R.id.reader_readCode);

        text_data = findViewById(R.id.reader_text);

    }

    public void readQrCode(View view) {
        //intializing scan object
        mQrScanner.initiateScan();
    }


    //Getting the scan results
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
                    mProfileUrl = jsonObject.getString("qrImageURL");

                    saveData();

                } catch (JSONException e) {
                    e.printStackTrace();

                    //if json isn't created, show the contents in a toast.
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();

                    Log.i("READER", "onActivityResult: " + result.getContents());
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void saveData() {
        mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        mTime = DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());

        //checkNodeValue();

        //save data.

        HashMap<String, String> data = new HashMap<>();
        data.put(mName, "Entry");

        mReference.child(mDate).child(mTime).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ReaderActivity.this, "Task  complete", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("Reader-saveData", "onComplete: error: " + task.getException().getMessage());
                }
            }
        });
    }

    private void checkNodeValue() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Register_Global");
        Query query = reference.limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.i("Reader", "onDataChange: snapshot: " + dataSnapshot);
                    //check for today's date.


                } else {
                    Log.i("Reader", "onDataChange: No data found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}
