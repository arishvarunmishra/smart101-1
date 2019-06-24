package com.example.utils.sensors;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;


/**
 * Created by ernstmulders on 11/03/2018.
 */

public class WifiSensor {
    private static String TAG = "WifiSensor";

    private WifiManager wifiManager;
    private Context context;

    private List<ScanResult> scanResults;

    /**
     * Initialize the wifi sensor
     */
    public WifiSensor(Context c){
        context = c;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);;
    }

    /**
     * returns the number of visible accesspoints
     */
    public int getNumberOfVisibleAccessPoints(){
        //perform new scan
        this.performNewScan();

        //return the number in the list
        return scanResults.size();
    }

    /**
     * return the scanresults
     */
    public List<ScanResult> getScanResults(){
        //first turn off wifi
        wifiManager.setWifiEnabled(false);

        //now turn it back on again
        wifiManager.setWifiEnabled(true);

        //perform new scan
        this.performNewScan();

        //return the result
        return scanResults;
    }

    /**
     * performs a new scan
     */
    private void performNewScan(){
        wifiManager.startScan();
        scanResults = wifiManager.getScanResults();
    }


    /**
     * checks if all permissions are set to get the data
     **/
    public boolean hasPermission(){

        return (
                (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED) &&

                        (ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED) &&

                        (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)&&

                        (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)&&

                        (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)&&

                        (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
        );
    }
}
