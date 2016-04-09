package com.xebia.spicejet;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import net.sourceforge.zbar.Symbol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class MainActivity extends AppCompatActivity {

    private static final int ZBAR_SCANNER_REQUEST = 0;
    private static final int ZBAR_QR_SCANNER_REQUEST = 1;
    public static Context mainContext;
    public static Map<String, ContentRecord> records = new HashMap<>();
    public static Date referenceDate = new Date();
    public static long TIMEOUT = 60 * 60 * 2 * 1000;
    public static String securityToken = "test12345678";
    public static Cipher encryptionCipher;
    public static Cipher decryptionCipher;
    public static ContentRecord currentlyPlaying;
    public static SecretKey secretKey;
    public static byte[] iv;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private boolean wifiP2pEnabled;
    private P2PBroadcastReceiver receiver;

    // DEFAULT IP
    public static String SERVERIP = "10.100.102.15";

    // DESIGNATE A PORT
    public static final int SERVERPORT = 8080;

    private static ServerSocket serverSocket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mainContext = this;
        encryptionCipher = getEncryptionCipher(securityToken, Cipher.ENCRYPT_MODE);
        decryptionCipher = getDecryptionCipher(securityToken, Cipher.DECRYPT_MODE);

        SERVERIP = getLocalIpAddress();

        Thread fst = new Thread(new ServerThread());
        fst.start();
    }

    public static String[] getRecordList() {
        Set<String> set = records.keySet();
        String[] arr = new String[set.size()];
        return set.toArray(arr);
    }

    public static ContentRecord getRecord(String name) {
        return records.get(name);
    }

    public void launchScanner(View v) {
        if (isCameraAvailable()) {
            Intent intent = new Intent(this, QRCodeScannerActivity.class);
            startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
        } else {
            Toast.makeText(this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

    public Cipher getDecryptionCipher(String key, int mode) {
        Cipher c = null;
        try {
            c = Cipher.getInstance("DES/CFB8/NoPadding");
            c.init(mode, secretKey, new IvParameterSpec(iv));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return c;
    }

    public Cipher getEncryptionCipher(String key, int mode) {
        Cipher c = null;

        try {
            c = Cipher.getInstance("DES/CFB8/NoPadding");
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
            secretKey = skf.generateSecret(desKeySpec);
            c.init(mode, secretKey);
            iv = c.getIV();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return c;
    }

    public void launchPlayer(View v) {
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }

    public void launchDownloaded(View v) {
        Intent intent = new Intent(this, DownloadedActivity.class);
        startActivity(intent);
    }

    public void launchContent(View v) {
        Intent intent = new Intent(this, ContentActivity.class);
        startActivity(intent);
    }


    public void launchQRScanner(View v) {
        if (isCameraAvailable()) {
            Intent intent = new Intent(this, QRCodeScannerActivity.class);
            intent.putExtra(QRCodeConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
            startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
        } else {
            Toast.makeText(this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    public void discoverPeers(View view) {
        Intent intent = new Intent(this, WifiP2pActivity.class);
        startActivity(intent);
    }

    public boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ZBAR_SCANNER_REQUEST:
            case ZBAR_QR_SCANNER_REQUEST:
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra(QRCodeConstants.SCAN_RESULT);
                    //Toast.makeText(this, "Scan Result = " + result, Toast.LENGTH_SHORT).show();
                    ConnectionUtils.connectToNetwork(result, this, mManager, mChannel);


                } else if (resultCode == RESULT_CANCELED && data != null) {
                    String error = data.getStringExtra(QRCodeConstants.ERROR_INFO);
                    if (!TextUtils.isEmpty(error)) {
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //   @Override
//    protected void onStop() {
//        super.onStop();
//        try {
//            serverSocket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public class ServerThread implements Runnable {

        public void run() {
            try {
                if (SERVERIP != null) {
                    serverSocket = new ServerSocket(SERVERPORT);
                    while (true) {
                        // LISTEN FOR INCOMING CLIENTS
                        Socket client = serverSocket.accept();

                        try {
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                            client.getInputStream()));
                            String line = null;
                            while ((line = in.readLine()) != null) {
                                if (line.startsWith("GET")) {
                                    String f = line.replace("GET /?", "").replace(" HTTP/1.1", "").replace("file:", "");
                                    OutputStream sender = client.getOutputStream();
//                                    sender.write("HTTP-Version: HTTP/1.0 200 OK\n".getBytes("UTF-8"));
//                                    sender.write("Content-Type: image/png\n".getBytes("UTF-8"));
//                                    sender.write("Content-Disposition: attachment; filename=\"content.3gp\"\n\n".getBytes("UTF-8"));
                                    FileInputStream input = new FileInputStream("/storage/emulated/0/Download/sample4.3gp");
                                    FileInputStream fileinput = new FileInputStream(f);
                                    OutputStream output = sender;//new CipherOutputStream(sender, MainActivity.decryptionCipher);
                                    byte buf[] = new byte[128];
                                    do {
                                        int numread = fileinput.read(buf);
                                        if (numread <= 0)
                                            break;
                                        output.write(buf, 0, numread);
                                    } while (true);
                                    output.close();
                                    //sender.close();
                                    fileinput.close();

                                    //OutputStream sender  = new FileOutputStream(f);
                                    //OutputStream stream = new CipherOutputStream(sender, MainActivity.decryptionCipher);
                                    //while(stream.)
                                }
                                Log.d("ServerActivity", line);
                            }
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
