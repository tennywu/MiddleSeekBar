package com.tenny.middleseekbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tenny.bar.MiddleSeekBar;

public class MainActivity extends AppCompatActivity {

    private MiddleSeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBar= (MiddleSeekBar) findViewById(R.id.seekBar_edit);
        seekBar.setOnSeekBarChangeListener(new MiddleSeekBar.onSeekBarChangeListener() {
            @Override
            public void onProgressChanged(MiddleSeekBar doubleSeekBar, float secondThumbRatio) {

            }
        });

    }
}
