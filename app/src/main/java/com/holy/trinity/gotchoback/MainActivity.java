package com.holy.trinity.gotchoback;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
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
import android.os.Message;
//

public class MainActivity extends ActionBarActivity {

    Button mButton;
    EditText mEdit;
    TextView pEdit;
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
    final int DANGER = 1;
    AlertDialog dialog, dialog2;
    final int TIMEOUT = 5000;
    String phoneNoInput;
    boolean isPaused, isCanceled;
    MediaPlayer sound;
    Button sButton;
    int remainingTime;

    /*** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sButton = (Button) findViewById(R.id.safe);
        mButton = (Button) findViewById(R.id.button);
        mEdit = (EditText) findViewById(R.id.minuteView);
        sEdit = (EditText) findViewById(R.id.secondView);
        txtphoneNo = (EditText) findViewById(R.id.phoneNumberInput);
        txtMessage = (EditText) findViewById(R.id.messageInput);
        sound = MediaPlayer.create(this, R.raw.alarm);

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
                        if (!isPaused) {
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
                        }

                        System.out.println("totalTime: " + totalTime);

                        cEdit = (TextView) findViewById(R.id.countdownView);

                        //COINTDOWN TIMER
                        if (!countdownHasStarted || isPaused || isCanceled) {
                            countdownHasStarted = true;
                            //isPaused = false;
                            isCanceled = false;

                            phoneNoInput = txtphoneNo.getText().toString();

                            //COINTDOWN TIMER
                            counter = new CountDownTimer(totalTime, 1000) {
                                public void onTick(long millisUntilFinished) {
                                    if (isPaused || isCanceled) {
                                        totalTime = (int) millisUntilFinished;
                                        countdownHasStarted = false;
                                        //pEdit = (TextView) findViewById(R.id.countdownView);
                                        cancel();
                                    }
//                                    countdownHasStarted = true;
//                                    //Convert to minutes and seconds
                               cEdit.setText("" + String.format(FORMAT,
                                      TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                                         TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                         TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                                  TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                                    //totalTime = (int) millisUntilFinished;
                                    //System.out.println(totalTime);

                             }


                                //GET LOCATION OF PHONE
                                Location lastLocation;

                                public void onFinish() {

                                    cEdit.setText("done!");
                                    totalTime = 0;
                                    countdownHasStarted = false;
                                    System.out.println("Generating first dialog");
                                    generateDialog();
                                }

                            }.start();

                        }


                    }

                    private Handler mHandler = new Handler() {
                        public void handleMessage(Message msg) {
                            System.out.println("Handle Message: CASE: " + msg.what);
                            switch (msg.what) {
                                case DISMISS:
                                    if (dialog != null && dialog.isShowing()) {
                                        System.out.println("I hit the dismiss case");
                                        dialog.dismiss();
                                        //Play Music
                                        sound.start();
                                        generateSecondDialog();
                                        //dialog.dismiss();
                                    }
                                    break;
                                case DANGER:
                                    if (dialog2 != null && dialog2.isShowing()) {
                                        System.out.println("I hit the danger case");
                                        //Send out text to loved one.
                                        sendSMSMessage();
                                        dialog2.dismiss();
                                    }
                                default:
                                    break;
                            }
                        }
                    };

                    private void generateDialog() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("The time allotted has run out and you haven't confirmed if you've reached your destination. Are you home yet?")
                                .setCancelable(false).setPositiveButton("I'm home", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                System.out.println(" onclick Builder");
                                //Finish closes the app
                                finish();
                            }
                        }).setNegativeButton("I need more time", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                        dialog = builder.create();
                        dialog.show();
                        mHandler.sendEmptyMessageDelayed(DISMISS, TIMEOUT);
                    }

                    private void generateSecondDialog() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Please respond in the next 5 seconds or your text message and last location will be sent to your emergency contact.")
                                .setCancelable(false).setPositiveButton("I'm home", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                //I want to return to a default state. Yes I made it home.
                            }
                        }).setNegativeButton("I need more time", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog2, int id) {

                            }
                        });
                        dialog2 = builder.create();
                        dialog2.show();
                        mHandler.sendEmptyMessageDelayed(DANGER, TIMEOUT);
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

        sButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Toast.makeText(getApplicationContext(), "I'm glad you're safe! :)", Toast.LENGTH_LONG).show();
                        System.exit(0);

                        //totalTime = 0;

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

                                if (countdownHasStarted) {
                                    countdownHasStarted = false;
                                    isPaused = true;
                                    counter.cancel();

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