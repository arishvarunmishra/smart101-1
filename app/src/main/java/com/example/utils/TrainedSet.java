package com.example.utils;

/**
 * Created by ernstmulders on 12/03/2018.
 */

public class TrainedSet {
    public Double[] matrix;
    public String bssid;

    public TrainedSet(Double[] matrix, String bssid){
        this.matrix = matrix;
        this.bssid = bssid;
    }
}