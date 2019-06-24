package com.example.assignment3;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class map {
    private static String TAG = "floor4";
    private List<ShapeDrawable> walls;
    private List<ShapeDrawable> dividers;
    private List<ShapeDrawable> closed_areas;

    public List getClosedAreas(int width, int height){

        // remove top portion, subtract top from all top_input values
        int top = 0;

        closed_areas = new ArrayList<>();

        //top area (we might chop this off the floorplan entirely
        //closed_areas.add(functionDimensionsToClosedArea(0,0,14300,7800));
        closed_areas.add(functionDimensionsToClosedArea(3000,0,6100,3000));
        closed_areas.add(functionDimensionsToClosedArea(0,4000,6100,36000));
        //rooms below area 8- 12 (as seen on the official floorplan)



        //Right Closed
        closed_areas.add(functionDimensionsToClosedArea(8200,12000-top,14300,36000-top));


        return closed_areas;
    }

    /**
     * floor parameters
     */
    private int screenWidth;
    private int screenHeight;
    private int floorWidthInCm = 14300;
    private int floorHeightInCm = 40000;
    //private int floorHeightInCm = 33800;

    public map(int width, int height){
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public List getDividers(int width, int height){
        //initialize dividers
        dividers = new ArrayList<>();
        dividers.add(functionDimensionsToWall(3000,3000,1000,false));
        dividers.add(functionDimensionsToWall(6100,3000,1000,false));
        dividers.add(functionDimensionsToWall(6100,37500,1000,false));
        //RightDoors
        dividers.add(functionDimensionsToWall(8200,1500,1000,false));
        dividers.add(functionDimensionsToWall(8200,5500,1000,false));
        dividers.add(functionDimensionsToWall(8200,9500,1000,false));
        dividers.add(functionDimensionsToWall(8200,37500,1000,false));

        dividers.add(functionDimensionsToWall(6100,4000,2100,true));
        dividers.add(functionDimensionsToWall(6100,8000,2100,true));
        dividers.add(functionDimensionsToWall(6100,12000,2100,true));
        dividers.add(functionDimensionsToWall(6100,16000,2100,true));
        dividers.add(functionDimensionsToWall(6100,20000,2100,true));
        dividers.add(functionDimensionsToWall(6100,24000,2100,true));
        dividers.add(functionDimensionsToWall(6100,28000,2100,true));
        dividers.add(functionDimensionsToWall(6100,32000,2100,true));
        dividers.add(functionDimensionsToWall(6100,36000,2100,true));


        return dividers;
    }

    public List getWalls(int width, int height){
        Log.d(TAG, "screen x:" + width + " y:" + height);



        //initialize walls
        walls = new ArrayList<>();

        /*
         * Door width and position variables to set
         */

        // distance between top right of cell 1 and door
        int wallUntilCell1Door = 1000;
        // door width at cell 1
        int cell1DoorWidth = 1500;

        // distance from top right corner of cell 5 (staircase) to door
        int wallUntilCell5Door = 10000;
        // door width at cell 5
        int cell5DoorWidth = 1500;

        // distance of wall between doors of cell 5 and cell 7
        int wallBetween5and7 = 2000;

        // door width at cell 7
        int cell7DoorWidth = 1500;

        // door width at cell 9
        int cell9DoorWidth = 1500;

        // distance between bottom left office's top horizontal wall and cell 9 door
        int cell9Distance = 4000;

        // distance between top left corner of cell 16 and door
        int cell16DoorFromTop = 350;
        int cell16DoorWidth = 1500;

        /*
         * Define walls of floor 4
         */

        //outlines
        walls.add(functionDimensionsToWall(0,0,14300,true));
        walls.add(functionDimensionsToWall(0,0,40000,false));
        walls.add(functionDimensionsToWall(14300,0,40000,false));
        walls.add(functionDimensionsToWall(0,40000,14300,true));

        // Corridor
        walls.add(functionDimensionsToWall(8200,12000,40000,false));
        walls.add(functionDimensionsToWall(6100,4000,40000,false));
        walls.add(functionDimensionsToWall(6100,0,3000,false));
        // Lift
        walls.add(functionDimensionsToWall(0,4100,6100,false));
        walls.add(functionDimensionsToWall(0,20000,6100,false));

        //Left Rooms
        walls.add(functionDimensionsToWall(0,4000,6100,true));
        walls.add(functionDimensionsToWall(3000,0,4000,false));
        walls.add(functionDimensionsToWall(3000,3000,3100,true));
        walls.add(functionDimensionsToWall(0,20000,6100,true));
        walls.add(functionDimensionsToWall(0,24000,6100,true));
        walls.add(functionDimensionsToWall(0,28000,6100,true));
        walls.add(functionDimensionsToWall(0,32000,6100,true));
        walls.add(functionDimensionsToWall(0,36000,6100,true));


        //Right rooms
        walls.add(functionDimensionsToWall(8200,4000,6100,true));
        walls.add(functionDimensionsToWall(8200,8000,6100,true));
        walls.add(functionDimensionsToWall(8200,12000,6100,true));
        walls.add(functionDimensionsToWall(8200,16000,6100,true));
        walls.add(functionDimensionsToWall(8200,20000,6100,true));
        walls.add(functionDimensionsToWall(8200,24000,6100,true));
        walls.add(functionDimensionsToWall(8200,28000,6100,true));
        walls.add(functionDimensionsToWall(8200,32000,6100,true));
        walls.add(functionDimensionsToWall(8200,36000,6100,true));


        return walls;

    }


    /**
     *
     * @param cmFromLeft distance from left floor bound in cm
     * @param cmFromTop distance from top floor bound in cm
     * @param sizeInCm length of the wall in cm
     * @param isHorizontal if its a vertical wall or horizontal line
     * IMPORTANT: the floorplan is 90degrees turned to fit the landscape plan on a portrait screen
     * @return
     */


    private ShapeDrawable functionDimensionsToWall(int cmFromLeft, int cmFromTop, int sizeInCm, boolean isHorizontal){
        ShapeDrawable d = new ShapeDrawable(new RectShape());

        //correct cmFromLeft and cmFromTop for line thinkness
        int cmFromLeftPixelWallThinknessCorrection = (cmFromLeft/this.floorWidthInCm)*10;
        int cmFromTopPixelWallThinknessCorrection = (cmFromTop/this.floorHeightInCm)*10;

        double partial = ((double)cmFromLeft/(double)this.floorWidthInCm);

        int left = (int)(((double)cmFromLeft/(double)this.floorWidthInCm) * (double)this.screenWidth - (double)cmFromLeftPixelWallThinknessCorrection);
        int top = (int) (((double)cmFromTop/(double)this.floorHeightInCm) * (double)this.screenHeight - (double)cmFromTopPixelWallThinknessCorrection);
        int right = (int) ((isHorizontal) ? (((double)cmFromLeft+sizeInCm)/(double)this.floorWidthInCm) * (double)this.screenWidth : (((double)cmFromLeft/(double)this.floorWidthInCm) * (double)this.screenWidth + 10.0));
        int bottom = (int)((!isHorizontal) ? (((double)cmFromTop+sizeInCm)/(double)this.floorHeightInCm) * (double)this.screenHeight : (((double)cmFromTop/(double)this.floorHeightInCm) * (double)this.screenHeight + 10.0));

        d.setBounds(
                left,
                top,
                right,
                bottom);


        return d;
    }

    /**
     *
     * @param left_input distance from left floor bound in cm
     * @param top_input distance from top floor bound in cm
     * @param right_input distance from left floor bound in cm
     * @param bottom_input distance from top floor bound in cm
     * IMPORTANT: the floorplan is 90degrees turned to fit the landscape plan on a portrait screen
     * @return
     */


    private ShapeDrawable functionDimensionsToClosedArea(int left_input, int top_input, int right_input, int bottom_input){
        ShapeDrawable d = new ShapeDrawable(new RectShape());
        d.getPaint().setColor(Color.RED);
        int left = (int)(((double)left_input/(double)this.floorWidthInCm) * (double)this.screenWidth);
        int top = (int) (((double)top_input/(double)this.floorHeightInCm) * (double)this.screenHeight);
        int right = (int)(((double)right_input/(double)this.floorWidthInCm) * (double)this.screenWidth);
        int bottom = (int) (((double)bottom_input/(double)this.floorHeightInCm) * (double)this.screenHeight);

        d.setBounds(
                left,
                top,
                right,
                bottom);


        return d;
    }
}
