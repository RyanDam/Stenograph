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
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rstudio.notii_pro.adapters.CheckItemAdapter;
import com.rstudio.notii_pro.database.DatabaseMng;
import com.rstudio.notii_pro.item.CheckItem;
import com.rstudio.notii_pro.item.NoteItem;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Editnote extends ActionBarActivity {

    private Intent intent;
    private NoteItem note_item;
    private Typeface font_bold, font_regular;

    private FrameLayout background;
    private EditText title, text;
    private TextView date;
    private ImageView flag;
    private TextView setRemind;
    private int color;
    private int quick_color;
    private boolean bold;
    private long remind;
    private SharedPreferences sharedPref;
    private boolean makeNewNote = false;
    private boolean deleteNote = false;
    private boolean checkListMode = false;
    private int wishId = 0;
    private boolean isEdited = false;

    private Calendar now;
    private SimpleDateFormat getTime;
    private SimpleDateFormat getDate;
    private int hour_picked, min_picked, day_picked, month_picked, year_picked;
    private boolean isTimePicked;
    private CheckItemAdapter checkListAdap;
    private ListView checkList;
    private ArrayList<CheckItem> mCheckArrayList;

    private DatabaseMng database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editnote);

        // Setup ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setElevation(0);

        // Get time now from System
        now = Calendar.getInstance();
        // Setup time format
        getTime = new SimpleDateFormat("HH:mm");
        getDate = new SimpleDateFormat("dd/MM/yyyy");
        isTimePicked = true; // use for TimePicker

        setupConnector();

        database = new DatabaseMng(this);
        mCheckArrayList = new ArrayList<>();

        // Setup Intent
        intent = getIntent();
        note_item = database.getNote(intent.getIntExtra("Note", -1));
        checkListMode = intent.getBooleanExtra("CheckMode", false);
        wishId = intent.getIntExtra("WishId", -1);

        if (note_item == null) {
            note_item = getNote(null);
            note_item.setColor(quick_color);
            makeNewNote = true;
            checkListAdap = new CheckItemAdapter(this, mCheckArrayList, wishId, this, checkList);
        }
        else {
            ArrayList<CheckItem> cache = database.getAllCheckItemWithNoteID(note_item.getId());
            if (cache != null)
                mCheckArrayList.addAll(cache);
            checkListAdap = new CheckItemAdapter(this, mCheckArrayList, note_item.getId(), this, checkList);
        }

        checkList.setAdapter(checkListAdap);
        checkListAdap.notifyDataSetChanged();

        checkList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteCheckItem(position).show();
                return true;
            }
        });

        if (checkListMode) {
            text.setVisibility(View.GONE);
        }
        else {
            checkList.setVisibility(View.GONE);
        }

        initialData();
    }

    private void setupConnector() {

        // Setup handle
        background = (FrameLayout) findViewById(R.id.editNote_background);
        title = (EditText) findViewById(R.id.editNote_title);
        text = (EditText) findViewById(R.id.editNote_text);
        date = (TextView) findViewById(R.id.editNote_time);
        flag = (ImageView) findViewById(R.id.editNote_flag);
        setRemind = (TextView) findViewById(R.id.editNote_remind);
        checkList = (ListView) findViewById(R.id.check_list_edit);

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
    }

    private void initialData() {

        // setup typeface
        font_bold = Typeface.createFromAsset(getAssets(), "fonts/slab_bold.ttf");
        font_regular = Typeface.createFromAsset(getAssets(), "fonts/slab_regular.ttf");

        // Setup initial data
        background.setBackgroundColor(note_item.getColor());
        title.setText(note_item.getTitle());
        text.setText(note_item.getText());
        date.setText(note_item.getDate());

        // this prevent keyboard popup
        background.requestFocus();

        title.setTypeface(font_bold);
        text.setTypeface(font_regular);

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
            setRemind.setText(getResources().getString(R.string.remind_text_note) + " " + hour_picked + ":" + min_picked
                    + " " + day_picked + "/" + (month_picked + 1));
        }
        else if (remind > 0){
            now.setTimeInMillis(remind);
            hour_picked = now.get(Calendar.HOUR_OF_DAY);
            min_picked = now.get(Calendar.MINUTE);
            day_picked = now.get(Calendar.DAY_OF_MONTH);
            month_picked = now.get(Calendar.MONTH);
            setRemind.setText(getResources().getString(R.string.reminded_text_note) + " " + hour_picked + ":" + min_picked
                    + " " + day_picked + "/" + (month_picked + 1));
        }
        else {
            setRemind.setText("");
        }

        if (setRemind.getText().toString().compareTo("") == 0) {
            setRemind.setVisibility(View.GONE);
        }
        if (date.getText().toString().compareTo("") == 0) {
            date.setVisibility(View.GONE);
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
        NoteItem send_note = getNote_info(note_item);
        now = Calendar.getInstance();
        if (!note_item.getText().equals(send_note.getText()) || !note_item.getTitle().equals(send_note.getTitle())
                || color != send_note.getColor() || remind != send_note.getRemind() || bold != send_note.getBold()
                || isEdited) {
            if (!makeNewNote) {
                send_note.setDate(getResources().getString(R.string.edit_text_note) + " " + getTime.format(now.getTime()) + " " + getDate.format(now.getTime()));
            }
            else {
                send_note.setDate(getResources().getString(R.string.create_text_note) + " " + getTime.format(now.getTime()) + " " + getDate.format(now.getTime()));
            }
        }
        if (!checkListMode) {

            // detect the note changed and change the time

            if (makeNewNote) {
                if (!deleteNote) {
                    if (send_note.getText().compareTo("") != 0)
                        database.addNote(send_note);
                }
                else {
                    database.removeNote(send_note.getId());
                }
            } else {
                if (!deleteNote)
                    database.updateNote(send_note);
                else
                    database.removeNote(send_note.getId());
            }

        }

        else {
            if (mCheckArrayList != null) {
                if (mCheckArrayList.size() > 0) {
                    if (mCheckArrayList.get(0).getText().compareTo("") != 0) {
                        if (deleteNote) {
                            database.removeCheckItemWithNoteId(note_item.getId());
                            database.removeNote(note_item.getId());
                        } else {
                            if (makeNewNote) {
                                database.addNote(send_note);
                            } else {
                                database.updateNote(send_note);
                            }
                            database.removeCheckItemWithNoteId(note_item.getId());
                            int size = mCheckArrayList.size();
                            for (int i = 0; i < size; i++) {
                                database.addCheckItem(mCheckArrayList.get(i));
                            }
                        }
                    }
                }
            }
        }
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
                                    Toast.makeText(Editnote.this, getResources().getString(R.string.wrong_time_pick), Toast.LENGTH_SHORT).show();
                                } else {
                                    if (now.getTimeInMillis() < getNote_info(note_item).getRemind()) {
                                        cancelNotification(getNote_info(note_item));
                                    }
                                    note_item.setRemind(calendar.getTimeInMillis());
                                    Toast.makeText(Editnote.this, getString(R.string.remind_set), Toast.LENGTH_SHORT).show();
                                    setRemind.setVisibility(View.VISIBLE);
                                    setRemind.setText(getResources().getString(R.string.will_remind_text) + " " + hour_picked + ":" + min_picked
                                            + " " + day_picked + "/" + (month_picked + 1));
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
        else if (id == R.id.editNote_share) {
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_TITLE, title.getText().toString());
            share.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
            share.setType("text/plain");
            startActivity(share);
        }
        return super.onOptionsItemSelected(item);
    }

    // pop up when color action button pressed
    public Dialog colorsPopup() {
        // Using module MaterialDialogCompat.Builder for Material Design tatse
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(Editnote.this);

        dialog.items(R.array.colors)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        switch (i) {
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
        return dialog.build();
    }

    // pop up when delete action button pressed
    public Dialog deleteNotePopup() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(Editnote.this);
        builder.title(R.string.delete_note)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        deleteNote = true;
                        onBackPressed();
                    }
                });
        return builder.build();
    }

    // pop up when delete check item
    public MaterialDialog deleteCheckItem(final int pos) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .items(R.array.check_list_long_click_item)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        switch (i) {
                            case 0:
                                mCheckArrayList.remove(pos);
                                checkListAdap.notifyDataSetChanged();
                                break;
                        }
                    }
                }).build();
        return dialog;
    }

    // pop up when delete action button pressed
    public Dialog deleteNoteRemindPopup() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(Editnote.this);
        builder.title(R.string.delete_note_remind)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        cancelNotification(getNote_info(note_item));
                        note_item.setRemind(0);
                        setRemind.setVisibility(View.GONE);
                    }
                });
        return builder.build();
    }

    // setup note from intent bundle
    private NoteItem getNote(Bundle bundle) {
        NoteItem note = new NoteItem();
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
    private Bundle setBundle(NoteItem note) {
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
    private NoteItem getNote_info(NoteItem note_in) {
        NoteItem note = new NoteItem();
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
    private void sendNotification (NoteItem note) {
        Intent passNotification = new Intent(Editnote.this, SendNotification.class);
        passNotification.putExtra("Text", note.getTitle());
        passNotification.putExtra("ID", note.getId());
        AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        PendingIntent pending = PendingIntent.getService(Editnote.this,
                note.getId(), passNotification, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, note.getRemind(), pending);
    }

    // use for cancel Notification
    private void cancelNotification (NoteItem note) {
        if (note.getRemind() < Calendar.getInstance().getTimeInMillis()) return;
        Intent passNotification = new Intent(Editnote.this, SendNotification.class);
        PendingIntent pending = PendingIntent.getService(Editnote.this, note.getId(), passNotification,
                PendingIntent.FLAG_UPDATE_CURRENT);
        pending.cancel();
        AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pending);
    }

    public void setEdited(boolean edit) {
        isEdited = edit;
    }
}
