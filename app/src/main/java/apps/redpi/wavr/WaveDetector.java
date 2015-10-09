package apps.redpi.wavr;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

/**
 * Created by Nikhil on 09-10-2015.
 */
public class WaveDetector extends IntentService implements SensorEventListener {
    private SharedPreferences pref;
    private  SensorManager mSensorManager;
    private  Sensor mSensor;
    private int sensorChangeCount=0;
    private TimerTask task;
    private boolean firstWave;
    private boolean secondWave;
    private boolean thirdWave;
    private Handler handler;
    private Timer timer;

    public WaveDetector() {
        super("WaveDetector");

        handler = new Handler();
        timer = new Timer();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        pref =getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_MULTI_PROCESS);

        while (true){

        }


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        sensorChangeCount++;

        if(sensorChangeCount==2){
            firstWave=true;
            //Toast.makeText(this,"2 Waved detected",Toast.LENGTH_SHORT).show();
        }
        if(sensorChangeCount==4){
            secondWave=true;
            //Toast.makeText(this,"2 Waved detected",Toast.LENGTH_SHORT).show();
        } else if (sensorChangeCount == 6) {
            thirdWave = true;
            //Toast.makeText(this,"3 Waved detected",Toast.LENGTH_SHORT).show();

        }

        if (task == null) {
            task = new ResetCount();
            timer.schedule(task, 1500);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class ResetCount extends TimerTask {

        @Override
        public void run() {
            sensorChangeCount = 0;
            task = null;
            if (thirdWave) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String packageName = pref.getString(MainActivity.THREE_WAVE, "");
                        if (!TextUtils.isEmpty(packageName) && !packageName.equals(MainActivity.NONE)) {
                            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                            startActivity(launchIntent);
                            Toast.makeText(getApplicationContext(), "3 Waved detected", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            } else if (secondWave) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String packageName = pref.getString(MainActivity.TWO_WAVE, "");
                        if(!TextUtils.isEmpty(packageName) && !packageName.equals(MainActivity.NONE)) {
                            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                            startActivity(launchIntent);
                        }

                    }
                });
            }else if(firstWave){

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                        if( myKM.inKeyguardRestrictedInputMode()) {
                            PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
                            long l = SystemClock.uptimeMillis();
                            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"").acquire();
                        } else {
                            //it is not locked
                        }
                        Toast.makeText(getApplicationContext(), "1 Waved detected", Toast.LENGTH_SHORT).show();

                    }
                });
            }
            thirdWave=false;
            secondWave=false;
            firstWave=false;
        }
    }
    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }
}
