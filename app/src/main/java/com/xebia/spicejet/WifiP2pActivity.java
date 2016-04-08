package com.xebia.spicejet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class WifiP2pActivity extends ActionBarActivity implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener {
    private Boolean isWifiP2pEnabled = false;
    private Boolean isConnected = false;

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        receiver = new P2PBroadcastReceiver(wifiP2pManager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });

        setContentView(R.layout.device_list_fragment);
    }

    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    protected void setWifiP2pEnabled() {
        isWifiP2pEnabled = true;
    }

    protected void setWifiP2pDisabled() {
        isWifiP2pEnabled = false;
    }

    protected void setConnected() {
        isConnected = true;
    }

    protected void setDisconnected() {
        isConnected = false;
    }

    public void stateChanged(Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            setWifiP2pEnabled();
        } else {
            setWifiP2pDisabled();
        }
    }

    public void peersChanged(Intent intent) {
        wifiP2pManager.requestPeers(channel, this);
    }

    protected void connectionChanged(Intent intent) {
        //WifiP2pInfo p2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        //WifiP2pGroup p2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

        if (networkInfo.isConnected())
            setConnected();
        else
            setDisconnected();
        if (isConnected) {
            //get Connection Info
            wifiP2pManager.requestConnectionInfo(channel, this);
        }

    }

    protected void deviceChanged(Intent intent) {
        //nothing to do.
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info.groupFormed) {

            Intent intent = new Intent(this, ConnectedActivity.class);
            Bundle bundle = new Bundle();

            bundle.putParcelable("Info", info);

            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Toast.makeText(getApplicationContext(), "Peers discovered", Toast.LENGTH_SHORT).show();
        ArrayAdapter<WifiP2pDevice> adapter = new ArrayAdapter<WifiP2pDevice>(this, android.R.layout.simple_list_item_1);
        adapter.addAll(peers.getDeviceList());
        ListView listViewPeers = (ListView) findViewById(R.id.listViewPeers);
        listViewPeers.setAdapter(adapter);
        listViewPeers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pDevice device = (WifiP2pDevice) parent.getItemAtPosition(position);
                connect(device);
            }
        });
    }

    protected void connect(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    protected void disconnect() {
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Disconnect", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Failed to disconnect:" + reason, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
