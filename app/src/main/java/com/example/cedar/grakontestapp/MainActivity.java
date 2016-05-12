package com.example.cedar.grakontestapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 *This is the code for the main screen of the application.
 *Before doing anything else, it will check to make sure the phone's Bluetooth adapter is turned on.
 *If not the user will be prompted to turn it on.
 *Once the user enables Bluetooth they will see the main screen
 */

public class MainActivity extends Activity {

    @TargetApi(19)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //deprecated
        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) // If user phone's bluetooth is not enabled, this function will be called to allow them to enable it
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,1);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();


    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        //outState.putString(savedName, name);
    }

    public void bluetooth_settings_click(View view) {
        Intent bluetoothIntent = new Intent(this, Chat.class); // When the user hits the CONNECT BLUETOOTH button the connection process will begin
        startActivity(bluetoothIntent);
    }

}
