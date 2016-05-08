package com.example.cedar.grakontestapp;

//Alex R. Delp
//Dec 3, 2014
//RGB LED Lamp Object
//V1.0

import android.graphics.Color;

/**
 * This is an object representing an RGB LED Lamp. It keeps track of the
 * on/off state of the lamp, as well as the values of the Red, Green, and
 * Blue channels. This version assumes 8 bit channel values (0 to 255) and
 * the values accepted are correspondingly limited. If you wish to use a higher
 * channel resolution, simply edit the MAX_CHANNEL_VAL constant to be the
 * appropriate number, such as 1024.
 */
public class lamp
{

    //data members
   // private int LampID;
    private int RedVal;
    private int GreenVal;
    private int BlueVal;
    private int masterColor;
    private boolean on;

    //constants
    private static final int MIN_CHANNEL_VAL = 0;
    private static final int MAX_CHANNEL_VAL = 255;


    /*
     * Constructs and returns a Lamp object. The default state is RGB=0,0,0, with the lamp
     * set to "off".
     *
     * @param id A number identifying the Lamp object. This will likely just be a number
     * from 0 to the number of lamps in whatever structure this object is being used.
     */

    public lamp()
    {
        RedVal = 0;
        GreenVal = 0;
        BlueVal = 0;
        on = false;
    }

    //Mutators

    /*
     * This function controls the red channel of the lamp. Values exceeding the 0 to
     * 255 range are capped.
     *
     * @param r An integer from 0 to 255 representing the
     * brightness of the red channel of an RGB LED lamp.
     */
    public void setMasterColor(int color){
        masterColor = color;
        setR(Color.red(color));
        setG(Color.green(color));
        setB(Color.blue(color));
    }
    private void setR(int r)
    {
        RedVal = r;
        if( r > MAX_CHANNEL_VAL){ r = MAX_CHANNEL_VAL; }
        if( r < MIN_CHANNEL_VAL){ r = MIN_CHANNEL_VAL; }
    }

    /**
     * This function controls the green channel of the lamp. Values exceeding the 0 to
     * 255 range are capped.
     *
     * @param g An integer from 0 to 255 representing the
     * brightness of the green channel of an RGB LED lamp.
     */
    private void setG(int g)
    {
        GreenVal = g;
        if( g > MAX_CHANNEL_VAL){ g = MAX_CHANNEL_VAL; }
        if( g < MIN_CHANNEL_VAL){ g = MIN_CHANNEL_VAL; }
    }
    /**
     * This function controls the blue channel of the lamp. Values exceeding the 0 to
     * 255 range are capped.
     *
     * @param b An integer from 0 to 255 representing the
     * brightness of the blue channel of an RGB LED lamp.
     */
    private void setB(int b)
    {
        BlueVal = b;
        if( b > MAX_CHANNEL_VAL){ b = MAX_CHANNEL_VAL; }
        if( b < MIN_CHANNEL_VAL){ b = MIN_CHANNEL_VAL; }
    }

    /**
     * Set the internal state of the lamp to off.
     */
    public void off(){ on = false; }

    /**
     * Set the internal state of the lamp to on.
     */
    public void on() { on = true;  }

    //Accesors
    public int getMasterColor(){return masterColor;};
    public int getR(){ return RedVal; }
    public int getG(){ return GreenVal; }
    public int getB(){ return BlueVal; }

    /**
     * @return A boolean describing the on/off state of the lamp.
     */
    public boolean getState(){ return on; }

    /**,
     * @return An integer describing the ID number of the lamp. This is a number
     * from 0 to the total number of lamps in the array in which the lamp was
     * created.
     */
    //public int getID(){ return LampID; }

}