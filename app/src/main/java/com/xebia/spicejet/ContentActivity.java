package com.xebia.spicejet;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.GridView;


public class ContentActivity extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_media_content);
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this, getBaseContext()));

    }
}
