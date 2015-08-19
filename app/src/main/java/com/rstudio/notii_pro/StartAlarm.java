package com.rstudio.notii_pro;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.rstudio.notii_pro.database.DatabaseMng;
import com.rstudio.notii_pro.item.NoteItem;

import java.util.ArrayList;
import java.util.Calendar;

public class StartAlarm extends Service {

    public StartAlarm() {
    }

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
        Calendar now = Calendar.getInstance();
        Intent passNotify = new Intent(this, SendNotification.class);
        ArrayList<NoteItem> data = database.getAllNote();

        // Send pending intent alarm if there is remind
        for (int i=0 ; i < data.size() ; i++){
            if (data.get(i).getRemind() > now.getTimeInMillis()) {
                passNotify.putExtra("Text", data.get(i).getTitle());
                passNotify.putExtra("ID", data.get(i).getId());
                PendingIntent pending = PendingIntent.getService(this, i, passNotify, PendingIntent.FLAG_UPDATE_CURRENT);
                am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, data.get(i).getRemind(), pending);
            }
        }

        // kill itself to save memory
        this.stopSelf();
    }

}
