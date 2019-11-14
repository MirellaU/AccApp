package com.example.mirella.testapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivity extends AppCompatActivity {

    public static String TAG = "StartActivity";

    Intent serviceIntent;
    public static String ACC_VALUES = "NEW_ACC_VALUES";
    double roll, pitch;

    public ArrayList<Double> rollValues = new ArrayList<>();
    public ArrayList<Double> pitchValues = new ArrayList<>();
    public ArrayList<Double> accValues = new ArrayList<>();

    IntentFilter accValuesIntentFilter;

    @BindView(R.id.startTestID)
    Button startTest;
    @BindView(R.id.stopTestID)
    Button stopTest;

    @OnClick(R.id.startTestID)
    public void StartTest() {
        serviceIntent = new Intent(this, GetTheDataService.class);
        startService(serviceIntent);
        Toast.makeText(getApplicationContext(), "Rozpoczęto badanie", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.stopTestID)
    public void StopTest() {
        stopService(serviceIntent);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Czy chcesz zapisać wynik do pliku .csv?")
                .setPositiveButton("Zapisz do pliku", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Save();
                        rollValues.clear();
                        pitchValues.clear();
                        Toast.makeText(getApplicationContext(), "Zapisano pomyślnie", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Nie zapisuj", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // Create the AlertDialog object
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        accValuesIntentFilter = new IntentFilter("NEW_ACC_VALUES");
        registerReceiver(accValuesReceiver, accValuesIntentFilter);
    }

    private BroadcastReceiver accValuesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACC_VALUES)) {
                accValues = (ArrayList<Double>) intent.getSerializableExtra("ACC_VALUES");
                rollValues.add(accValues.get(0));
                pitchValues.add(accValues.get(1));
                Log.d(TAG, String.valueOf(rollValues));
                Log.d(TAG, String.valueOf(pitchValues));
            }
        }
    };


    private void Save(){
        String timeStamp = new SimpleDateFormat(getString(R.string.date_format)).format(Calendar.getInstance().getTime());
        String csv = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csv));
            ArrayList data = new ArrayList<>();
            data.add(timeStamp);
            data.add("Roll: ");
            data.add(rollValues);
            data.add("Pitch: ");
            data.add(pitchValues);

            writer.writeAll(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onPause () {
        super.onPause();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        unregisterReceiver(accValuesReceiver);
    }
}
