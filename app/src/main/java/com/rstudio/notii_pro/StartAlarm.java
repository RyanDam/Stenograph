package com.rstudio.notii_pro;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Calendar;

public class StartAlarm extends Service {
    public StartAlarm() {
    }

    int databaseSize = 0;
    public static AlarmManager am;
    public static DatabaseMng database;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);
        database = new DatabaseMng(this);
        databaseSize = database.getNoteCount();
        Calendar now = Calendar.getInstance();
        Intent passNotify = new Intent(this, SendNotification.class);
        // Send pending intent alarm if there is remind
        for (int i=1 ; i < databaseSize ; i++){
            if ( database.getNote(i).getRemind() > now.getTimeInMillis() ){
                passNotify.putExtra("Text", database.getNote(i).getTitle());
                passNotify.putExtra("ID", i);
                PendingIntent pending = PendingIntent.getService(this, i, passNotify, PendingIntent.FLAG_UPDATE_CURRENT);
                am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, database.getNote(i).getRemind(), pending);
            }
        }
        // kill itself to save memory
        this.stopSelf();
    }

}
