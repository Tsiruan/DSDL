package tw.edu.ntu.csie.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;

import android.os.Bundle;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity  {
    Button b1,b2,b3,b4,b5;
    private BluetoothAdapter BA;
    private ArrayAdapter<String> mBTArrayAdapter;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;

    ArrayList list = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button)findViewById(R.id.bluetoothOn);
        b2 = (Button)findViewById(R.id.bluetoothOff);
        b3 = (Button)findViewById(R.id.listPaired);
        b4 = (Button)findViewById(R.id.bluetoothVisible);
        b5 = (Button)findViewById(R.id.discover);
        lv = (ListView)findViewById(R.id.listView);
        mBTArrayAdapter = new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1);


        BA = BluetoothAdapter.getDefaultAdapter();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        if (BA == null) {
            // Device doesn't support bluetooth
            Toast.makeText(getApplicationContext(), "Bluetooth unsupported", Toast.LENGTH_LONG).show();
        }
    }



    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                list.add(deviceName);
                final ArrayAdapter adapter = new ArrayAdapter(context,android.R.layout.simple_list_item_1, list);
                lv.setAdapter(adapter);
            }
        }
    };



    // turn on bluetooth
    public void on(View v){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    // turn off bluetooth
    public void off(View v){
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
    }


    // make device visible
    public void visible(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }


    // discover devices
    public void discover(View v){
        list.clear();
        Toast.makeText(getApplicationContext(), "Showing Discovered Devices",Toast.LENGTH_SHORT).show();
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);

        // Check if the device is already discovering
        if(BA.isDiscovering()){
            BA.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(BA.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                BA.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery start", Toast.LENGTH_SHORT).show();
                registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // list paired devices
    public void list(View v){
        pairedDevices = BA.getBondedDevices();

        //ArrayList list = new ArrayList();
        list.clear();

        for(BluetoothDevice bt : pairedDevices) list.add(bt.getName());
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }
}