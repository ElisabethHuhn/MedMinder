package com.androidchicken.medminder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by Elisabeth Huhn on 3/28/2017.
 * A service to convert alarms into notifications
 */

public class MMAlertAlarmService extends Service {
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

        //Initialize the DB if necessary
        MMDatabaseManager databaseManager = null;
        try {
            //initialize the database
            databaseManager = MMDatabaseManager.getInstance(context);
        }catch (Exception e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }

        //Get all the Medication Alerts in the DB










        //asking for personID = MMUtilities.ID_DOES_NOT_EXIST means to return
        // ALL the MedicationAlerts in the DB
        ArrayList<MMMedicationAlert> medicationAlerts =
                databaseManager.getMedicationAlerts(MMUtilities.ID_DOES_NOT_EXIST);


        //no medication alerts, no alarms to set
        if (medicationAlerts == null)return START_NOT_STICKY;

        //loop through all medicationAlert,
        //  and notify the proper person appropriately
        MMMedicationAlert medicationAlert;
        int lastMedAlert = medicationAlerts.size();
        int positionMedAlert = 0;

        while (positionMedAlert < lastMedAlert){
            medicationAlert = medicationAlerts.get(positionMedAlert);

            //loop through all the MEDICATIONs on the person
            //  and set an alarm for each schedule
            ArrayList<MMMedication> medications = null;
            if (medications != null) {
                MMMedication medication;
                int lastMedication = medications.size();
                int positionMedicaiton = 0;
                while (positionMedicaiton < lastMedication){
                    medication = medications.get(positionMedicaiton);
                    //only set alarms for current medications that are on schedule
                    if (medication.isCurrentlyTaken() ){

                        //loop through all SCHEDULEs, setting an alarm for each
                        ArrayList<MMScheduleMedication> schedules = medication.getSchedules();
                        if (schedules != null){
                            MMScheduleMedication schedule;
                            int lastSchedule = schedules.size();
                            int positionSchedule = 0;
                            while (positionSchedule < lastSchedule){
                                schedule = schedules.get(positionSchedule);
                                //create an Alarm to generate a notification for this scheduled dose
                                MMUtilities.scheduleNotification(context, schedule.getTimeDue());
                                positionSchedule++;
                            }//end schedule while loop
                        }
                    }
                    positionMedicaiton++;
                } //end medication while loop
            }
            positionMedAlert++;
        }//end person while loop

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return  START_NOT_STICKY;
    }
}