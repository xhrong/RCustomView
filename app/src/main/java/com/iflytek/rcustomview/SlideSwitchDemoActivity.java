package com.iflytek.rcustomview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.iflytek.rcustomview.slideswitch.SlideSwitch;

/**
 * Created by xhrong on 2016/8/17.
 */
public class SlideSwitchDemoActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.slideswitch_demo);

        ((SlideSwitch) findViewById(R.id.sw_circle)).setSlideListener(new SlideSwitch.SlideListener() {
            @Override
            public void open() {
                Toast.makeText(SlideSwitchDemoActivity.this, "Open", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void close() {
                Toast.makeText(SlideSwitchDemoActivity.this, "Close", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
