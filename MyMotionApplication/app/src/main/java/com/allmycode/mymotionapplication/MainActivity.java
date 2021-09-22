package com.allmycode.mymotionapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements SocketHelper  {


    private static final int PERMISSION_REQUEST_COARSE_LOCATION =121 ;
    private static final int PERMISSION_CODE =1003 ;
    private static final int STATE_MESSAGE_RECIEVED =326442 ;
    private Button connectButton;
    private Button clientButton;
    Context context;
    Activity activity;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> bluetoothDeviceSet;
    BluetoothDevice[] bluetoothDevices;
    BluetoothServerSocket serverSocket;
    ArrayAdapter<String> arrayAdapter;
    ListView listView;
    Button sendbutton;
    MyCommunicationClass myCommunicationClass;

    /**
     * {@inheritDoc}
     * <p>
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    @Override
    protected void onResume() {

        super.onResume();





    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        context = getApplicationContext();
        activity = getParent();
        connectButton=findViewById(R.id.connect);
        clientButton=findViewById(R.id.recieve);
        sendbutton=findViewById(R.id.send_data);
        listView=findViewById(R.id.list_views);
        String[] s;
        final SocketHelper socketHelper=this;


      //  listView.setVisibility(View.INVISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        PERMISSION_CODE);
            }
        }
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled()){
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),179);
        }else {
        bluetoothDeviceSet=bluetoothAdapter.getBondedDevices();
        if(bluetoothDeviceSet.size()!=0) {
            int index =0;
            s=new String[bluetoothDeviceSet.size()];
            bluetoothDevices=new BluetoothDevice[bluetoothDeviceSet.size()];
            for (BluetoothDevice bl : bluetoothDeviceSet) {
                bluetoothDevices[index]=bl;
                Log.e("bluetooth devices", bl.getName());
                s[index]=bl.getName();
                index++;
            }
             arrayAdapter=new ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,s);
            arrayAdapter.notifyDataSetChanged();

        }
        //4c3f49de-0b42-41e7-bdf5-fa8ebb2c17d0
        }

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("happy","onserver clicked");
                    MyConnectionThread myConnectionThread=new MyConnectionThread(bluetoothAdapter,handler,socketHelper);
                    myConnectionThread.start();
            }
        });
        clientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myCommunicationClass!=null)
                {myCommunicationClass.writedata("hai broos".getBytes());}
                else{
                    Log.e("happy_main","no communication possible");
                }

            }
        });
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MyRecieverThread myRecieverThread=new MyRecieverThread(bluetoothDevices[i],bluetoothAdapter,handler,socketHelper);
                myRecieverThread.start();
            }
        });

    }
    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case BluetoothAdapter.STATE_CONNECTING:
                    Toast.makeText(context, "bt connecting", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                    Toast.makeText(context, "bt connected", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    Toast.makeText(context, "bt siconnected", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.ERROR:
                    Toast.makeText(context, "bt error", Toast.LENGTH_SHORT).show();
                    break;
                case STATE_MESSAGE_RECIEVED:
                    String sr=(String) message.obj;
                    String incomi="message: "+message.arg1+" "+message.arg2+" "+sr;
                    Toast.makeText(context,incomi , Toast.LENGTH_SHORT).show();
                    Log.e("happy_recieve",incomi);
                    break;
                default:
                    Log.e("happy_handler","something big is happening");
                    break;

            }
            return true;
        }
    });



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void manageMyCommunication(MyCommunicationClass myCommunicationClass) {
        this.myCommunicationClass=myCommunicationClass;
    }
}


