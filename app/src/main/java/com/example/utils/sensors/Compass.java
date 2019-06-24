package com.example.utils.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.assignment3.NavParticles;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by ernstmulders on 11/03/2018.
 */

public class Compass extends NavParticles implements SensorEventListener{
    private static String TAG = "Compass";

    private Context context;
    private SensorManager mSensorManager;

    private TextView wifi_status_text;
    private ImageView compass_needle;
    private Boolean is_home;    //used to know if the data is used for the home activity, or the particles activity
    private float currentDegree = 0f;

    //direction buttons if not on home
    private Button left;
    private Button right;
    private Button up;
    private Button down;

    public String heading;

    /**
     * Initialize the compass
     * @param c context
     */
    public Compass(Context c, TextView statusview, ImageView compasneedle, Boolean ishome){
        context = c;
        wifi_status_text = statusview;
        compass_needle = compasneedle;
        is_home = ishome;

        //create sensor manager
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        //check if we have the correct sensors
        if(hasCorrectSensors()){
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        }
    }

    /**
     * Set the buttons from the activity
     * @param l left button id
     * @param r right button id
     * @param u up button id
     * @param d down button id
     */

    public void setButtons(Button l, Button r, Button u, Button d){
        left = l;
        right = r;
        up = u;
        down = d;

    }


    /**
     * returns if the device has the correct sensors
     */
    public boolean hasCorrectSensors(){

        return ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) && (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(Math.abs(currentDegree - event.values[0]) > 2){
            String direction;

            if(currentDegree - event.values[0] > 0){
                direction = " moving counter clockwise";
            }
            else{
                direction = " moving clockwise";
            }
            if(is_home){wifi_status_text.setText(Math.round(event.values[0]*10)/10 + " ° " + direction);}
            currentDegree = event.values[0];

        }
        else{
            currentDegree = event.values[0];
            if(is_home){wifi_status_text.setText(Math.round(event.values[0]*10)/10 + " ° ");}
        }

        if(is_home) {
            compass_needle.setRotation(-event.values[0]);
        }
        else{
            //on particle activity, so show arrow in direction of the map
            float imageRotation = ((event.values[0])) - 150.f;
            compass_needle.setRotation(imageRotation);

            //and calculate the heading
            computeHeading(Math.round(imageRotation));
        }

    }

    public void computeHeading(int imageRotation){

        //reset button colors
        right.setBackgroundColor(0x00000000);
        left.setBackgroundColor(0x00000000);
        up.setBackgroundColor(0x00000000);
        down.setBackgroundColor(0x00000000);


        //get the value between 0 and 360
        while(imageRotation < 0){
            imageRotation = imageRotation + 360;
        }
        imageRotation = imageRotation % 360;

        /*
        if(imageRotation > 45 && imageRotation < 135){
                heading = "right";
                right.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            }
        else if(imageRotation >= 135 && imageRotation < 215){
                heading = "down";
                down.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            }
        else if(imageRotation >= 215 && imageRotation < 315){
            heading = "left";
            left.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
        }
        else{
            heading = "up";
            up.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
        }

        NavParticles.heading = heading;
        */
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
