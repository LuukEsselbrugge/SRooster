package com.stenden.esselbrugge.stendenrooster;

/**
 * Created by luuk on 26-9-17.
 */

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class NotificationPublisher extends BroadcastReceiver {


    public void onReceive(Context context, Intent intent) {

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

            Map<String, Map<String, String>> CurrentSchedule = new HashMap<>();
            try {
                File file = new File(context.getDir("data", MODE_PRIVATE), "schedule");
                ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
                CurrentSchedule = (Map<String, Map<String, String>>)inputstream.readObject();
                inputstream.close();

                for (Map.Entry<String, Map<String, String>> entry : CurrentSchedule.entrySet()) {
                    String ID = entry.getKey();
                    Map<String, String> Data = entry.getValue();

                    Intent notificationIntent = new Intent(context, NotificationPublisher.class);
                    notificationIntent.putExtra("Title", Data.get("Title"));
                    notificationIntent.putExtra("Content", Data.get("Content"));
                    notificationIntent.putExtra("id",ID+"");
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(ID), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    long futureInMillis = Long.parseLong(Data.get("DelayFinal"));
                    AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
                }
            }
            catch(Exception e){
            Log.e("ERROR",e.toString());
            }

        }else {

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            int importance = NotificationManager.IMPORTANCE_HIGH;

            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel notificationChannel = new NotificationChannel("Schedule", "Schedule", importance);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Notification.Builder notification = new Notification.Builder(context);
            notification.setContentTitle(intent.getStringExtra("Title"))
                    .setContentText(intent.getStringExtra("Content"))
                    .setSmallIcon(R.drawable.ic_sroosterlogo)
                    .build();
            if (Build.VERSION.SDK_INT >= 26) {
                notification.setChannelId("Schedule");
            }

            notificationManager.notify(1, notification.build());

            try {
                File file = new File(context.getDir("data", MODE_PRIVATE), "schedule");
                ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
                Map<String,Map<String, String>> CurrentSchedule = (Map<String,Map<String, String>>)inputstream.readObject();
                inputstream.close();

                CurrentSchedule.remove(intent.getStringExtra("id"));

                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(CurrentSchedule);
                outputStream.flush();
                outputStream.close();
            }catch (Exception e){

            }
        }
    }
}