package com.example.assignment3;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class NavBayes extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_bayes);
    }

    public void sendLocalisation(View view) {
        setContentView(R.layout.activity_main);

    }

    public void sendMessage(View view) {
        setContentView(R.layout.activity_main);

    }
}
