package com.androidchicken.medminder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

/**
 * Created by Elisabeth Huhn on 3/28/2017.
 * A service to set / clear notification alarms for all schedules on the current patient
 */

class MMNotificationAlarmService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @SuppressWarnings("static-access")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        Context context = this.getApplicationContext();

        sendNotificationNow(context, intent);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }


    private void sendNotificationNow(Context context, Intent intent){
        //Build the notification
        NotificationCompat.Builder builder = getNotifBuilder(context);

        //define what the action the notification should take

         /*  Set the Notification's Click Behavior  */
        // update the notification with the PendingIntent that will wake up the MainActivity
        //   when the notifiation is touched
        builder.setContentIntent(getNotifAction(context));


        /* Issue the Notification */
        // Set an ID for the notification. The ID allows the notification to be updated later
        int mNotificationId = 001;
        // Get an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Build the notification and issues it.
        mNotifyMgr.notify(mNotificationId, builder.build());

    }

    private NotificationCompat.Builder getNotifBuilder(Context context){
                /*  Create a Notification Builder  */
        return  (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ground_station_icon)
                .setContentTitle("MedMinder")
                .setContentText("Time To Take Medicine")
                //notification is canceled as soon as it is touched by the user
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
    }

    private PendingIntent getNotifAction(Context context){
               /*  Define the Notification 's Action  */
        //This action takes users directly from the notification to
        // the main Activity in MedMinder
        Intent resultIntent = new Intent(context, MMMainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Add the back stack
        stackBuilder.addParentStack(MMMainActivity.class);
        // Add the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        // Get a PendingIntent containing the entire back stack
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }



}