package com.example.utils;

public class WifiData {

    public String SSID;
    public Integer RSSi;
    public Long timestamp;

    // never used?
    public void setValues(String SSID, Integer RSSi, Long timestamp){
        this.SSID = SSID;
        this.RSSi = RSSi;
        this.timestamp = timestamp;
    }

    public String toString(){
        return "SSID: " + SSID + ", RSSi:" + RSSi + ", timestamp:" + timestamp;
    }
}