package com.example.assignment3;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;


public class NavParticles extends AppCompatActivity {

    private Button getMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_particles);
        //getMap = (Button) findViewById(R.id.particle_4th_floor_button);
    }

    public void sendParticles(View view) {
        setContentView(R.layout.activity_particles);
    }
}
