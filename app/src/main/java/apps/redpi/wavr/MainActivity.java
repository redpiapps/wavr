package apps.redpi.wavr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public static final String TWO_WAVE = "two_wave";
    public static final String THREE_WAVE = "three_wave";
    public static final String PREF_NAME = "myPref";
    public static final String NONE ="none" ;


    Timer timer;
    private Switch serviceToggle;
    private TextView twoWaveAppName;
    private TextView threeWaveAppName;
    private ImageView twoWaveAppLogo;
    private ImageView threeWaveAppLogo;
    private LinearLayout twoWaveContainer;
    private LinearLayout threeWaveContainer;
    SharedPreferences pref;
    private ArrayList<AppData> appsList;
    private AppsListAdapter appsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();
        setApps();
    }

    private void init() {
        timer = new Timer();
        appsList = new ArrayList<>();
        pref = getSharedPreferences(PREF_NAME,MODE_MULTI_PROCESS);
        serviceToggle = (Switch) findViewById(R.id.serviceToggler);
        serviceToggle.setOnCheckedChangeListener(this);
        twoWaveAppName = (TextView) findViewById(R.id.twoWaveAppName);
        twoWaveAppLogo = (ImageView) findViewById(R.id.twoWaveLogo);
        twoWaveContainer = (LinearLayout)findViewById(R.id.twoWaveContainer);
        twoWaveContainer.setOnClickListener(this);
        threeWaveAppName = (TextView) findViewById(R.id.threeWaveAppName);
        threeWaveAppLogo = (ImageView) findViewById(R.id.threeWaveLogo);
        threeWaveContainer = (LinearLayout)findViewById(R.id.threeWaveContainer);
        threeWaveContainer.setOnClickListener(this);
    }

    private void setApps() {
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        CharSequence appName;

        AppData data = new AppData();
        data.packageName = NONE;
        data.appLogo = ResourcesCompat.getDrawable(getResources(),android.R.drawable.stat_sys_warning,null);
        data.appName = NONE;
        appsList.add(data);
        for (ApplicationInfo packageInfo : packages) {
            if((packageInfo.flags&ApplicationInfo.FLAG_SYSTEM)==1){
                continue;
            }
            appName = packageInfo.loadLabel(pm);
            if (!TextUtils.isEmpty(appName)) {
                AppData tempAppData= new AppData();
                tempAppData.appName = appName;
                tempAppData.packageName = packageInfo.packageName;
                tempAppData.appLogo = packageInfo.loadIcon(pm);
                appsList.add(tempAppData);

            }
        }
        loadDefaults();

        appsListAdapter = new AppsListAdapter(appsList,this);


    }

    private void loadDefaults() {
        String default1 = pref.getString(TWO_WAVE,"");
        String default2 = pref.getString(THREE_WAVE,"");
        AppData defaultAppData;
        if(TextUtils.isEmpty(default1)){
            defaultAppData = appsList.get(1);
            twoWaveAppName.setText(defaultAppData.appName);
            twoWaveAppLogo.setImageDrawable(defaultAppData.appLogo);
        }else if(default1.equals(NONE)){
            defaultAppData = appsList.get(0);
            twoWaveAppName.setText(defaultAppData.appName);
            twoWaveAppLogo.setImageDrawable(defaultAppData.appLogo);
        }
        else{
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(default1, 0);
                twoWaveAppName.setText(appInfo.loadLabel(getPackageManager()));
                twoWaveAppLogo.setImageDrawable(appInfo.loadIcon(getPackageManager()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }

        if(TextUtils.isEmpty(default2)){
            defaultAppData = appsList.get(2);
            threeWaveAppName.setText(defaultAppData.appName);
            threeWaveAppLogo.setImageDrawable(defaultAppData.appLogo);
        }else if(default2.equals(NONE)){
            defaultAppData = appsList.get(0);
            threeWaveAppName.setText(defaultAppData.appName);
            threeWaveAppLogo.setImageDrawable(defaultAppData.appLogo);
        }else{
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(default2, 0);
                threeWaveAppName.setText(appInfo.loadLabel(getPackageManager()));
                threeWaveAppLogo.setImageDrawable(appInfo.loadIcon(getPackageManager()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if (checked) {
            startService(new Intent(this, WaveDetector.class));
            Toast.makeText(this, "Shortcuts enabled", Toast.LENGTH_SHORT).show();
        } else {
            stopService(new Intent(this, WaveDetector.class));

        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.twoWaveContainer:
                promptSelectApp(twoWaveAppName,twoWaveAppLogo,TWO_WAVE);
                break;
            case R.id.threeWaveContainer:
                promptSelectApp(threeWaveAppName,threeWaveAppLogo,THREE_WAVE);
                break;
        }
    }

    public void promptSelectApp(final TextView appName,final ImageView appLogo,final String prefName){
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setTitle("Select App").setAdapter(appsListAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AppData tempData = appsList.get(i);
                appLogo.setImageDrawable(tempData.appLogo);
                appName.setText(tempData.appName);
                getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_MULTI_PROCESS).edit().putString(prefName,tempData.packageName).commit();
                //Toast.makeText(MainActivity.this,appsList.get(i).packageName,Toast.LENGTH_SHORT).show();

            }
        }).show();
    }

}