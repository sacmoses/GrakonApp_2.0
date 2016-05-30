package com.example.cedar.grakontestapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Locale;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/*
* This bit of code houses both the lighting control screen as well as the color picker screen.
* This class calls a BLE wrapper class to handle the connection and communication process with the micro-controller
* It also calls the ColorPicker class to create the color wheel and ring.
* The functions are explained as they appear
*/

public class Chat extends Activity {

    private final static int AMBIENT = 0;
    private final static int FUNCTION = 12;
    private final static int DEMO_MODE = 5;
    private final static int SIS_MODE = 6;
    private final static int NUM_TOGGLE_BUTTONS = 1;
    private final static int ZONE_0 = 0;
    private final static int ZONE_1 = 1;
    private final static int ZONE_2 = 2;
    private final static int ZONE_3 = 3;
    private final static int NUM_ZONES = 4;
    private final static String PERIPHERAL_NAME = "ATMEL-BLE";
	private int RSSI_THRESHOLD = -105;
    private static final int NUM_MODES = 7;
	private TextView tv = null;
    private Dialog mDialog;
	private int activeAmbientMode;
    private boolean inColorSeclector = false;
    private modeObj[] modes; // SAVE ME
    private ColorPicker colorPicker;
    private String[] AmbientModes = {"Relax","Night Drive","Custom 1"
            ,"Custom 2","Custom 3","Demo","Sound Sensor"}; // SAME ME
    private CustomOnItemSelectedListener myListener;
    private BleWrapper mBleWrapper = null;
    private int lastKnownRssi = 0;
    private TextView rssi_value = null;
    private int[] rssi_check = new int[]{0,0,0,0,0,0,0,0,0,0};
    private TextView connect_disconnect_textView;
    boolean inrange = false;
    boolean deviceConnected = false;
    boolean firstConnection = false;
    boolean setAllZones = false;
    private int currentZone = 0;



    private byte PWR_FLAG = (byte)0x80;
    private byte SIS_FLAG = (byte)0x40;
    private byte DEMO_FLAG = (byte)0x20;
    private byte UPD_FLAG = (byte)0x10;
    private byte NO_FLAG = (byte)0x00;
    private byte ON_FLAG = (byte)0x01;
    private byte OFF_FLAG = (byte)0x00;
    byte[] datagram = { NO_FLAG, NO_FLAG, NO_FLAG, NO_FLAG, NO_FLAG, NO_FLAG,NO_FLAG, NO_FLAG, NO_FLAG, NO_FLAG, NO_FLAG, NO_FLAG, NO_FLAG};
    private ArrayList<lamp> lampList;
    private String mDeviceAddress;
    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;


    private boolean modeToggleButtons[];

    private static final UUID
            UUID_GRAKON_SERVICE = UUID.fromString("000000ef-0000-1000-8000-00805f9b34fb"),
            UUID_GRAKON_CHAR_TX = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb"),
            UUID_GRAKON_CHAR_RX = UUID.fromString("713d0002-503e-4c75-ba94-3148f18d941e");


