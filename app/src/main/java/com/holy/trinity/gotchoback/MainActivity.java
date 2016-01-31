package com.holy.trinity.gotchoback;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.os.CountDownTimer;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.TimeUnit;
import android.telephony.SmsManager;
//

public class MainActivity extends ActionBarActivity {

    Button mButton;
    EditText mEdit;
    EditText sEdit;
    TextView cEdit;
    int totalTime = 0;
    private static final String FORMAT = "%02d:%02d:%02d";
    SmsManager smsManager = SmsManager.getDefault();
    EditText txtphoneNo;
    EditText txtMessage;
    boolean countdownHasStarted = false;
    double longitude;
    double latitude;
    //shaker
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    //no response
    CountDownTimer counter;
    final int DISMISS = 0;
    AlertDialog dialog;
    final int TIMEOUT = 5000;
    String phoneNoInput;



    /*** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button);
        mEdit = (EditText) findViewById(R.id.minuteView);
        sEdit = (EditText) findViewById(R.id.secondView);
        txtphoneNo = (EditText) findViewById(R.id.phoneNumberInput);
        txtMessage = (EditText) findViewById(R.id.messageInput);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();

//        mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {
//
//            public void onShake() {
//                if (countdownHasStarted == true) {
//                    Toast.makeText(MainActivity.this, "Shake!", Toast.LENGTH_SHORT).show();
//                    call();
//                }
//            }
//        });


        final LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

        };


        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

        System.out.println("Latitude: " + latitude + "longitude: " + longitude);

        mButton.setOnClickListener(

                new View.OnClickListener() {
                    public void onClick(View view) {
                        Log.v("TIME??", mEdit.getText().toString());
                        if (!TextUtils.isEmpty(mEdit.getText())) {
                            totalTime += Integer.parseInt(mEdit.getText().toString()) * 60000;
                        } else {
                            totalTime += 0;
                        }
//convert the second field into milliseconds
                        if (!TextUtils.isEmpty(sEdit.getText())) {
                            totalTime += Integer.parseInt(sEdit.getText().toString()) * 1000;
                        } else {
                            totalTime += 0;
                        }

                        System.out.println("totalTime: " + totalTime);

                        cEdit = (TextView) findViewById(R.id.countdownView);

                        //COINTDOWN TIMER
                        if (countdownHasStarted == false) {
                            countdownHasStarted = true;

                            phoneNoInput = txtphoneNo.getText().toString();

                            new CountDownTimer(totalTime, 1000) {
                                public void onTick(long millisUntilFinished) {

                                    //Convert to minutes and seconds
                                    cEdit.setText("" + String.format(FORMAT,
                                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                                }


                                //GET LOCATION OF PHONE
                                Location lastLocation;

                                //get #


                                public void onFinish() {
                                    cEdit.setText("done!");
                                    totalTime = 0;
                                    sendSMSMessage();
                                    countdownHasStarted = false;
                                    generateDialog();
                                }
                            }
                                    .start();
                        }


                    }

                    private Handler mHandler = new Handler() {
                        public void handleMessage(android.os.Message msg) {
                            System.out.println("Handle Message");
                            switch (msg.what) {
                                case DISMISS:
                                    if (dialog != null && dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                    break;

                                default:
                                    break;
                            }
                        }
                    };


                    private void generateDialog() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        System.out.println("Created Builder");
                        builder.setMessage("The time allotted has run out and you haven't confirmed if you've reached your destination. Are you home yet?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                System.out.println(" onclick Builder");
                                finish();
                                //I want to return to a default state. Yes I made it home.
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                        dialog = builder.create();
                        dialog.show();
                        mHandler.sendEmptyMessageDelayed(DISMISS, TIMEOUT);
                        System.out.println("Send message");
                    }


                    //SEND SMS MESSAGE
                    protected void sendSMSMessage() {

                        Log.i("Send SMS", "");
                        // String phoneNoInput = txtphoneNo.getText().toString();
                        String mapLink = "https://www.google.com/maps/?q=" + latitude + "," + longitude;
                        String messageInput = txtMessage.getText().toString() + "\n Here's my last location: " + mapLink;


                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(phoneNoInput, null, messageInput, null, null);
                            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                });




        mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {
            private void call() {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + phoneNoInput));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException e) {
                    Log.e("helloandroid", "Call failed", e);
                }
            }
            public void onShake() {

                if (countdownHasStarted == true) {

                    Toast.makeText(MainActivity.this, "Shake!", Toast.LENGTH_SHORT).show();
                    call();
                }
            }
        });
    }




    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // First super, then do stuff.
        // Let us display the previous posts, if any.
        //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);

    }


    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
