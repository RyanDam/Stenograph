package com.rstudio.notii_pro;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.MetadataBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SyncActivity extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    private Button backup, restore;
    private boolean modeBackup;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private TextView sync_text;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_sync);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // connect object to xml
        backup = (Button) findViewById(R.id.backup_bt);
        restore = (Button) findViewById(R.id.restore_bt);
        sync_text = (TextView) findViewById(R.id.sync_text);

        // setup instruction text
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/archer.ttf");
        String text = "You can backup your notes\n"
                + "on Cloud (Drive). You will need\n"
                + "internet access to use this function.\n\n"
                + "Click on Backup button to backup notes.\n"
                + "Click on Restore button to restore notes.\n\n"
                + "You will lose all current notes. So be careful";
        sync_text.setText(text);
        sync_text.setTypeface(font);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(SyncActivity.this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(SyncActivity.this)
                    .addOnConnectionFailedListener(SyncActivity.this)
                    .build();
        }

        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient != null) {
                    if (mGoogleApiClient.isConnected()) {
                        modeBackup = true;
                        doInConnect();
                    }
                    else {
                        show("You haven't connected to Google Drive");
                    }
                }
                else {
                    show("You haven't connected to Google Drive");
                }
            }
        });

        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient != null) {
                    if (mGoogleApiClient.isConnected()) {
                        modeBackup = false;
                        doInConnect();
                    }
                    else {
                        show("You haven't connected to Google Drive");
                    }
                }
                else {
                    show("You haven't connected to Google Drive");
                }
            }
        });

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("DriveAPI", "API client connected.");
//        show("Connected to drive");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(SyncActivity.this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(SyncActivity.this)
                    .addOnConnectionFailedListener(SyncActivity.this)
                    .build();
        }
        mGoogleApiClient.connect();
        super.onResume();
    }

    public void doInConnect () {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "NotesDatabases.db"))
                .build();
        // Query if NotesDatabases exist in the App Folder
        Drive.DriveApi.getAppFolder(mGoogleApiClient).queryChildren(mGoogleApiClient, query)
                .setResultCallback(queryCallback);
    }

    private ResultCallback<MetadataBufferResult> queryCallback
            = new ResultCallback<MetadataBufferResult>() {
        @Override
        public void onResult(MetadataBufferResult metadataBufferResult) {
            if (!metadataBufferResult.getStatus().isSuccess()) {
                if (!modeBackup) {
                    show("Database backup not found");
                } else {
                    saveNewFile();
                }
                return;
            }

            MetadataBuffer data = metadataBufferResult.getMetadataBuffer();
            if (data.getCount() <= 0) {
                if (!modeBackup) {
                    show("Database backup not found");
                } else {
                    saveNewFile();
                }
            }
            else {
                DriveId fileID = metadataBufferResult.getMetadataBuffer().get(0).getDriveId();
                Drive.DriveApi.fetchDriveId(mGoogleApiClient, fileID.getResourceId())
                        .setResultCallback(fetchIDCallback);
            }
        }
    };

    ResultCallback<DriveIdResult> fetchIDCallback
            = new ResultCallback<DriveIdResult>() {
        @Override
        public void onResult(DriveIdResult driveIdResult) {
            if (!driveIdResult.getStatus().isSuccess()) {
                if (!modeBackup) {
                    show("Drive ID not found");
                } else {
                    saveNewFile();
                }
                return;
            }
            // Start edit NotesDatabases
            DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, driveIdResult.getDriveId());
            new EditContentASyncTask(SyncActivity.this).execute(file);
        }
    };

    public class EditContentASyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {

        public EditContentASyncTask(Context context) {
            super(context);
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveFile... args) {
            DriveFile file = args[0];
            if (!modeBackup) {
                DriveContentsResult driveContentsResult = file.open(
                        getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    return false;
                }
                DriveContents content = driveContentsResult.getDriveContents();
                InputStream inputStream = content.getInputStream();
                setDB(inputStream);
                return true;
            }
            else {
                DriveContentsResult driveContentsResult = file.open(
                        getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    return false;
                }
                DriveContents content = driveContentsResult.getDriveContents();
                OutputStream outputStream = content.getOutputStream();
                try {
                    outputStream.write(getDBBytes());
                } catch (IOException e) {
                    show("Can't write data backup");
                }
                com.google.android.gms.common.api.Status status
                        = content.commit(getGoogleApiClient(), null).await();
                return status.getStatus().isSuccess();
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                if (!modeBackup) {
                    show("Database backup failed");
                } else {
                    show("Error while editing contents");
                }
                return;
            }
            if (!modeBackup) {
                // this will stop app when save data done
                show("Data restore successfully\nRestart Stenograph please");
                setResult(MainActivity.RESTORE_DONE);
                finish();
            } else {
                show("Contents edited successfully");
            }
        }
    }

    // in case there are no backup exist on drive, make a new one
    private void saveNewFile() {
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveContentsResult>() {
            @Override
            public void onResult(DriveContentsResult driveContentsResult) {
                if (!driveContentsResult.getStatus().isSuccess()) {
                    show("Can't create new data backup");
                    return;
                }
                final DriveContents contents = driveContentsResult.getDriveContents();
                new Thread () {
                    @Override
                    public void run () {
                        OutputStream outputStream = contents.getOutputStream();
                        try {
                            outputStream.write(getDBBytes());
                        } catch (IOException e) {
                            show("Can't write data backup");
                        }
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setMimeType(MimeTypeMap.getSingleton().getExtensionFromMimeType("db"))
                                .setTitle("NotesDatabases.db")
                                .setStarred(true)
                                .build();
                        Drive.DriveApi.getAppFolder(mGoogleApiClient)
                                .createFile(mGoogleApiClient, changeSet, contents)
                                .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                    @Override
                                    public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                                        if (!driveFileResult.getStatus().isSuccess()) {
                                            show("Google drive can't create data backup");
                                            return;
                                        }
                                        show("Backup completed");
                                    }
                                });
                    }
                }.start();
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // suspend handle here
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Called whenever the API client fails to connect.
        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e("DriveAPI", "Exception while starting resolution activity", e);
        }
    }

    // show toast message
    private void show(String in) {
        Toast.makeText(getApplicationContext(), in, Toast.LENGTH_SHORT).show();
    }

    // get byte[] buffer from Database
    private byte[] getDBBytes() {
        Context ctx = getApplicationContext();
        byte[] out = null;
        try {
            File from = ctx.getDatabasePath(ctx.getString(R.string.database_name));
            if (from.exists())
                out = stream2Bytes(new FileInputStream(from));
        } catch (Exception e) {}
        return out;
    }

    // set database from InputStream Drive content
    public void setDB(InputStream is) {
        Context ctx = getApplicationContext();
        // must close database before edited
        MainActivity.database.close();
        try {
            getApplicationContext().deleteDatabase(ctx.getString(R.string.database_name));
            File ii = getApplicationContext().getDatabasePath(ctx.getString(R.string.database_name));
            stream2File(is, ii);
        } catch (Exception e) {}
    }

    // convert input stream to byte[]
    public byte[] stream2Bytes(InputStream is) {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        BufferedInputStream bufIS = null;
        if (is != null)
            try {
                bufIS = new BufferedInputStream(is);
                int cnt = 0;
                while ((cnt = bufIS.read(buffer)) >= 0) {
                    byteBuffer.write(buffer, 0, cnt);
                }
            } catch (Exception e) {}
            finally {
                try {
                    if (bufIS != null)
                        bufIS.close();
                } catch (IOException e) {}
            }
        return byteBuffer.toByteArray();
    }

    // convert input stream to file
    private void stream2File(InputStream inStream, File flNm) {
        try {
            OutputStream outStream = new FileOutputStream(flNm);
            try {
                try {
                    final byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inStream.read(buffer)) != -1)
                        outStream.write(buffer, 0, read);
                    outStream.flush();
                } finally {outStream.close();}
            } catch (Exception e) {}
            inStream.close();
        } catch (Exception e) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sync, menu);
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
