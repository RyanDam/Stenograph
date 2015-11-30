package com.rstudio.notii_pro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.rstudio.notii_pro.adapters.MainAdapter;
import com.rstudio.notii_pro.anim.ShowUpCard;
import com.rstudio.notii_pro.database.DatabaseMng;
import com.rstudio.notii_pro.item.NoteItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class MainActivity extends AppCompatActivity {

    public static final int CREATE_NEW_NOTE = 1;
    public static final int EDIT_NOTE = 2;
    public static final int LOGIN = 4;
    public static final int LOGIN_STATUS_BACK = 5;
    public static final int SYNC_TASK = 6;
    public static final int RESTORE_DONE = 6;

    private NoteItem note;
//    private StaggeredGridView list;
    static ArrayList<NoteItem> arrayList;
//    public NoteAdapter adapterList;
    public Intent mMainToEdit;
    private Intent intent;
    private SharedPreferences sharedPref;
    private boolean loginMode;
    private boolean quickOpenNote = false;
    private View mMainHolder;
    private View mSearchHolder;

    private RecyclerView mRecycleView;
    private MainAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private EditText mSearchInput;
    private ImageButton mSearchButton;
    private boolean searchMode = false;
    private Toolbar mToolbar;
    private Animation searchDropdow, searchSlideAway;

    private Context mContext;

    Calendar now;
    SimpleDateFormat getTime;
    SimpleDateFormat getDate;

    public static DatabaseMng database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(mToolbar);

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

    }

    @Override
    public void onResume() {
        loadDatabase();
        // setup list column number
        // must reset state after back activity
        String list_column_setting = sharedPref.getString("list_column", "2");
//        list.setColumnCount(Integer.parseInt(list_column_setting));
        ((StaggeredGridLayoutManager)mLayoutManager).setSpanCount(Integer.parseInt(list_column_setting));
        if (true == searchMode) {
            arrayList.clear();
            arrayList.addAll(database.search(mSearchInput.getText().toString()));
            mAdapter.notifyDataChanged();
        }
        super.onResume();

        View view = ((MainActivity)mContext).getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
        else if (id == R.id.action_search) {
            toggleSearchMode();
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
//        list = (StaggeredGridView) findViewById(R.id.list_note_staggered);
        arrayList = new ArrayList<NoteItem>();
        /////////////////////////////////////////////////////////////////

        // new view /////////////////////////////////////////////////////
        mRecycleView = (RecyclerView) findViewById(R.id.main_list);
        mRecycleView.setHasFixedSize(true);
        mAdapter = new MainAdapter(context, arrayList, this);
        mLayoutManager = new StaggeredGridLayoutManager(2, 1);
        mRecycleView.setLayoutManager(mLayoutManager);
//        mRecycleView.setAdapter(mAdapter);
        mRecycleView.setAdapter(new SlideInBottomAnimationAdapter(mAdapter));
        mRecycleView.setItemAnimator(new SlideInUpAnimator());
        ////////////////////////////////////////////////////////////////

        mMainHolder = (findViewById(R.id.main_holder));
        mSearchButton = (ImageButton) findViewById(R.id.search_button);
        mSearchInput = (EditText) findViewById(R.id.search_input);
        mSearchHolder = findViewById(R.id.search_holder);
        setupSearch();


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
//            adapterList.notifyDataSetChanged();
            mAdapter.notifyDataChanged();
//            mRecycleView.
        }

        // setup list column number
        String list_column_setting = sharedPref.getString("list_column", "2");
//        list.setColumnCount(Integer.parseInt(list_column_setting));

        mSearchHolder.setVisibility(View.GONE);

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

    public void loadDatabase(){
        arrayList.clear();
        if (database.getAllNote() != null)
            arrayList.addAll(database.getAllNote());
        for (int i = 0; i < arrayList.size(); i++) {
            mAdapter.notifyDataChanged();
        }
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

    private void setupSearch() {
        TextWatcher watcher = new TextWatcher() {

            String text = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (text.compareTo(s.toString()) != 0) {
                    arrayList.clear();
                    arrayList.addAll(database.search(s.toString()));
                    mAdapter.notifyDataChanged();
                    text = s.toString();
                }
            }
        };

        mSearchInput.addTextChangedListener(watcher);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSearchMode();
//                arrayList.clear();
//                arrayList.addAll(database.search(""));
//                mAdapter.notifyDataChanged();
                loadDatabase();
            }
        });

        searchDropdow = AnimationUtils.loadAnimation(this, R.anim.search_dropdown);
        searchSlideAway = AnimationUtils.loadAnimation(this, R.anim.search_scoll_up);

        final Animation toolZoomIn = AnimationUtils.loadAnimation(this, R.anim.search_zoomin);
        final Animation toolZoomOut = AnimationUtils.loadAnimation(this, R.anim.search_zoomout);

        toolZoomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mToolbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        toolZoomOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mToolbar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        searchDropdow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mSearchHolder.setVisibility(View.VISIBLE);
                mToolbar.startAnimation(toolZoomOut);

                mSearchInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchInput, InputMethodManager.SHOW_IMPLICIT);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        searchSlideAway.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mToolbar.startAnimation(toolZoomIn);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mSearchHolder.setVisibility(View.GONE);

                View view = ((MainActivity)mContext).getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
//                mSearchHolder.requestFocus();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void toggleSearchMode() {
        if (false == searchMode) {
            mMainHolder.setBackgroundColor(getResources().getColor(R.color.main_search_background));
            mSearchHolder.startAnimation(searchDropdow);
            searchMode = true;
        }
        else {
            mMainHolder.setBackgroundColor(getResources().getColor(R.color.background_color));
            mSearchHolder.startAnimation(searchSlideAway);
            searchMode = false;
        }
    }

}
