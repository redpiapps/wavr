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
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.text.TextUtils;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.seismic.ShakeDetector;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nikhil on 09-10-2015.
 */
public class WaveDetector extends IntentService implements SensorEventListener,ShakeDetector.Listener  {
    private SharedPreferences pref;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int sensorChangeCount = 0;
    private TimerTask task;
    private boolean firstWave;
    private boolean secondWave;
    private boolean thirdWave;
    private Handler handler;
    private Timer timer;
    private InterstitialAd interstitial;
    public static final String DEVICE_ID = "238A7894A33CFA10EC340868FE7F242D";
    private Random random;


    public WaveDetector() {
        super("WaveDetector");
        random = new Random();
        handler = new Handler();
        timer = new Timer();
    }

    public void initAd(){
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getString(R.string.inters_id));
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                interstitial.show();
            }
        });

    }
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(DEVICE_ID)
                .build();

        interstitial.loadAd(adRequest);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        pref = getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_MULTI_PROCESS);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);

        initAd();
        while (true) {

        }


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        sensorChangeCount++;

        if (sensorChangeCount == 2) {
            firstWave = true;
            //Toast.makeText(this,"2 Waved detected",Toast.LENGTH_SHORT).show();
        }
        if (sensorChangeCount == 4) {
            secondWave = true;
            //Toast.makeText(this,"2 Waved detected",Toast.LENGTH_SHORT).show();
        } else if (sensorChangeCount == 6) {
            thirdWave = true;
            //Toast.makeText(this,"3 Waved detected",Toast.LENGTH_SHORT).show();

        }

        if (task == null) {
            task = new ResetCount();
            if (isScreenLocked()) {
                if((sensorChangeCount%2)==0)
                timer.schedule(task, 1);
                else{
                    task=null;
                }
            } else {
                timer.schedule(task, 1500);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void hearShake() {
        int shakePref = pref.getInt(MainActivity.SHAKE, 0);
        if(shakePref == 0){
            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }else if(shakePref==1){
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else{
            //do nothing
        }

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
                            //Toast.makeText(getApplicationContext(), "Opening " + pref.getString(MainActivity.THREE_WAVE_APP_NAME,"app"), Toast.LENGTH_SHORT).show();

                            //Toast.makeText(getApplicationContext(), "Opening app...", Toast.LENGTH_SHORT).show();
                            if(random.nextInt(3)==2) {
                                //requestNewInterstitial();
                            }

                        }
                    }
                });

            } else if (secondWave) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String packageName = pref.getString(MainActivity.TWO_WAVE, "");
                        if (!TextUtils.isEmpty(packageName) && !packageName.equals(MainActivity.NONE)) {
                            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                            startActivity(launchIntent);
                            //Toast.makeText(getApplicationContext(), "Opening " + pref.getString(MainActivity.TWO_WAVE_APP_NAME,"app"), Toast.LENGTH_SHORT).show();
                            if(random.nextInt(3)==2) {
                                //requestNewInterstitial();
                            }
                        }

                    }
                });
            } else if (firstWave) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (isScreenLocked() && pref.getInt(MainActivity.ONE_WAVE,0)==0) {
                            PowerManager.WakeLock screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
                            screenLock.acquire();

                            screenLock.release();


                        } else {
                            //it is not locked
                        }

                    }
                });
            }
            thirdWave = false;
            secondWave = false;
            firstWave = false;
        }
    }

    private boolean isScreenLocked() {
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.inKeyguardRestrictedInputMode()) {
            return true;
        } else {
            return false;
        }
    }



    @Override
    public void onTaskRemoved(Intent rootIntent) {
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
