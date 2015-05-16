package com.rstudio.notii_pro;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialogCompat;
import com.origamilabs.library.views.StaggeredGridView;
import com.origamilabs.library.views.StaggeredGridView.OnItemClickListener;
import com.origamilabs.library.views.StaggeredGridView.OnItemLongClickListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends ActionBarActivity {

    public static final int CREATE_NEW_NOTE = 1;
    public static final int EDIT_NOTE = 2;
    public static final int NOTE_BACK = 3;
    public static final int LOGIN = 4;
    public static final int LOGIN_STATUS_BACK = 5;
    public static final int SYNC_TASK = 6;
    public static final int RESTORE_DONE = 6;

    Note_item note;
    int countQuick = 0, posNote = -1, count = 0;
    public StaggeredGridView list;
    public static ArrayList<Note_item> arrayList;
    public Note_adapter adapterList;
    public Intent mMainToEdit, mMainToSetting, mMainToStartAlarm;
    private SharedPreferences sharedPref;
    private int location_editnote;
    private boolean saveDataMode = true;
    private boolean loginMode;

    Calendar now;
    SimpleDateFormat getTime;
    SimpleDateFormat getDate;

    public static DatabaseMng database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.logo_actionbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // turn login screen on
        loginMode = true;

        // initial data
        readingSetup(this);

        // turn login screen off to not show login screen again
        loginMode = false;

        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(StaggeredGridView parent, View view, int position, long id) {
                mMainToEdit.putExtra("Note", setBundle(arrayList.get(position)));
                location_editnote = position;
                startActivityForResult(mMainToEdit, EDIT_NOTE);
            }
        });

        list.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(StaggeredGridView parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putInt("Position", position);
                if (longClickNotes(bundle) != null) {
                    longClickNotes(bundle).show();
                }
                return false;
            }
        });

    }

    @Override
    public void onDestroy() {
        // when this app exit, save data function will be called
        if (saveDataMode)
            saveDatabase();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        adapterList.notifyDataSetChanged();
        // setup list column number
        // must reset state after back activity
        String list_column_setting = sharedPref.getString("list_column", "2");
        list.setColumnCount(Integer.parseInt(list_column_setting));
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        else if (id == R.id.action_list) {
            if (list.getColumnCount() == 1) {
                list.setColumnCount(2);
                sharedPref.edit().putString("list_column", "2").apply();
                adapterList.notifyDataSetChanged();
            }
            else if (list.getColumnCount() == 2) {
                list.setColumnCount(3);
                sharedPref.edit().putString("list_column", "3").apply();
                adapterList.notifyDataSetChanged();
            }
            else if (list.getColumnCount() == 3) {
                list.setColumnCount(4);
                sharedPref.edit().putString("list_column", "4").apply();
                adapterList.notifyDataSetChanged();
            }
            else if (list.getColumnCount() == 4) {
                list.setColumnCount(1);
                sharedPref.edit().putString("list_column", "1").apply();
                adapterList.notifyDataSetChanged();
            }
            return true;
        }
        else if (id == R.id.action_plus) {
            mMainToEdit.putExtra("Note", setBundle(null));
            startActivityForResult(mMainToEdit, CREATE_NEW_NOTE);
            return true;
        }
        else if (id == R.id.action_sync) {
            startActivityForResult(new Intent(MainActivity.this, SyncActivity.class), SYNC_TASK);
            return true;
        }
        else if (id == R.id.action_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_NEW_NOTE && resultCode == NOTE_BACK) {
            note = getNote(data.getBundleExtra("NoteBack"));
            if (note != null) {
                if (note.getText().equals("")) return;
                arrayList.add(0, note);
                adapterList.notifyDataSetChanged();
            }
        }
        else if (requestCode == EDIT_NOTE && resultCode == NOTE_BACK) {
            note = getNote(data.getBundleExtra("NoteBack"));
            if (note != null) {
                if (note.getText().equals("")) {
                    arrayList.remove(location_editnote);
                    adapterList.notifyDataSetChanged();
                }
                else {
                    arrayList.set(location_editnote, note);
                    adapterList.notifyDataSetChanged();
                }
            }
        }
        else if (requestCode == LOGIN && resultCode == LOGIN_STATUS_BACK) {
            boolean status = data.getBooleanExtra("login_status", false);
            if (!status) {
                finish();
            }
        }
        else if (requestCode == SYNC_TASK && resultCode == RESTORE_DONE) {
            saveDataMode = false;
            finish();
        }
    }

    // Initial settup for main activity
    private void readingSetup(Context context) {

        // Connect Object to xml /////////////////////////////////////////
        list = (StaggeredGridView) findViewById(R.id.list_note_staggered);
        arrayList = new ArrayList<Note_item>();
        /////////////////////////////////////////////////////////////////

        // Setup for ListView
        adapterList = new Note_adapter (MainActivity.this, arrayList);
        list.setAdapter(adapterList);

        // Setup Database
        database = new DatabaseMng(context);

        // Setup start reading data
        loadDatabase();

        // Setup Intent
        mMainToEdit = new Intent(MainActivity.this, Editnote.class);

        // Get time from System
        now = Calendar.getInstance();
        // // Setup time format
        getTime = new SimpleDateFormat("HH:mm");
        getDate = new SimpleDateFormat("dd/MM/yyyy");

        // whether login or not
        if (loginMode) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            boolean isRequitedPassword = sharedPref.getBoolean("password_enable", false);
            if (isRequitedPassword) {
                Intent startLogin = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(startLogin, LOGIN);
            }
        }

        // Make an Intruction if list note is empty
        if (database.getNoteCount() <= 0){
            note = new Note_item(99, "Intruction tip","Welcome to Stenograph\nStenograph is the most simple app you have ever seen\nPress the Plus button and write down on the top box and make a quick note\nHope you have fun with this app ;)", "Deer, the actor of this app", getResources().getColor(R.color.blue), true);
            arrayList.add(0, note);
            adapterList.notifyDataSetChanged();
        }

        // setup list column number
        String list_column_setting = sharedPref.getString("list_column", "2");
        list.setColumnCount(Integer.parseInt(list_column_setting));

