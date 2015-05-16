package com.rstudio.notii_pro;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rstudio.notii_pro.R;

public class AboutActivity extends ActionBarActivity {

    private TextView about_text;
    private Button rate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        // setup ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // connect object with xml
        about_text = (TextView) findViewById(R.id.about_text);
        rate = (Button) findViewById(R.id.rate_button);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/archer.ttf");
        String text;
        text = "Stenograph 2.0\n\n"
            + "dot R Studio\n"
            + "Author: Dam Vu Duy\n\n"
            + "This is my first app on android\n"
            + "I just made this app for fun and\n"
            + "test my coding skill.\n\n"
            + "Thank maurycyw for StaggeredGridView\n"
            + "Thank afollestad for Material dialogs\n\n"
            + "Hope you like this app, cheer.";
        about_text.setText(text);
        about_text.setTypeface(font);

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rate = new Intent(Intent.ACTION_VIEW).setData(Uri.parse
                        (getResources().getString(R.string.market_rate)));
                startActivity(rate);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
