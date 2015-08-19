package com.rstudio.notii_pro;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.origamilabs.library.views.StaggeredGridView;
import com.origamilabs.library.views.StaggeredGridView.OnItemClickListener;
import com.origamilabs.library.views.StaggeredGridView.OnItemLongClickListener;
import com.rstudio.notii_pro.adapters.NoteAdapter;
import com.rstudio.notii_pro.database.DatabaseMng;
import com.rstudio.notii_pro.item.CheckItem;
import com.rstudio.notii_pro.item.NoteItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends ActionBarActivity {

    public static final int CREATE_NEW_NOTE = 1;
    public static final int EDIT_NOTE = 2;
    public static final int LOGIN = 4;
    public static final int LOGIN_STATUS_BACK = 5;
    public static final int SYNC_TASK = 6;
    public static final int RESTORE_DONE = 6;

    private NoteItem note;
    private StaggeredGridView list;
    static ArrayList<NoteItem> arrayList;
    public NoteAdapter adapterList;
    public Intent mMainToEdit;
    private Intent intent;
    private SharedPreferences sharedPref;
    private boolean loginMode;
    private boolean quickOpenNote = false;

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

        getSupportActionBar().setElevation(0);

        // turn login screen on
        loginMode = true;

        // initial data
        readingSetup(this);

        // turn login screen off to not show login screen again
        loginMode = false;

        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(StaggeredGridView parent, View view, int position, long id) {
                if (arrayList.get(position).getText().compareTo("") == 0) {
                    // if it doesn't have any text
                    mMainToEdit.putExtra("CheckMode", true);
                } else {
                    mMainToEdit.putExtra("CheckMode", false);
                }
                mMainToEdit.putExtra("Note", arrayList.get(position).getId());
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
    public void onResume() {
        loadDatabase();
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
        else if (id == R.id.action_check_list) {
            mMainToEdit.putExtra("WishId", (getMaxNoteId() + 1));
            Log.d("max note", "" + (getMaxNoteId() + 1));
            mMainToEdit.putExtra("CheckMode", true);
            mMainToEdit.putExtra("Note", -1);
            startActivity(mMainToEdit);
            return true;
        }
        else if (id == R.id.action_plus) {
            mMainToEdit.putExtra("Note", -1);
            mMainToEdit.putExtra("CheckMode", false);
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
        else if (id == R.id.action_share_app) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String string = getResources().getString(R.string.share_text);
            intent.putExtra(Intent.EXTRA_TEXT, string);
            startActivity(intent);
        }
        else if (id == R.id.action_rate_app) {
            Intent intent = new Intent();
            intent.setType(Intent.ACTION_VIEW)
                    .setData(Uri.parse(getResources().getString(R.string.market_rate)));
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN && resultCode == LOGIN_STATUS_BACK) {
            boolean status = data.getBooleanExtra("login_status", false);
            if (!status) {
                finish();
            }
        }
        else if (requestCode == SYNC_TASK && resultCode == RESTORE_DONE) {
            finish();
        }

    }

    // Initial setup for main activity
    private void readingSetup(Context context) {

        intent = getIntent();
        quickOpenNote = intent.getBooleanExtra("QuickOpenNote", false);

        // Connect Object to xml /////////////////////////////////////////
        list = (StaggeredGridView) findViewById(R.id.list_note_staggered);
        arrayList = new ArrayList<NoteItem>();
        /////////////////////////////////////////////////////////////////

        // Setup for ListView
        adapterList = new NoteAdapter(MainActivity.this, arrayList, list);
        list.setAdapter(adapterList);

        // Setup Database
        database = new DatabaseMng(context);
        database.checkAndCreateTable();

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

        // Make an Instruction if list note is empty
        if (database.getNoteCount() <= 0){
            note = new NoteItem(99, "Instruction tip","Welcome to Stenograph\nStenograph is the most simple app you have ever seen\nPress the Plus button and write down on the top box and make a quick note\nHope you have fun with this app ;)", "Deer, the actor of this app", getResources().getColor(R.color.blue), true);
            database.addNote(note);
            adapterList.notifyDataSetChanged();
        }

        // setup list column number
        String list_column_setting = sharedPref.getString("list_column", "2");
        list.setColumnCount(Integer.parseInt(list_column_setting));

        if (quickOpenNote) {
            NoteItem item = database.getNote(intent.getIntExtra("ID", -1));
            if (item != null) {
                if (item.getText().compareTo("") == 0) {
                    mMainToEdit.putExtra("CheckMode", true);
                }
                else {
                    mMainToEdit.putExtra("CheckMode", false);
                }
            }
            mMainToEdit.putExtra("Note", intent.getIntExtra("ID", -1));
            startActivityForResult(mMainToEdit, EDIT_NOTE);
        }

    }

    public Dialog longClickNotes(Bundle bundle) {
        final int position = bundle.getInt("Position", -1);
        if (position == -1) return null;
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(MainActivity.this);
        if (arrayList.get(position).getBold()) {
            dialog.items(R.array.long_click_note_mark)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                            if (i == 0) {
                                NoteItem item = database.getNote(arrayList.get(position).getId());
                                item.setBold(false);
                                database.updateNote(item);
                            } else if (i == 2) {
                                database.removeCheckItemWithNoteId(arrayList.get(position).getId());
                                database.removeNote(arrayList.get(position).getId());
                            }
                            else if (i == 1) {
                                if (arrayList.get(position).getText().compareTo("") == 0) {
                                    ArrayList<CheckItem> tempCheckItems = database
                                            .getAllCheckItemWithNoteID(arrayList.get(position).getId());

                                    String text = "- ";
                                    for (int j = 0; j < tempCheckItems.size() - 1; j++) {
                                        if (j == tempCheckItems.size() - 2) {
                                            text = text + tempCheckItems.get(j).getText();
                                        }
                                        else {
                                            text = text + tempCheckItems.get(j).getText() + "\n- ";
                                        }
                                    }

                                    Intent share = new Intent();
                                    share.setAction(Intent.ACTION_SEND);
                                    share.putExtra(Intent.EXTRA_TITLE, arrayList.get(position).getTitle());
                                    share.putExtra(Intent.EXTRA_TEXT, text);
                                    share.setType("text/plain");
                                    startActivity(share);
                                }
                                else {
                                    Intent share = new Intent();
                                    share.setAction(Intent.ACTION_SEND);
                                    share.putExtra(Intent.EXTRA_TITLE, arrayList.get(position).getTitle());
                                    share.putExtra(Intent.EXTRA_TEXT, arrayList.get(position).getText());
                                    share.setType("text/plain");
                                    startActivity(share);
                                }
                            }
                            loadDatabase();
                        }
                    });
        }
        else {
            dialog.items(R.array.long_click_note_no_mark)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                            if (i == 0) {
                                NoteItem item = database.getNote(arrayList.get(position).getId());
                                item.setBold(true);
                                database.updateNote(item);
                            }
                            else if (i == 2) {
                                database.removeCheckItemWithNoteId(arrayList.get(position).getId());
                                database.removeNote(arrayList.get(position).getId());
                            }
                            else if (i == 1) {
                                if (arrayList.get(position).getText().compareTo("") == 0) {
                                    ArrayList<CheckItem> tempCheckItems = database
                                            .getAllCheckItemWithNoteID(arrayList.get(position).getId());

                                    String text = "- ";
                                    for (int j = 0; j < tempCheckItems.size() - 1; j++) {
                                        if (j == tempCheckItems.size() - 2) {
                                            text = text + tempCheckItems.get(j).getText();
                                        }
                                        else {
                                            text = text + tempCheckItems.get(j).getText() + "\n- ";
                                        }
                                    }

                                    Intent share = new Intent();
                                    share.setAction(Intent.ACTION_SEND);
                                    share.putExtra(Intent.EXTRA_TITLE, arrayList.get(position).getTitle());
                                    share.putExtra(Intent.EXTRA_TEXT, text);
                                    share.setType("text/plain");
                                    startActivity(share);
                                }
                                else {
                                    Intent share = new Intent();
                                    share.setAction(Intent.ACTION_SEND);
                                    share.putExtra(Intent.EXTRA_TITLE, arrayList.get(position).getTitle());
                                    share.putExtra(Intent.EXTRA_TEXT, arrayList.get(position).getText());
                                    share.setType("text/plain");
                                    startActivity(share);
                                }
                            }
                            loadDatabase();
                        }
                    });
        }
        return dialog.build();
    }

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

    private NoteItem getNote(Bundle bundle) {
        NoteItem note = new NoteItem();
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

    private void loadDatabase(){
        arrayList.clear();
        if (database.getAllNote() != null)
            arrayList.addAll(database.getAllNote());
        adapterList.notifyDataSetChanged();
    }

    private int getMaxNoteId() {
        int max = 0;
        for (int i = 0; i < arrayList.size(); i++) {
            if (max < arrayList.get(i).getId()) {
                max = arrayList.get(i).getId();
            }
        }
        return max;
    }
}
