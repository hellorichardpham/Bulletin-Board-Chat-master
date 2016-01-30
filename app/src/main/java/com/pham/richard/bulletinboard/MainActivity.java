package com.pham.richard.bulletinboard;

import android.widget.Button;
import android.widget.EditText;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;


import com.pham.richard.bulletinboard.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    Button mButton;
    EditText mEdit;
    EditText sEdit;
    int totalTime = 0;

    /*** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button);
        mEdit = (EditText) findViewById(R.id.minuteView);
        sEdit = (EditText) findViewById(R.id.secondView);

        mButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Log.v("TIME??", mEdit.getText().toString());
                        //convert the minute field into milliseconds
                        totalTime += Integer.parseInt(mEdit.getText().toString()) * 60000;
                        //convert the second field into milliseconds
                        totalTime += Integer.parseInt(sEdit.getText().toString()) * 1000;
                        System.out.println("totalTime: " + totalTime);

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

    }


    @Override
    protected void onPause() {

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