//        turn off over scrolling effect
//        list.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    public Dialog longClickNotes(Bundle bundle) {
        final int position = bundle.getInt("Position", -1);
        if (position == -1) return null;
        MaterialDialogCompat.Builder dialog = new MaterialDialogCompat.Builder(MainActivity.this);
        if (arrayList.get(position).getBold()) {
            dialog.setItems(R.array.long_click_note_mark, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        arrayList.get(position).setBold(false);
                    }
                    else if (which == 1) {
                        arrayList.remove(position);
                    }
                    adapterList.notifyDataSetChanged();
                }
            });
        }
        else {
            dialog.setItems(R.array.long_click_note_no_mark, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        arrayList.get(position).setBold(true);
                    }
                    else if (which == 1) {
                        arrayList.remove(position);
                    }
                    adapterList.notifyDataSetChanged();
                }
            });
        }
        return dialog.create();
    }

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

    private Note_item getNote(Bundle bundle) {
        Note_item note = new Note_item();
        if (bundle == null) return note;
        note.setTitle(bundle.getString("Title", "Quick note"));
        note.setText(bundle.getString("Text", ""));
        note.setDate(bundle.getString("Date"));
        note.setColor(bundle.getInt("Color", getResources().getColor(R.color.redorange)));
        note.setId(bundle.getInt("ID"));
        note.setBold(bundle.getBoolean("Bold", false));
        note.setRemind(bundle.getLong("Remind", 0));
        return note;
    }

    public void saveDatabase(){
        posNote = 0;
        database.delAllNote();
        int size = arrayList.size();
        while (posNote < size){
            arrayList.get(posNote).setId(posNote);
            database.addNote(arrayList.get(posNote));
            posNote++;
        }
        database.close();
    }

    public void loadDatabase(){
        posNote = 0;
        int size = database.getNoteCount();
        while ((size - 1) >= 0){
            arrayList.add(0, database.getNote(size-1));
            arrayList.get(0).setRemind(database.getNote(size-1).getRemind());
            size--;
        }
        adapterList.notifyDataSetChanged();
    }

}
