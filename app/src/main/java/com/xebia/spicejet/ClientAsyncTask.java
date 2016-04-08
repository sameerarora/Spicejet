package com.xebia.spicejet;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientAsyncTask extends AsyncTask<Integer, String, Void> {

    private Socket client;
    private int port;
    private InetAddress host;

    private Context context;
    private TextView statusText;

    public ClientAsyncTask(Context context, View statusText, InetAddress host) {
        this.context = context;
        this.statusText = (TextView) statusText;
        this.host = host;
    }

    @Override
    protected Void doInBackground(Integer... params) {

        port = params[0];
        client = new Socket();
        publishProgress("Attempting to connect to: " + host);

        try {
            client.connect((new InetSocketAddress(host, port)), 0);

            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));
            pw.println("test");
            pw.flush();
            client.close();
        } catch (IOException e) {
            publishProgress(e.getMessage());
            e.printStackTrace();
        }

        publishProgress("Connection closed");
        return null;
    }

    protected void onProgressUpdate(String... progress) {
        Toast.makeText(context, progress[0], Toast.LENGTH_SHORT).show();
    }
}
