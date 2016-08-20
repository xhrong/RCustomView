package com.iflytek.rcustomview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by xhrong on 2016/8/17.
 */
public class MainActivity extends Activity implements View.OnClickListener{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.slideswitch_demo).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.slideswitch_demo:
                Intent slideSwitchIntent=new Intent(MainActivity.this,SlideSwitchDemoActivity.class);
                startActivity(slideSwitchIntent);
                break;
            default:
                break;
        }
    }
}
