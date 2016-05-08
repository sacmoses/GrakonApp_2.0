package com.example.cedar.grakontestapp;

/**
 * Created by Cedar on 2/11/2015.
 */

import com.example.cedar.grakontestapp.lamp;

// Saves ambient lighting info for each mode (ie): Relax, Night Drive, Active, Custom 1..3
public class modeObj {

    private int modeID;
    public lamp objLampL;
    public lamp objLampR;
    private static final int NUM_LAMPS = 7;

    public modeObj(int id) {
        modeID = id;
        objLampL = new lamp();
        objLampR = new lamp();
    }

    public int getModeID() {
        return modeID;
    }

}
