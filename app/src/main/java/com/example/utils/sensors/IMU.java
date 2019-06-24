package com.example.utils.sensors;

import java.util.ArrayList;

/**
 * Created by lx on 29-3-18.
 */

public class IMU {

    private String TAG = "IMU data: ";

    // properties

    // time stamp
    private long time;

    // raw IMU data
    private float gyroX, gyroY, gyroZ;
    private float accX, accY, accZ;

    // features

    // constructor
    public IMU(float accX, float accY, float accZ, float gyroX, float gyroY, float gyroZ, long time){
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
        this.gyroX = gyroX;
        this.gyroY = gyroY;
        this.gyroZ = gyroZ;
        this.time = time;

    }

    public float getAccX() {return this.accX;}

    public float getAccY() {return this.accY;}

    public float getAccZ() {return this.accZ;}

    public float getGyroX() {return this.gyroX;}

    public float getGyroY() {return this.gyroY;}

    public float getGyroZ() {return this.gyroZ;}

    public long getTimeStamp() {return this.time;}
}
