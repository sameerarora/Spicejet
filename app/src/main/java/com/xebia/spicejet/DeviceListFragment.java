package com.xebia.spicejet;


import android.app.Fragment;
import android.net.wifi.p2p.WifiP2pDevice;

import java.util.ArrayList;
import java.util.List;

public class DeviceListFragment extends Fragment {

    private List<WifiP2pDevice> devices = new ArrayList<>();

    public void updateThisDevice(WifiP2pDevice wifiP2pDevice) {
        devices.add(wifiP2pDevice);
    }
}
