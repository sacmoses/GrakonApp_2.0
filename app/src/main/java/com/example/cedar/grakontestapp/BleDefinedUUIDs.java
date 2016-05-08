package com.example.cedar.grakontestapp;

/**
 * Created by delpa on 4/3/2015.
 */

import java.util.UUID;

public class BleDefinedUUIDs {

    public static class Service {
        final static public UUID HEART_RATE               = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    };

    public static class Characteristic {
        final static public UUID HEART_RATE_MEASUREMENT   = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
        final static public UUID MANUFACTURER_STRING      = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
        final static public UUID MODEL_NUMBER_STRING      = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
        final static public UUID FIRMWARE_REVISION_STRING = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
        final static public UUID APPEARANCE               = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
        final static public UUID BODY_SENSOR_LOCATION     = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb");
        final static public UUID BATTERY_LEVEL            = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

        //=====IS THIS NECESSARY??????=====
        final static public UUID CLIENT_CHARACTERISTIC_CONFIG
                                                          = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        final static public UUID BLE_SHIELD_TX            = UUID.fromString("713d0003-503e-4c75-ba94-3148f18d941e");
        final static public UUID BLE_SHIELD_RX            = UUID.fromString("713d0002-503e-4c75-ba94-3148f18d941e");
        final static public UUID BLE_SHIELD_SERVICE       = UUID.fromString("713d0000-503e-4c75-ba94-3148f18d941e");


    }

    public static class Descriptor {
        final static public UUID CHAR_CLIENT_CONFIG       = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

}
