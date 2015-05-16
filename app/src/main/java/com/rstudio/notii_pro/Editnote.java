package com.rstudio.notii_pro;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialogCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Editnote extends ActionBarActivity {

    private Intent intent;
    private Note_item note_item;

    private ScrollView background;
    private EditText title, text;
    private TextView date;
    private ImageView flag;
    private TextView setRemind;
    private int color;
    private int quick_color;
    private boolean bold;
    private long remind;
    private SharedPreferences sharedPref;

    Calendar now;
    SimpleDateFormat getTime;
    SimpleDateFormat getDate;
    int hour_picked, min_picked, day_picked, month_picked, year_picked;
    boolean isTimePicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editnote);

        // Setup ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Get time now from System
        now = Calendar.getInstance();
        // Setup time format
        getTime = new SimpleDateFormat("HH:mm");
        getDate = new SimpleDateFormat("dd/MM/yyyy");
        isTimePicked = true; // use for TimePicker

        // Setup handle
        background = (ScrollView) findViewById(R.id.editNote_background);
        title = (EditText) findViewById(R.id.editNote_title);
        text = (EditText) findViewById(R.id.editNote_text);
        date = (TextView) findViewById(R.id.editNote_time);
        flag = (ImageView) findViewById(R.id.editNote_flag);
        setRemind = (TextView) findViewById(R.id.editNote_remind);

        // Setup quick note color
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int number_color = Integer.parseInt(sharedPref.getString("quick_color", "1"));
        switch (number_color) {
            case 1:
                quick_color = getResources().getColor(R.color.redorange);
                break;
            case 2:
                quick_color = getResources().getColor(R.color.red);
                break;
            case 3:
                quick_color = getResources().getColor(R.color.cyan);
                break;
            case 4:
                quick_color = getResources().getColor(R.color.green);
                break;
            case 5:
                quick_color = getResources().getColor(R.color.yellow);
                break;
            case 6:
                quick_color = getResources().getColor(R.color.orange);
                break;
            case 7:
                quick_color = getResources().getColor(R.color.pink);
                break;
            default:
                break;
        }

        // Setup Intent
        intent = getIntent();
        note_item = getNote(intent.getBundleExtra("Note"));

        // Setup initial data
        background.setBackgroundColor(note_item.getColor());
        title.setText(note_item.getTitle());
        text.setText(note_item.getText());
        date.setText(note_item.getDate());
        if (note_item.getBold()) {
            flag.setVisibility(View.VISIBLE);
        }
        else {
            flag.setVisibility(View.INVISIBLE);
        }
        color = note_item.getColor();
        bold = note_item.getBold();

        // this handle notification status of note
        remind = note_item.getRemind();
        if (remind > now.getTimeInMillis()) {
            now.setTimeInMillis(remind);
            hour_picked = now.get(Calendar.HOUR_OF_DAY);
            min_picked = now.get(Calendar.MINUTE);
            day_picked = now.get(Calendar.DAY_OF_MONTH);
            month_picked = now.get(Calendar.MONTH);
            setRemind.setText("Will remind at " + hour_picked + ":" + min_picked
                    + " " + day_picked + "/" + month_picked);
        }
        else if (remind > 0){
            now.setTimeInMillis(remind);
            hour_picked = now.get(Calendar.HOUR_OF_DAY);
            min_picked = now.get(Calendar.MINUTE);
            day_picked = now.get(Calendar.DAY_OF_MONTH);
            month_picked = now.get(Calendar.MONTH);
            setRemind.setText("Reminded at " + hour_picked + ":" + min_picked
                    + " " + day_picked + "/" + month_picked);
        }
        else {
            setRemind.setText("");
        }
    }

    // Setup activity when terminal activity
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Override this method for handle put note item back to Main Activity
    @Override
    public void onBackPressed () {
        Note_item send_note = getNote_info(note_item);
        // detect the note changed and change the time
        if (!note_item.getText().equals(send_note.getText()) || !note_item.getTitle().equals(send_note.getTitle())
                || color != send_note.getColor() || remind != send_note.getRemind() || bold != send_note.getBold()) {
            now = Calendar.getInstance();
            send_note.setDate("Edited " + getTime.format(now.getTime()) + " " + getDate.format(now.getTime()));
        }
        intent.putExtra("NoteBack", setBundle(send_note));
        setResult(MainActivity.NOTE_BACK, intent);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editnote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.editNote_flag) {
            if (note_item.getBold()) {
                note_item.setBold(false);
                flag.setVisibility(View.INVISIBLE);
            }
            else {
                note_item.setBold(true);
                flag.setVisibility(View.VISIBLE);
            }
            return true;
        }
        else if (id == R.id.editNote_remind) {
            {
                if (note_item.getRemind() > 0) {
                    // this function include cancel pendingIntent
                    deleteNoteRemindPopup().show();
                }
                else {
                    // Show time picker and date picker
                    final Calendar calendar = Calendar.getInstance();
                    final int hour_now = calendar.get(Calendar.HOUR_OF_DAY);
                    final int min_now = calendar.get(Calendar.MINUTE);
                    boolean is24h = true;
                    final TimePickerDialog timePicker = new TimePickerDialog(Editnote.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            hour_picked = hourOfDay;
                            min_picked = minute;
                            // handle when date and time picked success
                            if (isTimePicked) {
                                calendar.set(year_picked, month_picked, day_picked, hour_picked, min_picked);
                                now = Calendar.getInstance();
                                if (now.getTimeInMillis() > calendar.getTimeInMillis()) {
                                    Toast.makeText(Editnote.this, "Wrong time picked, sir :(", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (now.getTimeInMillis() < getNote_info(note_item).getRemind()) {
                                        cancelNotification(getNote_info(note_item));
                                    }
                                    note_item.setRemind(calendar.getTimeInMillis());
                                    Toast.makeText(Editnote.this, getString(R.string.remind_set), Toast.LENGTH_SHORT).show();
                                    setRemind.setText("Will remind at " + hour_picked + ":" + min_picked
                                            + " " + day_picked + "/" + month_picked);
                                    sendNotification(getNote_info(note_item));
                                }
                            } else isTimePicked = true;
                        }
                    }, hour_now, min_now, is24h);
                    // setup cancel button action
                    timePicker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                timePicker.dismiss();
                                isTimePicked = false;
                            }
                        }
                    });
                    timePicker.show();
                    int year_now = calendar.get(Calendar.YEAR);
                    final int monthOfYear_now = calendar.get(Calendar.MONTH);
                    int dayOfMonth_now = calendar.get(Calendar.DAY_OF_MONTH);
                    final DatePickerDialog datePicker = new DatePickerDialog(Editnote.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            year_picked = year;
                            month_picked = monthOfYear;
                            day_picked = dayOfMonth;
                        }
                    }, year_now, monthOfYear_now, dayOfMonth_now);
                    // setup cancel button action
                    datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                datePicker.dismiss();
                                isTimePicked = false;
                            }
                        }
                    });
                    datePicker.show();
                }
            }
            return true;
        }
        else if (id == R.id.editNote_delete) {
            deleteNotePopup().show();
            return true;
        }
        else if (id == R.id.editNote_color) {
            colorsPopup().show();
            return true;
        }
        else if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // pop up when color action button pressed
    public Dialog colorsPopup() {
        // Using module MaterialDialogCompat.Builder for Material Design tatse
        MaterialDialogCompat.Builder dialog = new MaterialDialogCompat.Builder(Editnote.this);
            dialog.setItems(R.array.colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            background.setBackgroundColor(getResources().getColor(R.color.redorange));
                            note_item.setColor(getResources().getColor(R.color.redorange));
                            break;
                        case 1:
                            background.setBackgroundColor(getResources().getColor(R.color.red));
                            note_item.setColor(getResources().getColor(R.color.red));
                            break;
                        case 2:
                            background.setBackgroundColor(getResources().getColor(R.color.cyan));
                            note_item.setColor(getResources().getColor(R.color.cyan));
                            break;
                        case 3:
                            background.setBackgroundColor(getResources().getColor(R.color.green));
                            note_item.setColor(getResources().getColor(R.color.green));
                            break;
                        case 4:
                            background.setBackgroundColor(getResources().getColor(R.color.yellow));
                            note_item.setColor(getResources().getColor(R.color.yellow));
                            break;
                        case 5:
                            background.setBackgroundColor(getResources().getColor(R.color.orange));
                            note_item.setColor(getResources().getColor(R.color.orange));
                            break;
                        case 6:
                            background.setBackgroundColor(getResources().getColor(R.color.pink));
                            note_item.setColor(getResources().getColor(R.color.pink));
                            break;
                        default:
                            break;
                    }
                }
            });
        return dialog.create();
    }

    // pop up when delete action button pressed
    public Dialog deleteNotePopup() {
        MaterialDialogCompat.Builder builder = new MaterialDialogCompat.Builder(Editnote.this);
        builder.setMessage(R.string.delete_note)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        text.setText("");
                        onBackPressed();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    // pop up when delete action button pressed
    public Dialog deleteNoteRemindPopup() {
        MaterialDialogCompat.Builder builder = new MaterialDialogCompat.Builder(Editnote.this);
        builder.setMessage(R.string.delete_note_remind)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        cancelNotification(getNote_info(note_item));
                        note_item.setRemind(0);
                        setRemind.setVisibility(View.GONE);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    // setup note from intent bundle
    private Note_item getNote(Bundle bundle) {
        Note_item note = new Note_item();
        if (bundle == null) return note;
        note.setTitle(bundle.getString("Title", ""));
        note.setText(bundle.getString("Text", ""));
        note.setDate(bundle.getString("Date", ""));
        note.setColor(bundle.getInt("Color", quick_color));
        note.setId(bundle.getInt("ID", 0));
        note.setBold(bundle.getBoolean("Bold", false));
        note.setRemind(bundle.getLong("Remind", 0));
        return note;
    }

    // setup bundle to pass from intent
    private Bundle setBundle(Note_item note) {
        Bundle bundle = new Bundle();
        if (note == null) return bundle;
        bundle.putString("Title", note.getTitle());
        bundle.putString("Text", note.getText());
        bundle.putString("Date", note.getDate());
        bundle.putInt("Color", note.getColor());
        bundle.putInt("ID", note.getId());
        bundle.putBoolean("Bold", note.getBold());
        bundle.putLong("Remind", note.getRemind());
        return bundle;
    }

    // setup the newest note item base on current information
    private Note_item getNote_info(Note_item note_in) {
        Note_item note = new Note_item();
        note.setTitle(title.getText().toString());
        note.setText(text.getText().toString());
        note.setDate(date.getText().toString());
        note.setColor(note_in.getColor());
        note.setId(note_in.getId());
        note.setBold(note_in.getBold());
        note.setRemind(note_in.getRemind());
        return note;
    }

    // use for send Notification
    private void sendNotification (Note_item note) {
        Intent passNotification = new Intent(Editnote.this, SendNotification.class);
        passNotification.putExtra("Text", note.getTitle());
        passNotification.putExtra("ID", note.getId());
        AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        PendingIntent pending = PendingIntent.getService(Editnote.this,
                note.getId(), passNotification, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, note.getRemind(), pending);
    }

    // use for cancel Notification
    private void cancelNotification (Note_item note) {
        if (note.getRemind() < Calendar.getInstance().getTimeInMillis()) return;
        Intent passNotification = new Intent(Editnote.this, SendNotification.class);
        PendingIntent pending = PendingIntent.getService(Editnote.this, note.getId(), passNotification,
                PendingIntent.FLAG_UPDATE_CURRENT);
        pending.cancel();
        AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pending);
    }
}
