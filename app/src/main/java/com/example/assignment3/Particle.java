package com.example.assignment3;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.concurrent.ThreadLocalRandom;

public class Particle extends AppCompatActivity {

    private String TAG = "Particle class: ";

    private Canvas map;
    private Integer screen_width;
    private Integer screen_height;

    public ShapeDrawable particle;

    private Integer particle_size = 5;

    // particle current location
    private int posX;
    private int posY;

    private int index;  //index in the particleList

    public Particle(Canvas c, Integer width, Integer height, Integer listIndex){
        //store the canvas, width and height
        map = c;
        screen_width = width;
        screen_height = height;
        index = listIndex;
    }

    /**
     * get particle's current position
     */

    public int getX(){
        return this.posX;
    }

    public int getY(){
        return this.posY;
    }

    /**
     *  assign a particle given x and y positions
     */

    public void defineParticlePosition(int posX, int posY, boolean bigRedDot){

        // update this particle's position
        this.posX = posX;
        this.posY = posY;

        // create particle shape
        particle = new ShapeDrawable(new OvalShape());


        // if this particle should be a big red dot (current location) or a normal blue dot (normal particle)
        if (bigRedDot){
            particle.getPaint().setColor(Color.RED);
            particle.setBounds(posX - 5 * particle_size, posY - 5 * particle_size, posX + 5 * particle_size, posY + 5 * particle_size);
        } else {
            particle.getPaint().setColor(Color.BLUE);
            particle.setBounds(posX - particle_size, posY -  particle_size, posX +  particle_size, posY + particle_size);
        }
    }

    public void changeColor(){
        particle.getPaint().setColor(Color.CYAN);
    }

    /**
     * returns the list index
     */
    public int getIndex(){
        return index;
    }

    /**
     * draw particle on a random position
     */

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void assignRandomPosition(){

        // randomize position
        int randX = ThreadLocalRandom.current().nextInt(0, screen_width - (particle_size/2));
        int randY = ThreadLocalRandom.current().nextInt(0, screen_height - (particle_size / 2));

        // update this particle's position
        this.posX = randX;
        this.posY = randY;

        // create shape and color properties
        particle = new ShapeDrawable(new OvalShape());
        particle.getPaint().setColor(Color.GREEN);
        particle.setBounds(randX-particle_size, randY-particle_size, randX+particle_size, randY+particle_size);
    }

    /**
     * redraw the particle
     */
    public void redraw(){
        particle.draw(map);
    }
}
