package com.xebia.spicejet;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import java.net.Socket;

public class ConnectedActivity extends ActionBarActivity {


    Socket client;
    String host;
    int port = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_connected);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        WifiP2pInfo info = bundle.getParcelable("Info");

        /*String[] shared_contents = bundle.getStringArray("shared_content");
        for (String key : shared_contents) {
            MainActivity.sharedRecords.put(key, new ContentRecord(key, key, 1l));
        }*/
      //  intent = new Intent(this, SharedContentActivity.class);
      //  startActivity(intent);

        //TextView tv = (TextView) findViewById(R.id.textViewPeerName);

        if (info.isGroupOwner) {
            ServerAsyncTask serverAsyncTask = new ServerAsyncTask(getApplicationContext(), findViewById(R.id.textViewPeerName), info.groupOwnerAddress);
            serverAsyncTask.execute(port);
        } else {
            ClientAsyncTask clientAsyncTask = new ClientAsyncTask(getApplicationContext(), findViewById(R.id.textViewPeerName), info.groupOwnerAddress);
            clientAsyncTask.execute(port);
        }
    }

}