    PropertyChangeListener listener = new PropertyChangeListener() { // Listener
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            assert "color".equals(evt.getPropertyName());
            boolean oldColor = (Boolean) evt.getOldValue();
            boolean newColor = (Boolean) evt.getNewValue();

            Toast.makeText(getApplicationContext(), "Started tracking colorpicker: " + newColor , Toast.LENGTH_SHORT).show();
            //System.err.println(String.format("Property color updated: %b -> %b", oldColor, newColor));
        }
    };

    /**
     * onCreate():  Meat and Potatoes of the setup
     *
     * @param savedInstanceState
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.modes); // Sets the screen to mode screen
        //IMPORT CODE
        rssi_value = (TextView) findViewById(R.id.rssi_value);
        connect_disconnect_textView = (TextView) findViewById(R.id.connect_disconnect_textView);
        if(!deviceConnected){
            showRoundProcessDialog(Chat.this, R.layout.loading_process_dialog_anim);
        } //Code for the Loading Dialog


        mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null() // This function controls how the Bluetooth operates. It's constantly  running in the background.
        {
            public void uiDeviceFound(final BluetoothDevice device,
                                      final int rssi,
                                      final byte[] record) // If  a device is found then function will call the Bluetooth Wrapper Class to connect
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("GRAKON:", "" + rssi);
                        setRSSI(rssi);

                        if (device.getName().equals(PERIPHERAL_NAME)) {
                            boolean status;
                            status = mBleWrapper.connect(device.getAddress().toString());
                            if (!status) {
                                Log.d("DEBUG: ", "Can't connect to " + device.getName());
                            } else if (status)
                                Log.d("DEBUG: ", "Connected to " + device.getName());
//                    firstConnection = true;
                        }
                    }
                });


            }

            public void uiDeviceConnected(final BluetoothGatt gatt,
                                          final BluetoothDevice device) // Once the device is completely connected the app will stop scanning and log the connection
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String test = device.getName();
                        Log.d("CONNECTED: ", "Connected to a device: " + test);
                        deviceConnected = true; // local variable for class to access
                        firstConnection = true;
                        Stoptime();
                    }
                });
            }

            public void uiDeviceDisconnected(final BluetoothGatt gatt,
                                             final BluetoothDevice device) // If the device disconnects it will change local variables to indicate as such
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Disconnected", "device disconnected????");
                        deviceConnected = false;
                        inrange = false;
                        Scantime();
                    }
                });
            }


            public void uiAvailableServices(BluetoothGatt gatt,
                                            BluetoothDevice device,
                                            List<BluetoothGattService> services) // This function tells the phone what sort of messages the lighting controller can accept
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(BluetoothGattService service : mBleWrapper.getCachedServices())
                        {
                            String serviceName = BleNamesResolver.resolveUuid(service.getUuid().toString());
                            Log.d("DEBUG", serviceName);
                        }
                    }
                });

            }

            public void uiNewRssiAvailable(final BluetoothGatt gatt, final BluetoothDevice device, final int rssi) // This function keeps track of the RSSI value sent by the lighting controller
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setRSSI(rssi);
                    }
                });


            }

            @Override
            public void uiNewValueForCharacteristic(BluetoothGatt gatt,
                                                    BluetoothDevice device, BluetoothGattService service,
                                                    BluetoothGattCharacteristic ch, String strValue, int intValue,
                                                    byte[] rawValue, String timestamp) // this function allows the phone to communicate messages to the lighting controller
            {
                super.uiNewValueForCharacteristic(gatt, device, service, ch, strValue, intValue, rawValue, timestamp);
                Log.d("IMPORTANT", "uiNewValueForCharacteristic");
                for (byte b:rawValue)
                {
                    Log.d("IMPORTANT", "Val: " + b);
                }
            }

            public void uiSuccessfulWrite(BluetoothGatt gatt, BluetoothDevice device,
                                          BluetoothGattService service, BluetoothGattCharacteristic ch,
                                          String description) // This function lets the phone know that the lighting controller recieved message correctly
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //BluetoothGattCharacteristic c;
                        Log.d("DEBUG", "Write was successful!!!!!!!!!!!!!!!!!");
                        //transmitting = false;
                    }
                });


            }
        });

        if(!mBleWrapper.checkBleHardwareAvailable()) // If the user's phone does not have Bluetooth Low Energy the application will close
        { finish();}

        // END IMPORT CODE
        // Initialize mode objects.
        modes = new modeObj[NUM_MODES];
        for(int i = 0; i < NUM_MODES; i++){ //Creates the ambient profiles
            modes[i] = new modeObj(i);
        }
        modes[0].objLamp0.setMasterColor(-16776961); // Setting default colors for all the modes
        modes[0].objLamp1.setMasterColor(-16776961); // The Default color of Relax is Blue
        modes[0].objLamp2.setMasterColor(-16776961);
        modes[0].objLamp3.setMasterColor(-16776961);

        modes[1].objLamp1.setMasterColor( -65536); // The Default color of Night Drive is Red
        modes[1].objLamp0.setMasterColor( -65536); //The Default color of Custom Modes is White
        modes[1].objLamp2.setMasterColor( -65536);
        modes[1].objLamp3.setMasterColor( -65536);

        for(int k = 2; k< NUM_MODES; k++){
            modes[k].objLamp0.setMasterColor(-1);
            modes[k].objLamp1.setMasterColor(-1);
            modes[k].objLamp2.setMasterColor(-1);
            modes[k].objLamp3.setMasterColor(-1);
        }

        for(int j= 0; j < NUM_MODES; j++) // Checks to see if user has previously set colors of ambient profiles and changes the color settings accordingly
        {
            if(ReadModeObject(j,"0") != 0)
                modes[j].objLamp0.setMasterColor(ReadModeObject(j,"0"));
            if(ReadModeObject(j,"1") != 0)
                modes[j].objLamp1.setMasterColor(ReadModeObject(j,"1"));
            if(ReadModeObject(j,"2") != 0)
                modes[j].objLamp2.setMasterColor(ReadModeObject(j,"2"));
            if(ReadModeObject(j,"3") != 0)
                modes[j].objLamp3.setMasterColor(ReadModeObject(j,"3"));
        }



        // Initialize toggle buttons
        modeToggleButtons = new boolean[NUM_TOGGLE_BUTTONS];
        for (int i = 0; i < NUM_TOGGLE_BUTTONS; i++)
            modeToggleButtons[i] = false;



        myListener = new CustomOnItemSelectedListener(); // Initializes the Drop-down ambient profile menu
        activeAmbientMode = AMBIENT;


        /* Initialize lamp objects */
        lampList = new ArrayList<lamp>(NUM_ZONES);
        lampList.add(ZONE_0, modes[activeAmbientMode].objLamp0);
        lampList.add(ZONE_1, modes[activeAmbientMode].objLamp1);
        lampList.add(ZONE_2, modes[activeAmbientMode].objLamp2);
        lampList.add(ZONE_3, modes[activeAmbientMode].objLamp3);

        modes_activity();
        Scantime();
	}

	@Override
	protected void onResume() // this function is called when the user reopens the app
    {
		super.onResume();

        //check for ble enabled on each resume
        if(!mBleWrapper.isBtEnabled())
        {
            //not enabled, request user to turn it on
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivity(enableBtIntent);
            finish();
        }
        Scantime();
//        boolean status;
//        status = mBleWrapper.connect(PERIPHERAL_NAME);
//        if (!status) {
//            Log.d("DEBUG: ", "Can't connect to " + PERIPHERAL_NAME);
//        } else if (status)
//            Log.d("DEBUG: ", "Connected to " + PERIPHERAL_NAME);
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

    @Override
    protected void onPause() {
        super.onPause();
        mBleWrapper.stopMonitoringRssiValue();
        mBleWrapper.disconnect();
        mBleWrapper.close();
    }


	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

    // Import Code
    public void Scantime() // This function starts the scan process
    {
        if(mBleWrapper != null)
        {
            mBleWrapper.startScanning();
        }
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    String input = txtSpeechInput.getText().toString();
                    Log.d("SPEECH",input);
                    for(int i = 0; i < NUM_MODES; i++){
                        if(input.equalsIgnoreCase(AmbientModes[i])){
                            activeAmbientMode = i;
                            Spinner spinner = (Spinner)findViewById(R.id.ambient_select_spinner);
                            spinner.setSelection(activeAmbientMode);

                            /* Set the Image Mask Buttons' Colors */
                            ToggleButton t0 = (ToggleButton) findViewById(R.id.color_select_toggle_button);
                            t0.setChecked(true);
                            Button b0 = (Button) findViewById(R.id.zone0);
                            Button b1 = (Button) findViewById(R.id.zone1);
                            Button b2 = (Button) findViewById(R.id.zone2);
                            Button b3 = (Button) findViewById(R.id.zone3);
                            setLightColors(t0.isChecked(),b0,b1,b2,b3);
                            sendLightData();
                        }
                    }
                }
                break;
            }

        }
    }

    public void Stoptime() // This function stops the scan process
    {
        if(mBleWrapper != null)
        {
            mBleWrapper.stopScanning();
        }
    }


    private int RSSI_value_count = 0; // global to maintain value between iteration of function call
    private int[] RSSI_avg_array = new int[10];

    /**
     * avgRssiValues(): Finds average of 10 RSSI values
     *
     * @param rssi
     * @return
     */
    public int avgRssiValues(int rssi){
        if(firstConnection) {
            mDialog.dismiss();
            int numToAvg = 5; // Number of values needed to take average rssi.

            // Check for 8 identical rssi values in a row. If true device has disconnected

            deviceConnected = false;
            for (int i = 0; i < 8; i++)
                if (rssi_check[i] != rssi && !deviceConnected) {
                    deviceConnected = true;
                    rssi_check[i] = rssi;
                }

            if (!deviceConnected)
                inrange = false;

            changeConnectedText(deviceConnected);

            // while the device is connected the app will find the average RSSI value, and send the Arduino an acknowledgement packet every 5 seconds
            if (deviceConnected) {
                if (RSSI_value_count == numToAvg) {
                    int avgRSSI = 0;
                    for (int i = 0; i < numToAvg; i++) {
                        avgRSSI += RSSI_avg_array[i];
                    }
                    avgRSSI = (int) avgRSSI / numToAvg;
                    RSSI_value_count = 0;
                    if (deviceConnected)
                        notifyBLEPeripheral();
                    return avgRSSI;
                } else {
                    RSSI_avg_array[RSSI_value_count] = lastKnownRssi;
                    RSSI_value_count += 1;
                }
            }
        }
        return 0;
    }

    /**
     * changeConnectedText():   This function changes the test at the top of mode screen to reflect
     *                          the connection status of the app
     *
     * @param connected
     */
    public void changeConnectedText(boolean connected)
    {
        if (!connected) {
            rssi_value.setTextColor(Color.RED);
            connect_disconnect_textView.setTextColor(Color.RED);
            connect_disconnect_textView.setText("Disconnected");
            rssi_value.setText("---");
        } else {
            rssi_value.setTextColor(Color.GREEN);
            connect_disconnect_textView.setTextColor(Color.GREEN);
            connect_disconnect_textView.setText("Connected");
        }
    }

    /**
     * checkInRange():  This function checks if the phone has come inside a predetermined range
     *                  (declared at top) and turns on the ambient and overhead lamps if it is in Range.
     *
     * @param rssi
     */
    public void checkInRange(int rssi)
    {
        if(!inrange) {
            if (rssi > RSSI_THRESHOLD) {
                inrange = true;
                modeToggleButtons[AMBIENT] = true;
                sendLightData();
                if(!inColorSeclector){
                    ToggleButton t0 = (ToggleButton) findViewById(R.id.color_select_toggle_button);
                    t0.setChecked(true);

                    Button b0 = (Button) findViewById(R.id.zone0);
                    Button b1 = (Button) findViewById(R.id.zone1);
                    Button b2 = (Button) findViewById(R.id.zone2);
                    Button b3 = (Button) findViewById(R.id.zone3);
                    setLightColors(t0.isChecked(),b0,b1,b2,b3);
                }
            }
        }
        /* This portion handles when the phone goes "out of range" in terms of the RSSI threshold
        We decided not to turn off the lights if the phones goes "out of range" to due the finicky nature of RSSI.
        In our implementation, the lights will only turn off once the user has disconnected to the system
        else {
            if (rssi < RSSI_THRESHOLD) {
                inrange = false;
                modeToggleButtons[0] = false;
                modeToggleButtons[1] = false;
                sendLightData();
                if (!inColorSeclector) {
                    ToggleButton t1 = (ToggleButton) findViewById(R.id.color_select_toggle_button);
                    ToggleButton t2 = (ToggleButton) findViewById(R.id.overhead_toggle_button);
                    t1.setChecked(false);
                    t2.setChecked(false);
                }
            }
        }
        */
    }

    /**
     * setRSSI():   Continuously averages RSSI value
     *
     * @param rssi
     */
    public void setRSSI(int rssi)
    {
        lastKnownRssi = rssi;
        runOnUiThread(new Runnable() {
           @Override
            public void run() {
               //Average 10 RSSI values
               // Add values to array to be averaged
               int avgRSSI = avgRssiValues(lastKnownRssi);
               if (avgRSSI != 0) {
                   final String dataVal = "" + avgRSSI;
                   rssi_value.setText(dataVal);
                   checkInRange(avgRSSI);
               }
           }
        });


    }
    // END IMPORT CODE

    /**
     * onBackPressed(): Handles when the user presses the back buttons
     *                  If the user is in the color selector screen they will taken back to the Lighting control screen
     *                  If the user is in the lighting control screen a dialogue will pop up asking if the user wants to disconnect or stay connected
     *                  If they hit disconnect they will disconnect from the lighting controller and return to main screen
     */
    public void onBackPressed()
    {
        if (inColorSeclector) {
            EditText text = (EditText) findViewById(R.id.current_mode_edit_text);
            saveName(activeAmbientMode, text);
            AmbientModes[activeAmbientMode] = text.getText().toString();
            setContentView(R.layout.modes);
            modes_activity();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Exit Application");
            builder.setMessage("Do you wish to disconnect from the module");
            builder.setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mBleWrapper.disconnect();
                    finish();
                }
            });
            builder.setNegativeButton("Stay Connected", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                   return;
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    /**
     * modes_activity():    Code that controls what happens in the lighting control screen
     */
    public void modes_activity()
    {
        //Checks if the user has previously saved names for their custom profiles
         for(int i = 2; i < NUM_MODES; i++){
           if(readName(i) !=null)
              AmbientModes[i] = readName(i);
        }



        inColorSeclector= false;
        ArrayAdapter<String> stringArrayAdapter = new CustomAdapter(this,
                R.layout.spinner_rows, AmbientModes);
        // Set up spinner
        Spinner spinner = (Spinner)findViewById(R.id.ambient_select_spinner);
        spinner.setAdapter(stringArrayAdapter);
        spinner.setSelection(activeAmbientMode);
        spinner.setOnItemSelectedListener(myListener);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        /* Set the Image Mask Buttons' Colors */
        ToggleButton t0 = (ToggleButton) findViewById(R.id.color_select_toggle_button);
        Button b0 = (Button) findViewById(R.id.zone0);
        Button b1 = (Button) findViewById(R.id.zone1);
        Button b2 = (Button) findViewById(R.id.zone2);
        Button b3 = (Button) findViewById(R.id.zone3);
        setLightColors(t0.isChecked(),b0,b1,b2,b3);

        /* Set listeners for the Image Mask Buttons */
        b0.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                currentZone = ZONE_0;
                Log.d("DEBUG", "Zone 0 has been clicked!.....................");
                edit_button_click(view);
            }
        });
        b1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                currentZone = ZONE_1;
                Log.d("DEBUG", "Zone 1 has been clicked!.....................");
                edit_button_click(view);
            }
        });
        b2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                currentZone = ZONE_2;
                Log.d("DEBUG", "Zone 2 has been clicked!.....................");
                edit_button_click(view);
            }
        });
        b3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                currentZone = ZONE_3;
                Log.d("DEBUG", "Zone 3 has been clicked!.....................");
                edit_button_click(view);
            }
        });

        t0.setChecked(modeToggleButtons[AMBIENT]);

        rssi_value = (TextView) findViewById(R.id.rssi_value);
        connect_disconnect_textView = (TextView) findViewById(R.id.connect_disconnect_textView);

        changeConnectedText(deviceConnected);

    }

