package com.example.utils.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by ernstmulders on 11/03/2018.
 *
 * This class is not used within the ParticleFilter, because our phones don't have a barometer
 */

public class Barometer implements SensorEventListener {

    private Context context;
    private TextView barometer_status_view;
    private SensorManager mSensorManager;

    /**
     * Initialize barometer
     * @param c context
     * @param statusview homepage output text
     */
    public Barometer(Context c, TextView statusview){
        context = c;
        barometer_status_view = statusview;

        //create sensor manager
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        //and listen to changes
        Sensor pS = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mSensorManager.registerListener(this, pS, SensorManager.SENSOR_DELAY_UI);

    }

    /**
     * returns if the device has the correct sensors
     */
    public boolean hasCorrectSensors(){

        return ((mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        barometer_status_view.setText("Pressure: " + values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
