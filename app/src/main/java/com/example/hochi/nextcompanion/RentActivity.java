package com.example.hochi.nextcompanion;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.qrcode.detector.Detector;

import org.w3c.dom.Text;

public class RentActivity extends AppCompatActivity implements AsyncTaskCallbacks<String> {
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private RequestHandler rentRequestTask = null;

    private boolean cameraEnabled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button mRentSubmitButton = findViewById(R.id.rent_submit_button);
        mRentSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rentRequest();
            }
        });

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            String bikeID = data.toString().substring(15);
            ((TextView) findViewById(R.id.bike_id)).setText(bikeID);
        }
    }

    void hasCameraAccess(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraEnabled = true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(RentActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraEnabled = true;
            } else {
                com.google.zxing.qrcode.decoder.Decoder decoder;

                //Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    boolean requestCameraAccess(){
        return false;
    }
    void rentRequest() {
        //Prepare request to rent bike
        TextView mBikeInput;
        mBikeInput = findViewById(R.id.bike_id);
        String bikeID = mBikeInput.getText().toString();
        //get loginkey
        SharedPreferences sharedPref = getSharedPreferences("persistence", MODE_PRIVATE);
        String defaultValue = "nokey";
        String loginKey = sharedPref.getString("loginKey", defaultValue);

        String[] params = {
                "apikey=", getString(R.string.apikey),
                "loginkey=", loginKey,
                "bike=", bikeID
        };

        rentRequestTask = new RequestHandler(this, "POST",
                "api/rent.json", params);
        rentRequestTask.execute((Void) null);
    }

    @Override
    public void onTaskComplete(String response) {
        //get back to main activity
        //TODO: *any* response handling
        finish();
    }
}