// This function allows the user to set the RSSI threshold. It's useful for development purposes, but should not be in the final user version
// There is corresponding button in the modes.xml final
/*    public void RSSI_click(View view){
        EditText editText = (EditText) findViewById(R.id.RSSI_edit_text);
        RSSI_THRESHOLD = Integer.parseInt( editText.getText().toString());
        Toast.makeText(getApplicationContext(), Integer.toString(RSSI_THRESHOLD), Toast.LENGTH_SHORT).show();


    }*/


    /**
     * color_select_activity(): This determines what happens in the color selector screen
     */
    public void color_select_activity()
    {
        setContentView(R.layout.color_picker_screen);
        inColorSeclector = true;
        colorPicker = (ColorPicker) findViewById(R.id.colorPicker);
        // Always initialize color wheel to left ambient lamp.
        colorPicker.setColor(modes[activeAmbientMode].objLamp0.getMasterColor());
        //saveModeObject(activeAmbientMode);
        EditText text = (EditText) findViewById(R.id.current_mode_edit_text);
        text.setText(AmbientModes[activeAmbientMode]);
        colorPicker.addPropertyChangeListener("color", listener);
    }

    /**
     * edit_button_click()  Handles what happens when an image mask button is clicked
     *
     * @param view
     */
    public void edit_button_click(View view) {
        color_select_activity();
    }

    // ON/OFF BUTTONS

    /**
     * ambient_toggle_click():  manages the image mask buttons after the ambient toggle has been clicked,
     *                          then sends information to the BLE peripheral
     *
     * @param view
     */
    public void ambient_toggle_click(View view) {
        Button b0 = (Button) findViewById(R.id.zone0);
        Button b1 = (Button) findViewById(R.id.zone1);
        Button b2 = (Button) findViewById(R.id.zone2);
        Button b3 = (Button) findViewById(R.id.zone3);
        modeToggleButtons[AMBIENT] = ((ToggleButton) view).isChecked();
        setLightColors(modeToggleButtons[AMBIENT], b0, b1, b2, b3);
        sendLightData();
    }

    // END ON/OFF BUTTONS

    /**
     * Class CustomOnItemSelectedListener: Handles clicks to the ambient profile drop-down menu
     */
    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener //
    {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            activeAmbientMode = (int) id;
            /* Set the Image Mask Buttons */
            ToggleButton t0 = (ToggleButton) findViewById(R.id.color_select_toggle_button);
            if(t0.isChecked()) {
                Button b0 = (Button) findViewById(R.id.zone0);
                Button b1 = (Button) findViewById(R.id.zone1);
                Button b2 = (Button) findViewById(R.id.zone2);
                Button b3 = (Button) findViewById(R.id.zone3);
                setLightColors(t0.isChecked(),b0,b1,b2,b3);
            }
            sendLightData();


        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    public void set_all_zones(View view){
        setAllZones = true;
        set_zone(view);
    }
    /**
     * set_zone():  Gathers information from the color picker wheel data and stores it to the lamp
     *              objects belonging to the current ambient mode
     *
     * @param view
     */
    public void set_zone(View view) {
        /* Get the current mode object and the new color to change to */
        modeObj m = modes[activeAmbientMode];
        int newColor = colorPicker.getColor();

        /* If all zones need to be updated with the same color */
        if(setAllZones){
            m.objLamp0.setMasterColor(newColor);
            m.objLamp1.setMasterColor(newColor);
            m.objLamp2.setMasterColor(newColor);
            m.objLamp3.setMasterColor(newColor);
            setAllZones = false;
        }
        /* Otherwise, cherry-pick the zones */
        else{
            switch(currentZone) {
                case ZONE_0:
                    m.objLamp0.setMasterColor(newColor);
                    break;
                case ZONE_1:
                    m.objLamp1.setMasterColor(newColor);
                    break;
                case ZONE_2:
                    m.objLamp2.setMasterColor(newColor);
                    break;
                case ZONE_3:
                    m.objLamp3.setMasterColor(newColor);
            }
        }
        saveModeObject(activeAmbientMode); // Save color data into file
        sendLightData();

    }

    //TODO: update this description
    /**
     * sendLightData(): This function forms the packets that are sent out to the lighting controller
     *                  when light state is changed
     *                  (more info on the packets contained in the 15.2 Project Report. Section 4.2.2)
     */
    public void sendLightData()
    {
        /* Get the gatt server and characteristic information */
        BluetoothGatt gatt;
        BluetoothGattCharacteristic c2;
        gatt = mBleWrapper.getGatt();

        /* If the Ambient light is checked on - Change each lamp/Zone */
        if(modeToggleButtons[AMBIENT]) {

            /* Set the flags and data for the appropriate setting */
            switch (activeAmbientMode) {
                case DEMO_MODE:
                    /* Turn the Demo on */
                    datagram[FUNCTION] = (byte)(DEMO_FLAG | ON_FLAG);
                    break;
                case SIS_MODE:
                    /* Turn the Sound Sensor on */
                    datagram[FUNCTION] = (byte)(SIS_FLAG | ON_FLAG);
                    break;
                default:
                    if(datagram[FUNCTION] == (byte)(PWR_FLAG | OFF_FLAG)) {
                        /* Send UPDATE data to BLE peripheral */
                        datagram[FUNCTION] = UPD_FLAG; // Turn on the lamps
                        try {
                            c2 = gatt.getService(UUID_GRAKON_SERVICE).getCharacteristic(UUID_GRAKON_CHAR_TX);
                            mBleWrapper.writeDataToCharacteristic(c2, datagram);
                            Log.d("DEBUG", "Sent POWER ON datagram");
                            Thread.sleep(200);
                        } catch (NullPointerException e) {
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    /*We're updating, so check the Update Flag*/
                    datagram[FUNCTION] = UPD_FLAG;
                    // Get the lamp objects
                    modeObj mode = modes[activeAmbientMode];
                    lampList.set(ZONE_0, mode.objLamp0);
                    lampList.set(ZONE_1, mode.objLamp1);
                    lampList.set(ZONE_2, mode.objLamp2);
                    lampList.set(ZONE_3, mode.objLamp3);
                    /* For every lamp assign the new data to datagram */
                    int j = 0;
                    for (int i = 0; i < lampList.size(); i++) {
                        datagram[j++] = (byte) lampList.get(i).getR();
                        datagram[j++] = (byte) lampList.get(i).getG();
                        datagram[j++] = (byte) lampList.get(i).getB();
                    }
            }
        }
        /* Otherwise, turn off the lights */
        else if(!modeToggleButtons[AMBIENT]) {
            datagram[FUNCTION] = (byte) (PWR_FLAG | OFF_FLAG);
        }

        /* Send data to BLE peripheral */
        try {
//            while(transmitting);
//            transmitting = true;
            c2 = gatt.getService(UUID_GRAKON_SERVICE).getCharacteristic(UUID_GRAKON_CHAR_TX);
            mBleWrapper.writeDataToCharacteristic(c2, datagram);
            Log.d("DEBUG", "Sent new datagram");
            Thread.sleep(200);
        }
        catch( NullPointerException e ) {  }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * notifyBLEPeripheral():   This function forms the acknowledgement packets sent by the phone to the lighting controller
     *                          See section 4.2.2 of ECE 15.2 project report for more info
     */
    public void notifyBLEPeripheral() //
    //
    {
        try {
            sendLightData();

        } catch (NullPointerException e) {
            Log.d("NULLPOINT", "Avoided exception w/ try catch block");
        }
    }


    /**
     * saveName():  Saves the name of the custom profile in a text file stored locally on the phone
     *
     * @param modeToRead
     * @param text
     */
    private void saveName(int modeToRead, EditText text)
    {
        String filename = AmbientModes[modeToRead]+".txt";
        try {
            FileOutputStream fileout = openFileOutput(filename, MODE_PRIVATE);
            OutputStreamWriter outputwriter = new OutputStreamWriter(fileout);
            outputwriter.write(text.getText().toString());
            outputwriter.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * readName():  Looks for a file with the name of the custom profile
     *              If the file exist then it reads the file and returns the name.
     *              If can't find the file it throws an exception which is caught
     *
     * @param modeToRead
     * @return
     */
    private String readName(int modeToRead)
    {
        String filename = AmbientModes[modeToRead] + ".txt";
        String s = null;
        try {
            FileInputStream filein = openFileInput(filename);
            InputStreamReader InputRead = new InputStreamReader(filein);

            char[] inputBuffer = new char[100];
            s = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return s;
    }

    /**
     * saveModeObject():    Saves the color of the both lamps in text files stored locally on the phone
     *                      converts the color, which is stored as an integer, and converts it to a
     *                      string so it can be saved as a text file.
     *
     * @param currentModeObject
     */
    private void saveModeObject(int currentModeObject)
    {
        String filename;

        filename = "ModeObject" +  Integer.toString(currentModeObject) + "0.txt";
        try {
            FileOutputStream fileout = openFileOutput(filename, MODE_PRIVATE);
            OutputStreamWriter outputwriter = new OutputStreamWriter(fileout);
            outputwriter.write(Integer.toString(modes[currentModeObject].objLamp0.getMasterColor()));
            outputwriter.close();

        }catch(Exception e) {
            e.printStackTrace();
        }

        filename = "ModeObject" +  Integer.toString(currentModeObject) + "1.txt";
        try {
            FileOutputStream fileout = openFileOutput(filename, MODE_PRIVATE);
            OutputStreamWriter outputwriter = new OutputStreamWriter(fileout);
            outputwriter.write(Integer.toString(modes[currentModeObject].objLamp1.getMasterColor()));
            outputwriter.close();

        }catch(Exception e) {
            e.printStackTrace();
        }

        filename = "ModeObject" +  Integer.toString(currentModeObject) + "2.txt";
        try {
            FileOutputStream fileout = openFileOutput(filename, MODE_PRIVATE);
            OutputStreamWriter outputwriter = new OutputStreamWriter(fileout);
            outputwriter.write(Integer.toString(modes[currentModeObject].objLamp2.getMasterColor()));
            outputwriter.close();

        }catch(Exception e) {
            e.printStackTrace();
        }

        filename = "ModeObject" +  Integer.toString(currentModeObject) + "3.txt";
        try {
            FileOutputStream fileout = openFileOutput(filename, MODE_PRIVATE);
            OutputStreamWriter outputwriter = new OutputStreamWriter(fileout);
            outputwriter.write(Integer.toString(modes[currentModeObject].objLamp3.getMasterColor()));
            outputwriter.close();

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ReadModeObject():    Reads the text file that holds the color of each lamp
     *                      It converts the string that it reads and converts it to an integer and
     *                      returns that integer
     *
     * @param currentModeObject
     * @param lamp
     * @return
     */
    private int ReadModeObject(int currentModeObject, String lamp)
    {

        String fileName = "ModeObject" + Integer.toString(currentModeObject) + lamp +".txt";
        String s = null;
        int color = 0;
        try {
            FileInputStream filein = openFileInput(fileName);
            InputStreamReader InputRead = new InputStreamReader(filein);

            char[] inputBuffer = new char[100];
            s = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
        if(s!= null)
            color = Integer.parseInt(s);

        return color;
    }

    /**
     * showRoundProcessDialog():    This function controls the loading dialogue
     *
     * @param mContext
     * @param layout
     */
    public void showRoundProcessDialog(Context mContext, int layout)
    {
        DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_HOME
                        || keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                if(keyCode == KeyEvent.KEYCODE_BACK){
                    dialog.dismiss();
                    finish();
                }
                return false;
            }
        };

        mDialog = new AlertDialog.Builder(mContext).create();
        mDialog.setOnKeyListener(keyListener);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mDialog.setContentView(layout);
    }

    /**
     * setLightColors():    Takes in the image masking buttons and changes the colors depending on the
     *                      input parameters.
     *
     * @param on    boolean indicating whether or not the ambient light toggle button is checked;
     *              if on == true, lights will turn/stay colorful, if on == false, buttons will turn/stay transparent
     * @param b0    button representing zone0
     * @param b1    button representing zone1
     * @param b2    button representing zone2
     * @param b3    button representing zone3
     */
    public void setLightColors(boolean on, Button b0, Button b1, Button b2, Button b3){

        /* If the ambient toggle button is checked, color the image mask buttons */
        if(on) {
            modeObj m = modes[activeAmbientMode];
            b0.setBackgroundColor(Color.argb(0x80, m.objLamp0.getR(), m.objLamp0.getG(), m.objLamp0.getB()));
            b1.setBackgroundColor(Color.argb(0x80, m.objLamp1.getR(), m.objLamp1.getG(), m.objLamp1.getB()));
            b2.setBackgroundColor(Color.argb(0x80, m.objLamp2.getR(), m.objLamp2.getG(), m.objLamp2.getB()));
            b3.setBackgroundColor(Color.argb(0x80, m.objLamp3.getR(), m.objLamp3.getG(), m.objLamp3.getB()));
        }
        /* Otherwise, make the image mask buttons transparent */
        else{
            b0.setBackgroundColor(Color.TRANSPARENT);
            b1.setBackgroundColor(Color.TRANSPARENT);
            b2.setBackgroundColor(Color.TRANSPARENT);
            b3.setBackgroundColor(Color.TRANSPARENT);
        }

        /* If Demo or Sound impact sensor are on, disable the image mask buttons */
        if((activeAmbientMode == DEMO_MODE)||(activeAmbientMode == SIS_MODE)){
            b0.setEnabled(false);
            b1.setEnabled(false);
            b2.setEnabled(false);
            b3.setEnabled(false);
            b0.setClickable(false);
            b1.setClickable(false);
            b2.setClickable(false);
            b3.setClickable(false);
        }
        /* Otherwise, enable them */
        else{
            b0.setEnabled(true);
            b1.setEnabled(true);
            b2.setEnabled(true);
            b3.setEnabled(true);
            b0.setClickable(true);
            b1.setClickable(true);
            b2.setClickable(true);
            b3.setClickable(true);
        }
    }
}
