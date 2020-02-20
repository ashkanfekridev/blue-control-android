package com.example.blueapp;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class ledControl extends Activity {

    static ToggleButton Tbtn_TurnOnOff_LED;

    String address = null;

    BluetoothAdapter bAdapter = null;
    BluetoothDevice  bDevice  = null;
    BluetoothSocket  bSocket = null;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //SPP UUID. Look for it

    ConnectedThread cThread;
    private static BluetoothResponseHandler brHandler;
    private final static int Error = 0;
    private final static int DataIsReady = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);

        Tbtn_TurnOnOff_LED = (ToggleButton)findViewById(R.id.Tbtn_TurnOnOff_LED);
        Tbtn_TurnOnOff_LED.setChecked(false);
        Tbtn_TurnOnOff_LED.setText("LED ????");

        address = getIntent().getStringExtra( "device_address" );

        bAdapter = BluetoothAdapter.getDefaultAdapter();

        try {
            Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();

            if( bSocket == null ) {
                bDevice = bAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                bSocket = bDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                bAdapter.cancelDiscovery();
                bSocket.connect();
            }

            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Connection Failed. Is it a SPP Bluetooth? Try again.", Toast.LENGTH_SHORT).show();
            finish();
        }

        cThread = new ConnectedThread(bSocket);
        cThread.start();

        if (brHandler == null) brHandler = new BluetoothResponseHandler(this);
        else brHandler.setTarget(this);
    }

    public void onClick_Tbtn_TurnOnOff_LED( View v ) {
        if( Tbtn_TurnOnOff_LED.isChecked() ) {
            SendData( "0" );
            Tbtn_TurnOnOff_LED.setText("LED Turned OFF");
        }
        else {
            SendData( "1" );
            Tbtn_TurnOnOff_LED.setText("LED Turned ON");
        }
    }
    public void SendData(String Data) {

        if( bSocket != null ) {
            try {
                bSocket.getOutputStream().write(Data.getBytes());
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error in Send Data", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Bluetooth in Not Connected", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if( bSocket == null ) return;

        if( bSocket.isConnected() ) {
            Disconnect();
        }
    }
    public void onClick_Bluetooth_btn_Disconnect( View v ) {
        Disconnect();
    }
    public void Disconnect() {

        if ( bSocket != null && bSocket.isConnected() ) {
            try  {
                bSocket.close();
                Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
            }
            catch( IOException e ) {
                Toast.makeText(getApplicationContext(), "Error in Disconnecting ", Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    public static class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            //Tell other phone that we have connected
            write("connected".getBytes());
        }

        public void run() {
            byte[] buffer = new byte[512];
            int bytes;
            StringBuilder readMessage = new StringBuilder();

            while( !this.isInterrupted() ) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readed = new String(buffer, 0, bytes);
                    readMessage.append(readed);

                    if (readed.contains("\n")) {
                        brHandler.obtainMessage(ledControl.DataIsReady, bytes, -1, readMessage.toString()).sendToTarget();
                        readMessage.setLength(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i( "ReadData", "22222222222222222222222222");
                    Message msg = brHandler.obtainMessage( ledControl.Error, "" );
                    brHandler.sendMessage(msg);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Call this from the main activity to shutdown the connection
        public void cancel() {
            if (mmInStream != null) {
                try {mmInStream.close();} catch (Exception e) {}
                mmInStream = null;
            }

            if (mmOutStream != null) {
                try {mmOutStream.close();} catch (Exception e) {}
                mmOutStream = null;
            }

            if (mmSocket != null) {
                try {mmSocket.close();} catch (Exception e) {}
                mmSocket = null;
            }

            this.interrupt();
        }
    }

    private static class BluetoothResponseHandler extends Handler {
        private WeakReference<ledControl> mActivity;

        public BluetoothResponseHandler(ledControl activity) {
            mActivity = new WeakReference<ledControl>(activity);
        }

        public void setTarget(ledControl target) {
            mActivity.clear();
            mActivity = new WeakReference<ledControl>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            ledControl activity = mActivity.get();
            String Data =  (String)msg.obj;
            if (activity != null) {
                switch (msg.what) {
                    case DataIsReady :
                        if( Data == null ) return;
                    break;
                }
            }
        }
    }
}