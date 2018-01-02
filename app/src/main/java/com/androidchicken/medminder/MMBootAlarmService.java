package com.androidchicken.medminder;

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

public class MMBootAlarmService extends Service {

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
        MMDatabaseManager databaseManager;
        try {
            //initialize the database
            databaseManager = MMDatabaseManager.getInstance(context);
        }catch (Exception e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }

        //Set alarms / notifications for all the currently active people in the DB
        MMPersonManager personManager = MMPersonManager.getInstance();
        ArrayList<MMPerson> people = personManager.getPersonList(true);

        //no people, no alarms to set
        if (people == null)return START_NOT_STICKY;

        //loop through all PEOPLE,
        //  and set an alarm for each schedule
        MMPerson person;
        int lastPerson = people.size();
        int positionPerson = 0;
        while (positionPerson < lastPerson){
            person = people.get(positionPerson);

            //loop through all the MEDICATIONs on the person
            //  and set an alarm for each schedule
            ArrayList<MMMedication> medications = person.getMedications();
            if (medications != null) {
                MMMedication medication;
                int lastMedication = medications.size();
                int positionMedicaiton = 0;
                while (positionMedicaiton < lastMedication){
                    medication = medications.get(positionMedicaiton);
                    //only set alarms for current medications that are on schedule
                    if ((medication.isCurrentlyTaken() ) &&
                        (medication.getDoseStrategy() != MMMedication.sAS_NEEDED)){

                        //loop through all SCHEDULEs, setting an alarm for each
                        // TODO: 12/31/2017 This needs to happen by strategy
                        ArrayList<MMSchedule> schedules = medication.getSchedules();
                        if (schedules != null){
                            MMSchedule schedule;
                            int lastSchedule = schedules.size();
                            int positionSchedule = 0;
                            while (positionSchedule < lastSchedule){
                                schedule = schedules.get(positionSchedule);
                                //create an Alarm to generate a notification for this scheduled dose
                                MMUtilities utilities = MMUtilities.getInstance();
                                // TODO: 12/11/2017 this casting of context is WRONG!!!!!
                                // Mostly it just needs context,
                                // but to check settings it needs MMMainActivity
                                utilities.createScheduleNotification(
                                                    (MMMainActivity)context,
                                                    schedule.getTimeDue(),
                                                    medication.getMedicationID(),
                                                    medication.getMedicationNickname().toString());
                                positionSchedule++;
                            }//end schedule while loop
                        }
                    }
                    positionMedicaiton++;
                } //end medication while loop
            }
            positionPerson++;
        }//end person while loop

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return  START_NOT_STICKY;
    }
}