package com.lobxy.societyentrancesystem.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.lobxy.societyentrancesystem.R;

import org.json.JSONException;
import org.json.JSONObject;

public class ReaderActivity extends AppCompatActivity {

    private DatabaseReference mReference;

    private IntentIntegrator mQrScanner;

    private TextView text_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                    Log.i("ReadQr", "onActivityResult: data: " + jsonObject);

                    Log.i("ReadQr", "onActivityResult: Name: " + jsonObject.getString("name"));
                    text_data.setText(jsonObject.getString("name"));

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

}
