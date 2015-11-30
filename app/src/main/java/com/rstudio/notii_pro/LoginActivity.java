package com.rstudio.notii_pro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rstudio.notii_pro.R;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private EditText input_password;
    private Button button_signin;
    private String password;
    private Intent intent;
    private boolean status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        getSupportActionBar().hide();

        // Connect object to xml
        input_password = (EditText) findViewById(R.id.input_password);
        button_signin = (Button) findViewById(R.id.button_password);

        // get parent intent
        intent = getIntent();
        status = false;

        // get password and handle it
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        password = sharedPref.getString("password", "");
        button_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input_pass = input_password.getText().toString();
                if (input_pass.equals(password)) {
                    status = true;
                    intent.putExtra("login_status", true);
                    setResult(MainActivity.LOGIN_STATUS_BACK, intent);
                    LoginActivity.this.finish();
                } else if (input_pass.equals("")) {
                    Toast.makeText(LoginActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                    input_password.setText("");
                }
            }
        });


    }

    @Override
    public void onBackPressed () {
        // when back button pressed, this will tell the main activity
        // the login success or not
        intent.putExtra("login_status", status);
        setResult(MainActivity.LOGIN_STATUS_BACK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
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
}
