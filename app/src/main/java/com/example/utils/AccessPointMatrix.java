package com.example.utils;


import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by ernstmulders on 21/02/2018.
 */

/*
 *  Class which allows manipulation of matrices containing PMF values of access points
 */

public class AccessPointMatrix {
    private static String TAG ="AccessPointMatrix";
    public Double[][] matrix;
    public String BSSID;

    public AccessPointMatrix(String BSSID){
        this.BSSID = BSSID;
    }

    public void createMatrix(Integer cellcount){
        matrix = new Double[cellcount][100]; // 100 bins for RSSI values
    }

    public void addValue(Integer cellIndex, Integer level, Integer timesTrained, Boolean useFrequency){
        // make the level absolute e.g. 30dBm = strong signal, 90dBm = weak signal
        // not negative values -30dBm or -90dBm
        level = Math.abs(level);
        // ignore values above 100dBm
        if(level > 100){return;}

        //go through the required row and change existing values, and add the current level
        Integer cellCount = 0;

        for(Double cell : matrix[cellIndex]){

            if(cell == null){
                cell = 0.0;
            }

            Double newCellValue;

            // useFrequency:
            // true: log the frequency of each RSSI value to each matrix entry (no normalization)
            //          this is easier to process by MATLAB
            // false: log the relative frequency of each RSSI level value to each matrix entry
            //          this means dividing by the number of entries every time we add to the matrix
            if(useFrequency){
                newCellValue = cell;

                if (cellCount.equals(level)) {
                    newCellValue = cell + 1;
                }
            }
            else {
                newCellValue = (cell * (timesTrained - 1)) / timesTrained;

                if (cellCount.equals(level)) {
                    newCellValue += (1.0 / timesTrained);
                }
            }

            // add new cell value to the matrix
            matrix[cellIndex][cellCount] = newCellValue;

            cellCount ++;

        }

    }

    public void saveToCsv(Context context){
        String csvText = "";

        //loop through all accesspoints
        for(Double[] rssiValues : matrix){
            //loop through all rssi values
            for(Double value : rssiValues){
                if(value == null){
                    value = 0.0;
                }
                csvText = csvText + value.toString() + ",";
            }
            //chop off last comma
            csvText = csvText.substring(0, csvText.length() - 1);

            //add brakeline
            csvText = csvText + "\n";
        }

        //create the file
        try {

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(BSSID.replace(":","-")+".csv", context.MODE_PRIVATE));
            outputStreamWriter.write(csvText);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    // convert access point matrix to string
    public String toString(){
        String output = "[";

        for(Double[] row : matrix){

            for(Double cell : row){
                output += cell + ",";
            }

            output += "\n";
        }

        output += "]";
        return output;
    }
}
