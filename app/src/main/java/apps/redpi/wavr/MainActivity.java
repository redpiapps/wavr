package apps.redpi.wavr;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener{

    public static final String ONE_WAVE = "one_wave";
    public static final String TWO_WAVE = "two_wave";
    public static final String THREE_WAVE = "three_wave";
    public static final String PREF_NAME = "myPref";
    public static final String NONE = "nothing";
    private static final String FIRST_TIME = "first_time";
    private static final String SERVICE_STARTED = "service_started";
    public static final String TWO_WAVE_APP_NAME = "two_wave_app_name" ;
    public static final String THREE_WAVE_APP_NAME = "three_wave_app_name" ;
    public static final String SHAKE = "shake";
    private boolean helpShown = false;
    public static CharSequence[] options = new CharSequence[]{"unlock screen", "do nothing"};
    public static CharSequence[] shakeOptions = new CharSequence[]{"open alarm", "open dialer","do nothing"};


    Timer timer;
    private Switch serviceToggle;
    private TextView oneWaveFnctionality;
    private TextView shakeFunctionality;
    private TextView twoWaveAppName;
    private TextView threeWaveAppName;
    private ImageView twoWaveAppLogo;
    private ImageView threeWaveAppLogo;
    private LinearLayout oneWaveContainer;
    private LinearLayout shakeContainer;
    private LinearLayout twoWaveContainer;
    private LinearLayout threeWaveContainer;
    SharedPreferences pref;
    private ArrayList<AppData> appsList;
    private AppsListAdapter appsListAdapter;
    private ImageView handImage;
    private ImageView mobileImage;
    private TextView helpInstruction;
    private float effectiveLeft;
    private float right;
    private ObjectAnimator handAnimation;
    private ImageView closeHelp;
    private CardView helpContainer;
    private RelativeLayout helpTitleContainer;


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
        pref = getSharedPreferences(PREF_NAME, MODE_MULTI_PROCESS);
        serviceToggle = (Switch) findViewById(R.id.serviceToggler);
        serviceToggle.setOnCheckedChangeListener(this);
        oneWaveFnctionality = (TextView) findViewById(R.id.oneWaveFunctionality);
        shakeFunctionality = (TextView) findViewById(R.id.shakeFunctionality);
        twoWaveAppName = (TextView) findViewById(R.id.twoWaveAppName);
        twoWaveAppLogo = (ImageView) findViewById(R.id.twoWaveLogo);
        oneWaveContainer = (LinearLayout) findViewById(R.id.oneWaveContainer);
        oneWaveContainer.setOnClickListener(this);
        shakeContainer = (LinearLayout) findViewById(R.id.shakeContainer);
        shakeContainer.setOnClickListener(this);
        twoWaveContainer = (LinearLayout) findViewById(R.id.twoWaveContainer);
        twoWaveContainer.setOnClickListener(this);
        threeWaveAppName = (TextView) findViewById(R.id.threeWaveAppName);
        threeWaveAppLogo = (ImageView) findViewById(R.id.threeWaveLogo);
        threeWaveContainer = (LinearLayout) findViewById(R.id.threeWaveContainer);
        threeWaveContainer.setOnClickListener(this);
        handImage = (ImageView) findViewById(R.id.handImage);
        mobileImage = (ImageView) findViewById(R.id.mobileImage);
        closeHelp = (ImageView) findViewById(R.id.closeHelp);
        helpInstruction = (TextView) findViewById(R.id.helpInstr);
        helpContainer = (CardView) findViewById(R.id.helpContainer);
        helpTitleContainer = (RelativeLayout) findViewById(R.id.helpTitleContainer);
        helpTitleContainer.setOnClickListener(this);

        if (!pref.getBoolean(FIRST_TIME, false)) {
            pref.edit().putBoolean(FIRST_TIME, true).apply();
            helpShown = true;
            serviceToggle.setChecked(true);
        } else {
            serviceToggle.setChecked(pref.getBoolean(SERVICE_STARTED, true));
            helpShown = false;
        }
        initAd();
    }

    private void initAd() {
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(WaveDetector.DEVICE_ID).build();
        mAdView.loadAd(adRequest);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (helpShown) {
            showhelp();
            animateInit();
        }
    }

    private void animateInit() {

        float left = mobileImage.getX();
        float top = mobileImage.getY() - 20;
        handImage.setY(top);
        effectiveLeft = left - getResources().getDimension(R.dimen.hand_size);
        right = left + getResources().getDimension(R.dimen.mobile_size);
        startAnim1();

    }


    private void startAnim1() {
        handAnimation = ObjectAnimator.ofFloat(handImage, "X", effectiveLeft, right);
        handAnimation.setDuration(400);
        handAnimation.setStartDelay(1000);

        handAnimation.start();
        handAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                helpInstruction.setText("1 wave");
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                startAnim2();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void startAnim2() {
        handAnimation.cancel();
        handAnimation.removeAllListeners();
        handAnimation = ObjectAnimator.ofFloat(handImage, "X", effectiveLeft, right);
        handAnimation.setDuration(400);
        handAnimation.setRepeatCount(1);
        handAnimation.setRepeatMode(ObjectAnimator.REVERSE);
        handAnimation.setStartDelay(1000);
        handAnimation.start();
        handAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                helpInstruction.setText("2 waves");
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                startAnim3();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void startAnim3() {
        handAnimation.cancel();
        handAnimation.removeAllListeners();
        handAnimation = ObjectAnimator.ofFloat(handImage, "X", effectiveLeft, right);
        handAnimation.setDuration(400);
        handAnimation.setRepeatCount(2);
        handAnimation.setRepeatMode(ObjectAnimator.REVERSE);
        handAnimation.setStartDelay(1000);
        handAnimation.start();
        handAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                helpInstruction.setText("3 waves");
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                startAnim1();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void setApps() {
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        CharSequence appName;

        AppData data = new AppData();
        data.packageName = NONE;
        data.appLogo = ResourcesCompat.getDrawable(getResources(), android.R.drawable.stat_sys_warning, null);
        data.appName = NONE;
        appsList.add(data);
        for (ApplicationInfo packageInfo : packages) {
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                continue;
            }
            appName = packageInfo.loadLabel(pm);
            if (!TextUtils.isEmpty(appName)) {
                AppData tempAppData = new AppData();
                tempAppData.appName = appName;
                tempAppData.packageName = packageInfo.packageName;
                tempAppData.appLogo = packageInfo.loadIcon(pm);
                appsList.add(tempAppData);

            }
        }
        loadDefaults();

        appsListAdapter = new AppsListAdapter(appsList, this);


    }

    private void loadDefaults() {
        int default1 = pref.getInt(ONE_WAVE, -1);
        int defaultShake = pref.getInt(SHAKE, -1);
        String default2 = pref.getString(TWO_WAVE, "");
        String default3 = pref.getString(THREE_WAVE, "");
        AppData defaultAppData;

        if (default1 == -1) {
            oneWaveFnctionality.setText(options[0]);
            pref.edit().putInt(ONE_WAVE,0).commit();

        } else {
            oneWaveFnctionality.setText(options[default1]);
        }

        if (defaultShake == -1) {
            shakeFunctionality.setText(shakeOptions[0]);
            pref.edit().putInt(SHAKE,0).commit();

        } else {
            shakeFunctionality.setText(shakeOptions[defaultShake]);
        }

        if (TextUtils.isEmpty(default2)) {
            defaultAppData = appsList.get(1);
            twoWaveAppName.setText(defaultAppData.appName);
            twoWaveAppLogo.setImageDrawable(defaultAppData.appLogo);
            pref.edit().putString(TWO_WAVE,defaultAppData.packageName).commit();
            pref.edit().putString(TWO_WAVE_APP_NAME,defaultAppData.packageName).commit();

        } else if (default2.equals(NONE)) {
            defaultAppData = appsList.get(0);
            twoWaveAppName.setText(defaultAppData.appName);
            twoWaveAppLogo.setImageDrawable(defaultAppData.appLogo);
        } else {
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(default2, 0);
                twoWaveAppName.setText(appInfo.loadLabel(getPackageManager()));
                twoWaveAppLogo.setImageDrawable(appInfo.loadIcon(getPackageManager()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }

        if (TextUtils.isEmpty(default3)) {
            defaultAppData = appsList.get(2);
            threeWaveAppName.setText(defaultAppData.appName);
            threeWaveAppLogo.setImageDrawable(defaultAppData.appLogo);
            pref.edit().putString(THREE_WAVE,defaultAppData.packageName).commit();
            pref.edit().putString(THREE_WAVE_APP_NAME,defaultAppData.packageName).commit();


        } else if (default3.equals(NONE)) {
            defaultAppData = appsList.get(0);
            threeWaveAppName.setText(defaultAppData.appName);
            threeWaveAppLogo.setImageDrawable(defaultAppData.appLogo);
        } else {
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(default3, 0);
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
        if (id == R.id.rate) {
            rateApp();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |

                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if (checked) {
            startService(new Intent(this, WaveDetector.class));
            pref.edit().putBoolean(SERVICE_STARTED, true).apply();
            Toast.makeText(this, "Shortcuts enabled", Toast.LENGTH_SHORT).show();
        } else {
            stopService(new Intent(this, WaveDetector.class));
            pref.edit().putBoolean(SERVICE_STARTED, false).apply();


        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.oneWaveContainer:
                promptSelectOption();
                break;
            case R.id.shakeContainer:
                promptShakeSelectOption();
                break;
            case R.id.twoWaveContainer:
                promptSelectApp(twoWaveAppName, twoWaveAppLogo, TWO_WAVE,TWO_WAVE_APP_NAME);
                break;
            case R.id.threeWaveContainer:
                promptSelectApp(threeWaveAppName, threeWaveAppLogo, THREE_WAVE,THREE_WAVE_APP_NAME);
                break;
            case R.id.helpTitleContainer:
                if (helpShown) {
                    hideHelp();
                } else {
                    showhelp();
                    animateInit();
                }
                break;
        }
    }

    private void promptShakeSelectOption() {
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setItems(shakeOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                shakeFunctionality.setText(shakeOptions[i]);
                getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_MULTI_PROCESS).edit().putInt(SHAKE, i).commit();
                //Toast.makeText(MainActivity.this,appsList.get(i).packageName,Toast.LENGTH_SHORT).show();

            }
        }).show();

    }

    private void hideHelp() {
        handAnimation.cancel();
        handAnimation.removeAllListeners();
        helpShown = false;
        closeHelp.setVisibility(View.GONE);
        ValueAnimator anim = ValueAnimator.ofInt(helpContainer.getMeasuredHeight(), 0);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = helpContainer.getLayoutParams();
                layoutParams.height = val;
                helpContainer.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(300);
        anim.start();

    }

    private void showhelp() {
        helpShown = true;
        //animateInit();
        closeHelp.setVisibility(View.VISIBLE);
        helpContainer.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ValueAnimator anim = ValueAnimator.ofInt(0, helpContainer.getMeasuredHeight());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Integer val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = helpContainer.getLayoutParams();
                layoutParams.height = val;
                helpContainer.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(300);
        anim.start();

    }

    private void promptSelectOption() {

        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                oneWaveFnctionality.setText(options[i]);
                getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_MULTI_PROCESS).edit().putInt(ONE_WAVE, i).commit();
                //Toast.makeText(MainActivity.this,appsList.get(i).packageName,Toast.LENGTH_SHORT).show();

            }
        }).show();
    }

    public void promptSelectApp(final TextView appName, final ImageView appLogo, final String prefPkgName,final String prefAppName) {
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setTitle("Select App").setAdapter(appsListAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AppData tempData = appsList.get(i);
                appLogo.setImageDrawable(tempData.appLogo);
                appName.setText(tempData.appName);
                getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_MULTI_PROCESS).edit().putString(prefPkgName, tempData.packageName).commit();
                getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_MULTI_PROCESS).edit().putString(prefAppName, tempData.appName.toString()).commit();
                //Toast.makeText(MainActivity.this,appsList.get(i).packageName,Toast.LENGTH_SHORT).show();

            }
        }).show();
    }


}
