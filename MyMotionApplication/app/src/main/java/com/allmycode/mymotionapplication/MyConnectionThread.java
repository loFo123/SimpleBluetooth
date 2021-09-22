package com.allmycode.mymotionapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class MyConnectionThread extends Thread {
    private BluetoothServerSocket bluetoothServerSocket;
    Handler handler;
    SocketHelper socketHelper;
    public MyConnectionThread(BluetoothAdapter bluetoothAdapter,Handler handler,SocketHelper socketHelper)  {
        try {
            this.socketHelper=socketHelper;
            this.handler=handler;
            this.bluetoothServerSocket=bluetoothAdapter.listenUsingRfcommWithServiceRecord("mymotionapplication", UUID.fromString("4c3f49de-0b42-41e7-bdf5-fa8ebb2c17d0"));
        }catch (Exception er){
            er.printStackTrace();
        }
    }
    @Override
    public void run() {
        BluetoothSocket bluetoothSocket=null;
        while (true){
            try {
                Message message=new Message();
                message.what=BluetoothAdapter.STATE_CONNECTING;
                bluetoothSocket=bluetoothServerSocket.accept();
                handler.sendMessage(message);

            } catch (IOException e) {
                Message message=new Message();
                message.what=BluetoothAdapter.ERROR;
                handler.sendMessage(message);
                e.printStackTrace();
                break;
            }
            if(bluetoothSocket!=null){
                Message message=new Message();
                message.what=BluetoothAdapter.STATE_CONNECTED;
                handler.sendMessage(message);
                Log.e("myConnectionThread","socket_connected");

                MyCommunicationClass myCommunicationClass=new MyCommunicationClass(bluetoothSocket,handler);
                socketHelper.manageMyCommunication(myCommunicationClass);
                myCommunicationClass.start();
                try {
                    bluetoothServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("myConnectionThread","socket_failed");

                }
            }
        }
        super.run();
    }
    public void cancel(){
        try {
            bluetoothServerSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class MyRecieverThread extends Thread{
    //ClientClass
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;
    Handler handler;
  SocketHelper socketHelper;
    public MyRecieverThread(BluetoothDevice bluetoothDevice,BluetoothAdapter bluetoothAdapter,Handler handler,SocketHelper socketHelper){
        Log.e("happy_recievr","client_constructor");
        this.bluetoothDevice=bluetoothDevice;
        this.bluetoothAdapter=bluetoothAdapter;
        this.handler=handler;
        this.socketHelper=socketHelper;
        try {
            bluetoothSocket=bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("4c3f49de-0b42-41e7-bdf5-fa8ebb2c17d0"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //bluetoothAdapter.cancelDiscovery();

        try {
            Message message=new Message();
            message.what=BluetoothAdapter.STATE_CONNECTING;
            handler.sendMessage(message);
            bluetoothSocket.connect();

            MyCommunicationClass myCommunicationClass=new MyCommunicationClass(bluetoothSocket,handler);
            socketHelper.manageMyCommunication(myCommunicationClass);
            myCommunicationClass.start();


        } catch (IOException e) {
            e.printStackTrace();
            try {
                Message message=new Message();
                message.what=BluetoothAdapter.ERROR;
                handler.sendMessage(message);
                bluetoothSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }
        super.run();

    }
    public void cancel() {
        try {
  // manageMyConnectedSocket(bluetoothSocket);
            bluetoothSocket.close();
        } catch (IOException e) {
            Log.e("Bluetooth_devices", "Could not close the client socket", e);
        }
    }
}
class MyCommunicationClass extends Thread{
    private static final int STATE_MESSAGE_RECIEVED =326442 ;
    final BluetoothSocket bluetoothSocket;
     InputStream  inputStream;
     OutputStream outputStream;
     Handler handler;
    public MyCommunicationClass(BluetoothSocket bluetoothSocket,Handler handler){
        this.bluetoothSocket=bluetoothSocket;
        this.handler=handler;
        try {
            this.inputStream=bluetoothSocket.getInputStream();
            this.outputStream=bluetoothSocket.getOutputStream();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] buffer=new byte[1024];
        int bytes;
        while(true){
            try {
                bytes=inputStream.read(buffer);
                String sng=new String(buffer,0,bytes);
                handler.obtainMessage(STATE_MESSAGE_RECIEVED,bytes,-1,sng).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void writedata(byte[] buffer) {
        try {
            outputStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
interface SocketHelper{
    public void manageMyCommunication(MyCommunicationClass myCommunicationClass);
        }
