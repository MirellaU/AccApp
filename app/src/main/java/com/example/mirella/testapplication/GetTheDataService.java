package com.example.mirella.testapplication;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;

public class GetTheDataService extends JobIntentService implements SensorEventListener {
    public static final String TAG = "ServiceActivity";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private double lastX, lastY, lastZ, roll, pitch;
    public ArrayList<Double> accValues = new ArrayList<>();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // want service to continue running until its explicitly stopped so return sticky
    }

    @Override
    public void onCreate() {
//        super.onCreate();
        try {
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Nie wykryto akcelerometru. Nie można przeprowadzić badania.")
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG,"Service started");

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) { }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { return; }

    @Override
    public void onSensorChanged(SensorEvent event) {

        lastX = event.values[0];// - gravityX;
        lastY = event.values[1]; //- gravityY;
        lastZ = event.values[2];// - gravityZ;

//        lastX = event.values[0];
//        lastY = event.values[1];
//        lastZ = event.values[2];

        roll = Math.toDegrees(Math.atan2(lastX,lastZ));
        pitch = Math.toDegrees(Math.atan2(lastY,lastX));

        accValues.add(roll);
        accValues.add(pitch);
        //rollValues.add(roll);
        //pitchValues.add(pitch);
        Send();
    }

    protected void Send() {
        Intent accValuesIntent = new Intent("NEW_ACC_VALUES");
        accValuesIntent.putExtra("ACC_VALUES", accValues);
        Log.d(TAG, String.valueOf(accValues));
        sendBroadcast(accValuesIntent);
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);
    }
}
