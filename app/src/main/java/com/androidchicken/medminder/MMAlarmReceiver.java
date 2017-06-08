package com.androidchicken.medminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Elisabeth Huhn on 3/28/2017.
 * Broadcast Receiver to handle turning alarms into notifications
 */

public class MMAlarmReceiver extends BroadcastReceiver {


    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION    = "notification";
    public static String ALARM_TYPE      = "alarm_type";
    public static String MED_ALERT_ID    = "medAlertID";

    public static int scheduleNotificationID = 1;
    public static int alertRequestCode = 2;

    public static int noTypeSpecified     = 0;
    public static int schedNotifAlarmType = 1;
    public static int alertAlarmType      = 2;



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null){
            int type = intent.getIntExtra(ALARM_TYPE, noTypeSpecified);
            if (type == schedNotifAlarmType) {
                scheduleNotification(context, intent);
            } else if (type == alertAlarmType){
                //It is time to send an alert via email or text
                sendAlert(context, intent);
            } //otherwise do nothing with the intent

        } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            // A boot has just happened,
            //start service to set the alarms for all people/all medications/all services
            Intent bootAlarmService = new Intent(context, MMBootAlarmService.class);
            context.startService(bootAlarmService);
        }
    }

    private void scheduleNotification(Context context, Intent intent) {
        //A single alarm has gone off, send the notification
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int notifID = intent.getIntExtra(NOTIFICATION_ID, scheduleNotificationID);
        // Get an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                            (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Build the notification and issues it.
        mNotifyMgr.notify(notifID, notification);
    }

    private void sendAlert(Context context, Intent intent){
        //The intent will tell us which Alert needs to be sent:
        long medAlertID = intent.getLongExtra(MMMedicationAlert.sMedicationIDTag,
                                                                    MMUtilities.ID_DOES_NOT_EXIST);

        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();
        MMMedicationAlert medAlert = medicationAlertManager.getMedicationAlert(medAlertID);
        if (medAlert == null)return;

        long medID = medAlert.getMedicationID();
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        MMMedication medication = medicationManager.getMedicationFromID(medID);

        MMDoseManager doseManager = MMDoseManager.getInstance();
        MMDose recentDose = doseManager.getMostRecentDose(medID);

        //The time the last dose was taken:
        long timeTakenMilli;
        if (recentDose != null) {
            timeTakenMilli = recentDose.getTimeTaken();
        } else {
            timeTakenMilli = 0; //They haven't taken one yet
        }

        MMUtilities.getInstance().sendAlert(context, medAlertID, timeTakenMilli);
    }
}