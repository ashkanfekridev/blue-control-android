package com.example.blueapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Set;


public class DeviceList extends Activity
{
    ListView devicelist;
    ToggleButton Tbtn_EnableDisable_Bluetooth;

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        devicelist = (ListView)findViewById(R.id.Bluetooth_lv_PairedDevices);
        Tbtn_EnableDisable_Bluetooth = (ToggleButton) findViewById(R.id.Tbtn_EnableDisable_Bluetooth);
        Tbtn_EnableDisable_Bluetooth.setText( "Enable Bluetooth" );

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if( myBluetooth == null ) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
            finish();
        }

        if( myBluetooth.isEnabled() ) {
            Tbtn_EnableDisable_Bluetooth.setChecked(true);
            Tbtn_EnableDisable_Bluetooth.setText( "Disable Bluetooth" );
        }
        else {
            Tbtn_EnableDisable_Bluetooth.setChecked(false);
            Tbtn_EnableDisable_Bluetooth.setText( "Enable Bluetooth" );
        }
    }

    public void onClick_EnableDisable_Bluetooth( View v ) {
        if( myBluetooth.isEnabled() ) {
            myBluetooth.disable();

            Tbtn_EnableDisable_Bluetooth.setText( "Enable Bluetooth" );
        }
        else {
            Intent turnBTon = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
            startActivityForResult( turnBTon, 1 );

            Tbtn_EnableDisable_Bluetooth.setText( "Disable Bluetooth" );
        }
    }

    public void onClick_Bluetooth_btn_ShowPairedDevices( View v ) {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if( pairedDevices.size() > 0 ) {
            for(BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick( AdapterView<?> av, View v, int position, long id )  {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent i = new Intent(DeviceList.this, ledControl.class);
            i.putExtra("device_address", address);
            setResult(RESULT_OK,i);
            startActivity( i );
        }
    };
}