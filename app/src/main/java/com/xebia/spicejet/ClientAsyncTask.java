package com.xebia.spicejet;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
        int len;
        port = params[0];
        byte [] buf = new byte[1024];
        client = new Socket();
        publishProgress("Attempting to connect to: " + host);

        try {
            /*client.connect((new InetSocketAddress(host, port)), 0);

            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));
            pw.println("test");
            pw.flush();
            client.close();*/
            client.bind(null);
            client.connect((new InetSocketAddress(host, port)), 500);

            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data will be retrieved by the server device.
             */
            OutputStream outputStream = client.getOutputStream();
            InputStream inputStream = client.getInputStream();
            //ContentResolver cr = context.getContentResolver();
            //InputStream inputStream = new ByteArrayInputStream("shared_content".getBytes());
            //inputStream = cr.openInputStream(Uri.parse("path/to/picture.jpg"));
            //while ((len = inputStream.read(buf)) != -1) {
             //   outputStream.write(buf, 0, len);
            //}
            outputStream.write("shared_conent_clinet\n".getBytes());
            outputStream.flush();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String temp = bufferedReader.readLine();
            MainActivity.sharedRecords.put(temp, new ContentRecord(temp, temp, 1l));
            outputStream.close();
            inputStream.close();

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
