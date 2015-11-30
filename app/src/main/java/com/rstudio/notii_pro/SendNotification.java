package com.rstudio.notii_pro;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;

public class SendNotification extends Service {
    public SendNotification() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);
        Intent returnMain = new Intent(this, MainActivity.class);
        returnMain.putExtra("ID", intent.getIntExtra("ID", 0));
        returnMain.putExtra("QuickOpenNote", true);
        PendingIntent pending = PendingIntent.getActivity(this, 1, returnMain, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentIntent(pending)
                .setTicker(intent.getStringExtra("Text"))
                .setContentText(intent.getStringExtra("Text"))
                .setContentTitle("I got reminder for you")
                .setSmallIcon(R.drawable.noti_icon)
                .addAction(R.drawable.tick_yes, getResources().getString(R.string.ok), pending)
                .setVibrate(new long []{100, 250, 100, 500})
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        nm.notify(intent.getIntExtra("ID", 0), notification);
        // kill itself to save memory
        this.stopSelf();
    }
}
