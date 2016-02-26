package agi.hackday.lobot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

public class LaunchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView cloudCity = (TextView) findViewById(R.id.launch_title);
        TextView admin = (TextView) findViewById(R.id.launch_administrator);
        final Typeface typeface = Typeface.createFromAsset(getAssets(), "futura-light.otf");
        cloudCity.setTypeface(typeface);
        admin.setTypeface(typeface);
        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                final Intent intent = new Intent("agi.hackday.lobot.view_home");
                Log.e("Splash", "Start Activity");
                startActivity(intent);
            }
        }.start();
    }
}
