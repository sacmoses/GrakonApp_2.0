package com.example.cedar.grakontestapp;

/**
 * Created by Cedar on 2/11/2015.
 * Updated by Anders on 5/9/2016.
 */

import com.example.cedar.grakontestapp.lamp;

// Saves ambient lighting info for each mode (ie): Relax, Night Drive, Active, Custom 1..3
public class modeObj {

    private int modeID;
    public lamp objLampL;
    public lamp objLampR;
    public lamp objLamp2;
    public lamp objLamp3;
    private static final int NUM_LAMPS = 7;

    public modeObj(int id) {
        modeID = id;
        objLampL = new lamp();
        objLampR = new lamp();
        objLamp2 = new lamp();
        objLamp3 = new lamp();
    }

    public int getModeID() {
        return modeID;
    }

}
