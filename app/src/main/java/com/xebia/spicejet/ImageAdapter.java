package com.xebia.spicejet;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private Context baseContext;


    public ImageAdapter(Context c, Context baseContext) {
        mContext = c;
        this.baseContext = baseContext;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v;

        if (convertView == null) { // if it’s not recycled, initialize some attributes
            LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(R.layout.grid_item, null);
            TextView tv = (TextView)v.findViewById(R.id.icon_text);
            tv.setText(mTextsIds[position]);
            ImageView iv = (ImageView)v.findViewById(R.id.icon_image);
            iv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        Log.d("Download", "Downloading...");
                        Toast.makeText(MainActivity.mainContext, "Downloading...", Toast.LENGTH_LONG);
                        String playUrl = getDataSource(urls[position]);
                        Toast.makeText(MainActivity.mainContext, "Download complete " + playUrl, Toast.LENGTH_LONG);
                        ContentRecord contentRecord = new ContentRecord(mTextsIds[position], urls[position], MainActivity.TIMEOUT);
                        contentRecord.setPlayUrl(playUrl);
                        MainActivity.records.put(contentRecord.getName(), contentRecord);
                        Log.d("Download", "Download complete " + playUrl);
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            });
            iv.setPadding(8, 8, 8, 8);
            iv.setImageResource(mThumbIds[position]);
        } else {
            v = (View) convertView;
        }
        return v;
    }

    private String getDataSource(String path) throws IOException {
        if (!URLUtil.isNetworkUrl(path)) {
            return path;
        } else {

            URL url = new URL(path);
            String fl = url.getFile().replace("/", "");
            //String ext = "";
            //if (fl.contains(".") )
              //  ext = fl.substring(fl.lastIndexOf("."), fl.length()).replace(".", "");
            URLConnection cn = url.openConnection();
            cn.connect();
            InputStream cnIs = cn.getInputStream();
            //InputStream stream = cnIs;
            InputStream stream = cnIs;//new CipherInputStream(cnIs, MainActivity.encryptionCipher);

            if (stream == null)
                throw new RuntimeException("stream is null");
            //File temp = File.createTempFile(fl, ext);
            //String tempPath = temp.getAbsolutePath();
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/");
            File file = new File(downloadFolder + "/spice" );
            if (!file.exists()) {
                file.mkdirs();
            }
            File temp = new File(file.getAbsolutePath() + "/" + fl);
            String tempPath = temp.getAbsolutePath();
            FileOutputStream out = new FileOutputStream(temp);
            byte buf[] = new byte[128];
            do {
                int numread = stream.read(buf);
                if (numread <= 0)
                    break;
                out.write(buf, 0, numread);
            } while (true);
            try {
                out.close();;
                stream.close();
                cnIs.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex.getMessage());
            }
            return temp.toURI().toURL().toExternalForm();
        }
    }


    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.content1, R.drawable.content2,
            R.drawable.content3, R.drawable.content4,
            R.drawable.content5, R.drawable.content6,
            R.drawable.content7

    };

    // references to our texts
    private String[] mTextsIds = {
    "Hits of Pritam", "Ready",
            "Dilwale", "Hunger Games",
            "Justice League", "Archies",
            "Steve Jobs"
};

    private String[] urls = {
            "http://192.168.2.181:9090/sample1.3gp",
            "http://192.168.2.181:9090/sample2.3gp",
            "http://192.168.2.181:9090/sample3.3gp",
            "http://192.168.2.181:9090/sample4.3gp",
            "http://192.168.2.181:9090/sample5.3gp",
            "http://192.168.2.181:9090/sample6.3gp",
            "http://192.168.2.181:9090/sample7.3gp"
    };
}
