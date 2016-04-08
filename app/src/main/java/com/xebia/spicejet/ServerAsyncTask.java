package com.xebia.spicejet;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerAsyncTask extends AsyncTask<Integer, String, Void> {

    private ServerSocket server;
    private Socket client;
    private int port;

    private Context context;
    private TextView statusText;
    private InetAddress host;

    public ServerAsyncTask(Context context, View statusText, InetAddress host) {
        this.context = context;
        this.statusText = (TextView) statusText;
        this.host = host;
    }

    @Override
    protected Void doInBackground(Integer... params) {
        try {
            publishProgress("Waiting for connection");
            port = params[0];
            server = new ServerSocket(port);
            publishProgress("Server port opened:"+server.getLocalPort());
            client = server.accept();

            publishProgress("Connected to Client");

            //while(client.isConnected()) {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String temp = bufferedReader.readLine();
            publishProgress(temp);
            //}

            server.close();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        publishProgress("ConnectionClosed");
        return null;
    }

    protected void onProgressUpdate(String... progress) {
        Toast.makeText(context, progress[0], Toast.LENGTH_SHORT).show();
    }

}
