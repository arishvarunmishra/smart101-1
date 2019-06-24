package com.example.assignment3;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.example.assignment3.R;
import com.example.assignment3.R.*;
import com.example.utils.sensors.Compass;
import com.example.utils.sensors.IMU;
import com.example.utils.sensors.Steps;



public class Particles extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private static String TAG = "ParticlesActivity";

    //configuration
    private static Integer particlesAmount = 1000;

    // number of particles to refactor:
    // must be less than particlesAmount
    private static int refactorParticlesAmount = 5;
    private static Boolean shouldDrawClosedAreas = false;

    private static Canvas canvas;
    private static List<ShapeDrawable> walls;
    private static List<ShapeDrawable> dividers;
    private static List<ShapeDrawable> closed_areas;
    private static List<Particle> particlesList;
    private static Particle currentLocation;

    //define sensors


    // define buttons
    private static Button reset,locateMe,train;

    public static String heading = "";

    // define textview for button pressed and reset status

    private static TextView step_counter;
    private static TextView step_size;


    // manual location (Big red dot) original location on map
    private static int originalLocationX = 500;
    private static int originalLocationY = 220;

    // big red dot's current location
    private static int actualLocationX = originalLocationX;
    private static int actualLocationY = originalLocationY;

    private static ArrayList<features> featuresList = new ArrayList<features>();
    //how much millimeters is one step
    private static int stepSize = 700;
    private static int walkedDistanceCm = 0;
    private static int stepCount = 0;


    private int floorWidth = 14300;
    private int floorHeight = 40000;

    private SensorManager mSensorManager;

    private Sensor accelerometer, gyroscope;

    // accelerometer values
    private float aX = 0;
    private float aY = 0;
    private float aZ = 0;

    // gyroscope values
    private float gX = 0;
    private float gY = 0;
    private float gZ = 0;


    private static int screen_width = 0;
    private static int screen_height = 0;


    private static boolean busyTraining = false;
    private static int normalWalking = 2;

    private static ArrayList<IMU> imuMeasurementsList = new ArrayList<IMU>();

    private static String gyroscopeName;
    private static String accelerometerName;

    private int trainingCount = 0;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particles);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;
        screen_height = size.y;

        //Bundle bundle = getIntent().getExtras();
        //stepSize = bundle.getInt("stepsize");


        reset = (Button) findViewById(R.id.reset);
        locateMe = (Button) findViewById(R.id.locate);
        train = (Button) findViewById(R.id.particleTrain);

        // set the text views

        step_counter = (TextView) findViewById(R.id.step_counter);
        step_size = (TextView) findViewById(R.id.step_size);

        step_size.setText("Size:" + stepSize);


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // if the default accelerometer exists
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // get names of accelerometers
        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);

        if (android_id.equals("9342638c3cbf3199")) {
            gyroscopeName = "3-axis Gyroscope";
            accelerometerName = "3-axis Accelerometer";
        } else {
            gyroscopeName = "L3GD20 Gyroscope";
            accelerometerName = "LIS3DH Accelerometer";
        }

        // set listeners on buttons

        reset.setOnClickListener(this);
        locateMe.setOnClickListener(this);
        train.setOnClickListener(this);

        // create a canvas
        ImageView canvasView = (ImageView) findViewById(R.id.canvas);
        Bitmap blankBitmap = Bitmap.createBitmap(screen_width,screen_height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        canvasView.setImageBitmap(blankBitmap);

        //determine the floor

        map floor4 = new map(screen_width,screen_height);
        walls = floor4.getWalls(screen_width, screen_height);
        dividers = floor4.getDividers(screen_width, screen_height);
        closed_areas = floor4.getClosedAreas(screen_width, screen_height);

        // draw the objects
        for(ShapeDrawable wall : walls)
            wall.draw(canvas);

        for(ShapeDrawable divider : dividers) {
            divider.getPaint().setColor(Color.GRAY);
            divider.draw(canvas);
        }

        // draw the closed areas if needed
        if(shouldDrawClosedAreas) {
            for (ShapeDrawable closedArea : closed_areas)
                closedArea.draw(canvas);
        }


        //generate all particles and place them on the map
        particlesList = new ArrayList<>();
        Integer particleCount = 0;
        for(particleCount = 0; particleCount < particlesAmount; particleCount++){
            // generate a new particle
            Particle particle = new Particle(canvas, screen_width, screen_height, particleCount);

            // place particle at random position within bounds
            while(isCollision(particle) || isInClosedArea(particle)){
                particle.assignRandomPosition();
            }

            // add to our list of particles
            particlesList.add(particle);
        }

        // Create particle showing current location
        currentLocation = new Particle(canvas, screen_width, screen_height, particleCount + 1);
        actualLocationX = originalLocationX;
        actualLocationY = originalLocationY;
        currentLocation.defineParticlePosition(actualLocationX, actualLocationY,false);
        // add current location to list of particles so it gets redrawn every time
        particlesList.add(currentLocation);

        // and redraw everything async
        new redraw().execute("");

       // Log.d(TAG, "load floor: " + floor);

    }

    /**
     * determines if particle is in a closed area (within an obstacle)
     */
    private boolean isInClosedArea(Particle p){
        //if particles == null, it is not yet drawn, and so its in a closed area
        if(p.particle == null){return true;}

        for(ShapeDrawable closedArea : closed_areas) {
            if(isShapeCollision(closedArea,p.particle)){
                return true;
            }

        }

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v){
        Log.d(TAG, "a button was pressed");

        // check if reset is performed, if so, do not use motion model
        boolean resetPressed = false;
        boolean manualMovePressed = false;
        boolean sampleButtonPressed = false;
        boolean trainButtonPressed = false;

        //int distanceWalked = distanceWalkedMillimeters*screenwidth/floor4Width;
        int distanceWalkedMillimeters = 500;
        int orientationWalkedDegrees = 0;

        currentLocation.defineParticlePosition(actualLocationX,actualLocationY,false);
        currentLocation.redraw();

        /**
         * Check which button is pressed
         */
        switch (v.getId()) {

            case R.id.reset:{
                resetPressed = true;
                break;
            }
            case R.id.locate:{
                applyRANSACLocalization();
                break;
            }
            case R.id.particleTrain:{
                trainButtonPressed = true;
                break;
            }

        }

        /**
         * if UP, DOWN, LEFT, RIGHT is pressed
         */
        if (manualMovePressed) {
            // apply movement as specified by manual input
            calculateParticlesPosition(distanceWalkedMillimeters,orientationWalkedDegrees );
        }

        /**
         * if RESET is pressed
         */
        if (resetPressed){
            // if reset IS pressed, put particles on map (probability)
            // do reset of particles
            // generate all particles and place them on the map
            for(Integer particleCount = 0; particleCount < particlesAmount; particleCount++){
                // generate a new particle
                Particle particle = new Particle(canvas, screen_width, screen_height, particleCount);

                // place particle at random position within bounds
                while(isCollision(particle) || isInClosedArea(particle)){
                    particle.assignRandomPosition();
                }

                // update our list of particles
                particlesList.set(particleCount, particle);
            }

            // reset distance walked and step count
            walkedDistanceCm = 0;
            stepCount = 0;

            // update text views
            //step_distance.setText("0cm");
            step_counter.setText("0 steps");

            // redraw on a separate thread
            new redraw().execute("");
        }

        /**
         * If SAMPLE is pressed - used to
         */


        /**
         * If TRAIN is pressed
         */
        if (trainButtonPressed){
            busyTraining = !busyTraining;
            if (busyTraining) {
                trainingCount++;

            } else {
                // stop training and write CSV files
                if (featuresList.size() > 0){
                    //Log.d(TAG, "writing CSV of feature lists");
                    writeFeatureDataToCsv(getApplicationContext());
                }
                //sampling.setText("Training stopped");
            }
        }

    }

    @Override
    public void onBackPressed(){
        //reset stepcount
        stepCount = 0;

        //unregister sensors
        Log.d(TAG, "unregister sensors");
        //steps.unregisterListener();
        mSensorManager.unregisterListener(this);

        super.onBackPressed();
    }

    /**
     * uses a RANSAC algorithm to determine the most probable current location based on the particle
     * spread utilizing a circular location model
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void applyRANSACLocalization(){
        int[][] particleLocations = new int[2][particlesAmount];

        currentLocation.defineParticlePosition(currentLocation.getX(),currentLocation.getY(),false);
        currentLocation.redraw();

        // fill array with particle locations
        for (int idx = 0;idx < particlesAmount;idx++){
            particleLocations[0][idx] = particlesList.get(idx).getX();
            particleLocations[1][idx] = particlesList.get(idx).getY();
        }

        // RANSAC
        int rounds = 52;
        int NinliersMax = 0;
        int idxMax = 0;
        int distThreshold = 100; // pixels
        distThreshold = distThreshold*distThreshold;

        // try find the perfect particle 'round' times
        for (int iter = 0; iter < rounds; iter++){

            // choose random index
            int randomIdx = ThreadLocalRandom.current().nextInt(0,particlesAmount-1);

            int currentParticleX = particlesList.get(randomIdx).getX();
            int currentParticleY = particlesList.get(randomIdx).getY();

            particlesList.get(randomIdx).changeColor();

            int Ninliers = 0;



            for (int i = 0; i < particlesAmount; i++){
                int dist =  (int) Math.round(Math.pow( (particleLocations[0][i] - currentParticleX),2.0) + Math.pow((particleLocations[1][i] - currentParticleY),2));
                if (dist<distThreshold){
                    Ninliers++;
                }
            }

            if (Ninliers > NinliersMax){
                NinliersMax = Ninliers;
                idxMax = randomIdx;
            }
        }

        // Determine centroid using particles around best fitting particle

        int maxIdxParticleX = particlesList.get(idxMax).getX();
        int maxIdxParticleY = particlesList.get(idxMax).getY();

        // log all inliers to an array
        ArrayList<Integer> inliersX = new ArrayList<>();
        ArrayList<Integer> inliersY = new ArrayList<>();

        for (int i = 0; i < particlesAmount; i++) {
            int dist =  (int) Math.round(Math.pow( (particleLocations[0][i] - maxIdxParticleX),2.0) + Math.pow((particleLocations[1][i] - maxIdxParticleY),2));

            // check if distance is within a specified threshold
            if (dist<distThreshold){

                // if so, add this particle to list of inliers
                inliersX.add(particleLocations[0][i]);
                inliersY.add(particleLocations[1][i]);
            }
        }

        int centroidX = 0;
        int centroidY = 0;

        for(int i = 0; i < inliersX.size(); i++){
            centroidX = centroidX + inliersX.get(i);
            centroidY = centroidY + inliersY.get(i);
        }

        centroidX = centroidX/NinliersMax;
        centroidY = centroidY/NinliersMax;

        currentLocation.defineParticlePosition(centroidX,centroidY,true);

        // make sure red dot is on top
        currentLocation.redraw();
    }

    /**
     * apply noisy motion model to particles given a distance and orientation input
     * @param distanceWalkedMillimeters
     * @param orientationWalkedDegrees
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void calculateParticlesPosition(int distanceWalkedMillimeters, int orientationWalkedDegrees){
        long starttime = System.currentTimeMillis();

        // variance of orientation and distance
        int distanceVariance = 300; // 300 pixel variance
        int orientationVariance = 10; // 10 degrees orientation variance

        // init distance and orientation variables which include noise
        int noisyDistanceWalkedMillimeters;
        double noisyOrientationWalkedDegrees;

        int noisyDistanceWalkedPixelsX;
        int noisyDistanceWalkedPixelsY;

        ArrayList<Particle> survived = new ArrayList<>();
        ArrayList<Particle> dead = new ArrayList<>();

        // loop through particle list to apply motion model
        for (int particleIdx = 0; particleIdx < particlesAmount; particleIdx++) {

            // currentParticle is the particle at its current location (no motion applied yet)
            Particle currentParticle = particlesList.get(particleIdx);
            int initX = currentParticle.getX();
            int initY = currentParticle.getY();

            // create random variables and define (Gaussian) noisy distance and orientation (based on variances defined at OnClick)
            Random distanceRandom = new Random();
            Random orientationRandom = new Random();

            // create distance
            noisyDistanceWalkedMillimeters = distanceWalkedMillimeters + (int) Math.round(distanceRandom.nextGaussian()*distanceVariance);
            noisyOrientationWalkedDegrees = (double) orientationWalkedDegrees + orientationRandom.nextGaussian()*orientationVariance;

            int noisyDistanceWalkedMillimetersX = (int) Math.round(noisyDistanceWalkedMillimeters*Math.sin(Math.toRadians(noisyOrientationWalkedDegrees)));
            int noisyDistanceWalkedMillimetersY = (int) Math.round(noisyDistanceWalkedMillimeters*Math.cos(Math.toRadians(noisyOrientationWalkedDegrees)));

            noisyDistanceWalkedPixelsX = noisyDistanceWalkedMillimetersX*screen_width/floorWidth;
            noisyDistanceWalkedPixelsY = noisyDistanceWalkedMillimetersY*screen_height/floorHeight;

            // create new Particle which represents moved particle position
            Particle movedParticle = new Particle(canvas,screen_width,screen_height, particleIdx);

            // find new x and y coordinates of moved particle and define the movedParticle
            int newX = initX + noisyDistanceWalkedPixelsX;
            int newY = initY + noisyDistanceWalkedPixelsY;

            movedParticle.defineParticlePosition(newX, newY, false);

            /**
             *  Summary - we have:
             *  currentParticle - represents original particle position
             *  movedParticle - represents original particle moved with motion model
             */

            //check if new particle is dead
            if(isCollisionTrajectory(movedParticle, currentParticle)){
                dead.add(movedParticle);
            }
            else{
                //its not, so add to the alive list
                survived.add(movedParticle);
            }


            // update particleList
            particlesList.set(particleIdx,movedParticle);


        }



        //reassign the dead particles to an alive particle
        int randomParticleIdx;
        for(Particle deadParticle : dead){
            //get a random particle that survived
            randomParticleIdx = ThreadLocalRandom.current().nextInt(0, survived.size() - 1);
            Particle survivedParticle = survived.get(randomParticleIdx);

            //copy its location to the dead particle
            deadParticle.defineParticlePosition(survivedParticle.getX(), survivedParticle.getY(), false);

            //and put it in the correct place in the list
            particlesList.set(deadParticle.getIndex(), deadParticle);
        }

        /***
         * Refactor particles
         *
         * This means re-assigning random particles to random map positions
         *
         * The idea is to allow for alternative particle positions in case all particles converge
         * to the wrong location
         */


        for (int refactorIdx = 0; refactorIdx < refactorParticlesAmount; refactorIdx++){

            // get random index within particle amount
            int randomIdx = ThreadLocalRandom.current().nextInt(0, particlesAmount-1);

            // update new particles
            Particle newRandomParticle = new Particle(canvas,screen_width,screen_height, randomIdx);

            while(isCollision(newRandomParticle) || isInClosedArea(newRandomParticle)){
                newRandomParticle.assignRandomPosition();
            }

            // replace current particle at randomIdx with this new, random, refactored particle
            particlesList.set(randomIdx,newRandomParticle);
        }

        //redraw async
        new redraw().execute("");

        long taken = System.currentTimeMillis() - starttime;


    }

    /**
     * Updates states when walking is detected
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void walkingDetected(String direction){
        int distanceWalkedMillimeters = 1 * stepSize;   //1 = because the function is called for every step

        walkedDistanceCm = walkedDistanceCm + (distanceWalkedMillimeters / 10);
        stepCount++;


        step_counter.setText(stepCount + "steps");

        // do not create red dot for estimated current location
        currentLocation.defineParticlePosition(currentLocation.getX(),currentLocation.getY(),false);

        // Log.d(TAG, "heading:" + heading + ";");
        int directionInt = 0;

        switch(direction){
            case "up":
                directionInt = 180;
                break;
            case "right":
                directionInt = 90;
                break;
            case "down":
                directionInt = 0;
                break;
            case "left":
                directionInt = 270;
        }

        //make the calculations
        if (!busyTraining) {
            calculateParticlesPosition(distanceWalkedMillimeters, directionInt);
        }



        // if we are training, add to features list
        // if not busy training, just analyze data
        if (busyTraining) {
            // get features
            if (stepCount <= 1) {
                // first step, clear list
                featuresList.clear();
                imuMeasurementsList.clear();

            } else {
                featuresList.add(getSVMFeatures(imuMeasurementsList));
                imuMeasurementsList.clear();

            }


        } else {
            // determine if we are walking normally or on up/down stairs

            if (stepCount <= 1){
                imuMeasurementsList.clear();
            } else {

                features currentFeatures = getSVMFeatures(imuMeasurementsList);

                // get 2 PCA components
                PCA twoPCAComponents = applyPCAreduction(currentFeatures);

                // am I walking on stairs?

                imuMeasurementsList.clear();

            }
        }
    }

    /**
     * Determines if the drawable dot intersects with any of the walls.
     * @return True if that's true, false otherwise.
     */
    private boolean isCollision(Particle p) {
        //if particles == null, it is not yet drawn, and so its a collisions (since it needs a redraw)
        if(p.particle == null){return true;}

        for(ShapeDrawable wall : walls) {
            if(isShapeCollision(wall,p.particle)){
                return true;
            }

        }
        return false;
    }

    /**
     * Determines if the trajectory between a particle's old and new position intersects with a wall
     * or obstacle area
     *
     * @param newPosition - position of particle at new position
     * @param oldPosition - position of particle at original position
     * @return True if the rectangle spanning the old and new particle position intersect with a wall
     */
    private boolean isCollisionTrajectory(Particle newPosition, Particle oldPosition){

        int newX = newPosition.getX();
        int newY = newPosition.getY();

        int oldX = oldPosition.getX();
        int oldY = oldPosition.getY();

        int left, right, top, bottom;

        // check x values
        if (newX <= oldX){
            left = newX;
            right = oldX;
        } else { //if (newX > oldX)
            left = oldX;
            right = newX;
        }

        // check y values
        if (newY <= oldY){
            top = newY;
            bottom = oldY;
        } else { // if (newY > oldY)
            top = oldY;
            bottom = newY;
        }

        // create trajectory rectangle between two particles
        Rect trajectoryRect = new Rect(left, top, right, bottom);

        // check if this trajectory crosses any walls
        for(ShapeDrawable wall : walls) {
            if (trajectoryRect.intersect(wall.getBounds())){
                // trajectory rectangle does intersect with a wall - collision = true
                return true;
            }
        }
        return false;
    }

    /**
     * read IMU sensors. Also specify sampling rate (200 Hz)
     */
    protected void onResume() {
        super.onResume();
        // SENSOR_DELAY_NORMAL  - 100ms (  10 Hz) sampling
        // SENSOR_DELAY_UI      -  60ms (  16 Hz) sampling
        // SENSOR_DELAY_GAME    -  20ms (  50 Hz) sampling
        // SENSOR_DELAY_FASTEST -   5ms ( 200 Hz) sampling
        mSensorManager.registerListener(this, accelerometer, mSensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    /**
     * Determines if two shapes intersect.
     * @param first The first shape.
     * @param second The second shape.
     * @return True if they intersect, false otherwise.
     */
    private boolean isShapeCollision(ShapeDrawable first, ShapeDrawable second) {
        Rect firstRect = new Rect(first.getBounds());
        return firstRect.intersect(second.getBounds());
    }

    /**
     * Writes the IMU data to a CSV file so that it can be analyzed
     * @param context
     */
    private void writeIMUDataToCsv(Context context) {
        StringBuffer csvText = new StringBuffer("");

        for (int i = 0; i < imuMeasurementsList.size(); i++) {
            csvText.append(Float.toString(imuMeasurementsList.get(i).getAccX()));
            csvText.append(",");
            csvText.append(Float.toString(imuMeasurementsList.get(i).getAccY()));
            csvText.append(",");
            csvText.append(Float.toString(imuMeasurementsList.get(i).getAccZ()));
            csvText.append(",");
            csvText.append(Float.toString(imuMeasurementsList.get(i).getGyroX()));
            csvText.append(",");
            csvText.append(Float.toString(imuMeasurementsList.get(i).getGyroY()));
            csvText.append(",");
            csvText.append(Float.toString(imuMeasurementsList.get(i).getGyroZ()));
            csvText.append(",");
            csvText.append(Long.toString(imuMeasurementsList.get(i).getTimeStamp()));
            csvText.append("\n");
        }

        // remove last \n - necessary?
        csvText.setLength(csvText.length()-1);

        //create the file
        try {
            if(context == null){
                Log.d(TAG, "Context is null");
            }

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("dataIMUData08.csv", context.MODE_PRIVATE));
            outputStreamWriter.write(csvText.toString());
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * Write Features to CSV file
     * @param context - application context
     */
    private void writeFeatureDataToCsv(Context context){
        StringBuffer csvProcessed = new StringBuffer("");
        String csvFeaturesFileName = "dataFeatures" + Integer.toString(trainingCount) + ".csv";

        for (int idx = 0; idx < featuresList.size(); idx++) {
            csvProcessed.append(Double.toString(featuresList.get(idx).getX1()));
            csvProcessed.append(",");
            csvProcessed.append(Double.toString(featuresList.get(idx).getX2()));
            csvProcessed.append(",");
            csvProcessed.append(Double.toString(featuresList.get(idx).getX3()));
            csvProcessed.append(",");
            csvProcessed.append(Double.toString(featuresList.get(idx).getX4()));
            csvProcessed.append(",");
            csvProcessed.append(Double.toString(featuresList.get(idx).getX5()));
            csvProcessed.append(",");
            csvProcessed.append(Double.toString(featuresList.get(idx).getX6()));
            csvProcessed.append(",");
            csvProcessed.append(Integer.toString(featuresList.get(idx).walkingOnStairs()));
            csvProcessed.append("\n");
        }

        //create the file
        try {
            if(context == null){
                Log.e(TAG, "Context is null");
            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(csvFeaturesFileName, context.MODE_PRIVATE));
            outputStreamWriter.write(csvProcessed.toString());
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * read IMU (accelerometer and gyroscope) values to imuMeasurementsList
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        String sensorName = event.sensor.getName();
        long timeInMillis = 0;

        if (sensorName.equals(gyroscopeName)){ //3-axis Gyroscope
            //Log.d(TAG, "gyro!!!!");
            gX = event.values[0];
            gY = event.values[1];
            gZ = event.values[2];
        }

        if (sensorName.equals(accelerometerName)){ //3-axis Accelerometer
            aX = event.values[0];
            aY = event.values[1];
            aZ = event.values[2];

            // only add to list once, otherwise get same measurement twice
            timeInMillis = (new Date()).getTime()
                    + (event.timestamp - System.nanoTime()) / 1000000L;


            // if sampling at this point, add to arraylist
            if (stepCount>0){     // && (timeInMillis<prevWalkingDetectedTime+5000)){
                IMU newMeasurement = new IMU(aX,aY,aZ,gX,gY,gZ,timeInMillis);
                imuMeasurementsList.add(newMeasurement);
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * redraw, regenerate the canvas
     */
    private class redraw extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // redrawing of the object
            canvas.drawColor(Color.WHITE);

            for(ShapeDrawable wall : walls)
                wall.draw(canvas);

            for(Particle particle : particlesList){
                particle.redraw();
            }

            for(ShapeDrawable divider : dividers) {
                divider.getPaint().setColor(Color.GRAY);
                divider.draw(canvas);
            }

            //redraw the closed areas if needed
            if(shouldDrawClosedAreas) {
                for (ShapeDrawable closedArea : closed_areas)
                    closedArea.draw(canvas);
            }

            return "drawn";
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    /**
     * Extracts the SVM features from an ArrayList of IMU measurements
     *
     * @param imuMeasurement
     * @return features obtained from the imuMeasurement list
     */
    private features getSVMFeatures(ArrayList<IMU> imuMeasurement){
        //double feature1,feature2,feature3,feature4,feature5,feature6;

        // init first value of imuMeasurement
        double valueAccX = 0; // = imuMeasurement.get(0).getAccX();
        double valueAccY = 0; //imuMeasurement.get(0).getAccY();
        double valueAccZ = 0; //imuMeasurement.get(0).getAccZ();
        double valueGyroY = 0; //imuMeasurement.get(0).getGyroY();

        double meanEnergyXZ = 0.0;
        double meanAccY = 0.0;
        double meanAccZ = 0.0;

        double maxGyroY = 0;

        double varianceAccY = 0.0;
        double varianceEnergyXZ = 0.0;
        double varianceAccZ = 0.0;

        double smoothingFactor = 1; // represents LPF of 20 Hz

        ArrayList<Double> filteredAccY = new ArrayList<>();
        ArrayList<Double> filteredAccZ = new ArrayList<>();
        ArrayList<Double> energyXZ = new ArrayList<>();

        // loop through all parameters to apply LPF and get MEAN
        int measurementCount = imuMeasurement.size();

        for (int idx = 0; idx<measurementCount;idx++){

            // only apply to gyroY and accX,Y,Z

            // apply LPF
            valueAccX = valueAccX + (imuMeasurement.get(idx).getAccX()-valueAccX)/smoothingFactor;
            valueAccY = valueAccY + (imuMeasurement.get(idx).getAccY()-valueAccY)/smoothingFactor;
            valueAccZ = valueAccZ + (imuMeasurement.get(idx).getAccZ()-valueAccZ)/smoothingFactor;
            valueGyroY = valueGyroY + (imuMeasurement.get(idx).getGyroY()-valueGyroY)/smoothingFactor;

            // create new list
            filteredAccY.add(valueAccY);
            filteredAccZ.add(valueAccZ);
            energyXZ.add(Math.pow(valueAccX,2) + Math.pow(valueAccZ,2));

            if (Math.abs(valueGyroY) > Math.abs(maxGyroY)){
                maxGyroY = valueGyroY;
            }

            // determine mean so long
            meanEnergyXZ = meanEnergyXZ + Math.pow(valueAccX,2) + Math.pow(valueAccZ,2);
            meanAccY = meanAccY + valueAccY;
            meanAccZ = meanAccZ + valueAccZ;
        }

        meanAccZ = meanAccZ/measurementCount;
        meanAccY = meanAccY/measurementCount;
        meanEnergyXZ = meanEnergyXZ/measurementCount;

        // determine variance
        for (int idx = 0; idx<measurementCount;idx++) {
            varianceAccY = varianceAccY + Math.pow((filteredAccY.get(idx) - meanAccY),2);
            varianceAccZ = varianceAccZ + Math.pow((filteredAccZ.get(idx) - meanAccZ),2);
            varianceEnergyXZ = varianceEnergyXZ + Math.pow((energyXZ.get(idx) - meanEnergyXZ),2);
        }

        // final calculations

        varianceAccY = varianceAccY/measurementCount;
        varianceAccZ = varianceAccZ/measurementCount;
        varianceEnergyXZ = varianceEnergyXZ/measurementCount;

        /**
         * Assign variables to features
         */
        // check if we are training "normal walking" or "stairs walking" from button

        // return features object
        features featureVals = new features(maxGyroY,varianceAccY,meanEnergyXZ,varianceEnergyXZ,varianceAccZ,meanAccZ,normalWalking);

        return featureVals;
    }

    /**
     * Apply a Principal Component Analysis (PCA) to the feature set to obtain the two main components
     * @param currentFeatures containing the features of the current measurement
     * @return PCA object containing two main PCA components
     */
    private PCA applyPCAreduction(features currentFeatures){

        // define mean
        double[] meanVec = {-0.0547,0.5001,101.388,1933.3,3.9856,9.8310};

        // subtract mean from features
        double X1_no_mean = currentFeatures.getX1() - meanVec[0];
        double X2_no_mean = currentFeatures.getX2() - meanVec[1];
        double X3_no_mean = currentFeatures.getX3() - meanVec[2];
        double X4_no_mean = currentFeatures.getX4() - meanVec[3];
        double X5_no_mean = currentFeatures.getX5() - meanVec[4];
        double X6_no_mean = currentFeatures.getX6() - meanVec[5];

        // define W matrix
        double[][]W_transpose =  {{0.0001,    0.0000 ,   0.0031 ,   1.0000,    0.0019,    0.0001},
                {0.0208,   -0.0013,   -0.9967 ,   0.0030,    0.0565,   -0.0546}};

        // muliply by W matrix
        double PCA1 = X1_no_mean*W_transpose[0][0] + X2_no_mean*W_transpose[0][1] + X3_no_mean*W_transpose[0][2] + X4_no_mean*W_transpose[0][3] + X5_no_mean*W_transpose[0][4] + X6_no_mean*W_transpose[0][5];
        double PCA2 = X1_no_mean*W_transpose[1][0] + X2_no_mean*W_transpose[1][1] + X3_no_mean*W_transpose[1][2] + X4_no_mean*W_transpose[1][3] + X5_no_mean*W_transpose[1][4] + X6_no_mean*W_transpose[1][5];

        PCA currentPCAcomponents = new PCA(PCA1,PCA2);
        return currentPCAcomponents;
    }

    /**
     * Determine decision value of a PCA component object (containing 2 PCA components)
     * @param currentPCAcomponent
     * @return
     */
    private boolean amIWalkingOnStairs(PCA currentPCAcomponent){
        // SVM equation parameters
        double w1 = 0.0032;
        double w2 = 0.1021;
        double bias = 0;//5.1149;

        double decision = w1*currentPCAcomponent.getPCA1() + w2*currentPCAcomponent.getPCA2() + bias;
        Log.d(TAG, "Decision value is: " + Double.toString(decision));
        return (decision > 0);
    }

    /**
     * feature object of the IMU data
     * contains:    X1 to X6 which are the 6 features used for walking classification a
     *              stairs boolean - if feature is for normal walking or climbing stairs
     */
    private class features{
        private double X1,X2,X3,X4,X5,X6;

        private int stairs;

        features(double X1,double X2,double X3,double X4,double X5,double X6, int stairs){
            this.X1 = X1;
            this.X2 = X2;
            this.X3 = X3;
            this.X4 = X4;
            this.X5 = X5;
            this.X6 = X6;
            this.stairs = stairs;
        }

        public double getX1(){
            return this.X1;
        }

        public double getX2(){
            return this.X2;
        }

        public double getX3(){
            return this.X3;
        }

        public double getX4(){
            return this.X4;
        }

        public double getX5(){
            return this.X5;
        }

        public double getX6(){
            return this.X6;
        }

        public int walkingOnStairs() {return this.stairs; }
    }

    /**
     * class for first 2 PCA components
     */
    private class PCA{
        private double PCA1, PCA2;

        PCA(double PCA1, double PCA2){
            this.PCA1 = PCA1;
            this.PCA2 = PCA2;
        }

        public double getPCA1() { return this.PCA1;}

        public double getPCA2() { return this.PCA2;}

    }

}