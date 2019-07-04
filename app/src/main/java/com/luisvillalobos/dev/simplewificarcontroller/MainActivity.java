package com.luisvillalobos.dev.simplewificarcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private HTTPAsyncTask httptask;
    final Handler handler = new Handler();
    int proteccion = 0, tempX = 1, tempVelocidad = 0, tempX2 = 1, tempVelocidad2 = 0;
    boolean cambiar_cancion = false;
    private Button btnCambiarCancion;
    private SeekBar sbVolumen;
    View decorView;
    int uiOptions;
    int valor_slider = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        decorView = getWindow().getDecorView();
// Hide the status bar.
        uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;


        btnCambiarCancion = (Button) findViewById(R.id.btnCancion);
        sbVolumen = (SeekBar) findViewById(R.id.volumen);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        btnCambiarCancion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiar_cancion = true;
            }
        });

        sbVolumen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                valor_slider = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    long map(long x, long in_min, long in_max, long out_min, long out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (proteccion == 2) {
            float x, y;
            String http = "";
            int c, dx, v, color;
            char dy;


            float[] rotationMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(
                    rotationMatrix, sensorEvent.values);

            // Remap coordinate system
            float[] remappedRotationMatrix = new float[16];
            SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedRotationMatrix);

            // Convert to orientations
            float[] orientations = new float[3];
            SensorManager.getOrientation(remappedRotationMatrix, orientations);

            for (int i = 0; i < 3; i++) {
                orientations[i] = (float) (Math.toDegrees(orientations[i]));
            }
            //z = orientations[0];
            x = orientations[2];
            y = orientations[1];

            color = Color.rgb((int) map((long) x, 0, 180, 0, 255),
                    (int) map((long) y, 0, 180, 0, 255),
                    (int) map((long) y, 0, 180, 255, 0));

            GradientDrawable gdBtn = (GradientDrawable) btnCambiarCancion.getBackground();
            gdBtn.setColor(color);

            sbVolumen.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            //sbVolumen.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

            sbVolumen.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);


            if (y >= -15 && y < 25) {
                tempVelocidad = 0;
            } else if (y >= 25 && y < 50) {
                tempVelocidad = 1;
            } else if (y >= 50 && y < 75) {
                tempVelocidad = 2;
            } else if (y >= 75) {
                tempVelocidad = 3;
            } else if (y < -15) {
                tempVelocidad = -1;
            }
            if (x <= 60) {
                tempX = 0;
            } else if (x >= 120) {
                tempX = 2;
            } else {
                tempX = 1;
            }

            btnCambiarCancion.setText(tempVelocidad + "");

            if (tempVelocidad != tempVelocidad2 || tempX2 != tempX || cambiar_cancion) {
                decorView.setSystemUiVisibility(uiOptions);
                if (checkNetworkConnection()) {
                    String numero = "";
                    switch (tempX) {
                        case 0:
                            numero += "1";
                            break;
                        case 1:
                            numero += "2";
                            break;
                        case 2:
                            numero += "3";
                            break;
                    }
                    switch (tempVelocidad) {
                        case -1:
                            numero += "5";
                            break;
                        case 1:
                            numero += "2";
                            break;
                        case 2:
                            numero += "3";
                            break;
                        case 3:
                            numero += "4";
                            break;
                        case 0:
                            numero += "1";
                            break;
                    }
                    if (cambiar_cancion) {
                        //Conseguir el valor del slider
                        switch (valor_slider) {
                            case 0:
                                numero += "0";
                                break;
                            case 1:
                                numero += "1";
                                break;
                            case 2:
                                numero += "2";
                                break;
                            case 3:
                                numero += "3";
                                break;
                        }
                        cambiar_cancion = !cambiar_cancion;
                    }
                    new HTTPAsyncTask().execute("http://192.168.4.1/v=" + numero);
                }
                tempX2 = tempX;
                tempVelocidad2 = tempVelocidad;
            }
            proteccion = 0;
        }
        proteccion++;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Do something here if sensor accuracy changes.

    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    // check network connection
    public boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = false;
        if (networkInfo != null && (isConnected = networkInfo.isConnected())) {
            // show "Connected" & type of network "WIFI or MOBILE"
            //tvIsConnected.setText("Connected "+networkInfo.getTypeName());
            // change background color to red
            //tvIsConnected.setBackgroundColor(0xFF7CCC26);
        } else {
            // show "Not Connected"
            //tvIsConnected.setText("Not Connected");
            // change background color to green
            //tvIsConnected.setBackgroundColor(0xFFFF0000);
        }

        return isConnected;
    }

    class HTTPAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return HttpGet(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //tvResult.setText(result);
        }

        @Override
        protected void onCancelled(String s) {
            Log.e("cancelado", "xd");
            super.onCancelled(s);
        }
    }

    private String HttpGet(String myUrl) throws IOException {
        InputStream inputStream = null;

        Log.e("llega", myUrl);
        URL url = new URL(myUrl);

        // create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // make GET request to the given URL
        conn.connect();

        // receive response as inputStream
        inputStream = conn.getInputStream();


        conn.disconnect();
        return "";
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private static class MyHandler extends Handler {
    }

    private final MyHandler mHandler = new MyHandler();

    public static class MyRunnable implements Runnable {
        private final WeakReference<Activity> mActivity;

        public MyRunnable(Activity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            Activity activity = mActivity.get();
            if (activity != null) {

            }
        }

    }

    private MyRunnable mRunnable = new MyRunnable(this);

}
