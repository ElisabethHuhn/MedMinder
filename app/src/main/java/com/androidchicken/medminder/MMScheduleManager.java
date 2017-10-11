package com.androidchicken.medminder;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

import static com.androidchicken.medminder.MMDataBaseSqlHelper.SCHED_MED_STRATEGY;

/**
 * Created by Elisabeth Huhn on 5/15/17
 *
 * The class in charge of maintaining the set of instances of Schedule times for a medication
 *  both in-memory and in the DB
 */

class MMScheduleManager {
    //***********************************/
    //******** Static Constants *********/
    //***********************************/


    //***********************************/
    //******** Static Variables *********/
    //***********************************/
    private static MMScheduleManager ourInstance ;

    //***********************************/
    //******** Member Variables *********/
    //***********************************/
    //The list is kept on each medication, so no need for a list here
    //private ArrayList<MMScheduleMedication> mScheduleMedicationList;


    //***********************************/
    //******** Static Methods   *********/
    //***********************************/
    static MMScheduleManager getInstance() {
        if (ourInstance == null){
            ourInstance = new MMScheduleManager();
        }
        return ourInstance;
    }


    //***********************************/
    //******** Constructors     *********/
    //***********************************/
    private MMScheduleManager() {    }

    //***********************************/
    //******** Setters/Getters  *********/
    //***********************************/


    //******************************************/
    //******** CRUD Methods   *********/
    //******************************************/


    //The routine that actually adds the instance to DB
    long addScheduleMedication(MMSchedule scheduleMedication){


        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.addSchedMed(scheduleMedication);

    }




    //return the cursor containing all the Schedules in the DB
    Cursor getAllSchedMedsCursor(){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllSchedMedsCursor();
    }


    //return the cursor containing all the Schedules in the DB
    //that pertain to this medicationID
    Cursor getAllSchedMedsCursor(long medicationID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllSchedMedsCursor(medicationID);
    }

    //return the cursor containing all the Schedules in the DB
    // ordered by time dose is taken
    //that pertain to this personID
    Cursor getAllSchedMedsForPersonCursor(long personID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        String orderClause = getSchedMedOrderClause();
        return databaseManager.getAllSchedMedsForPersonCursor(personID, orderClause);
    }

    private String getSchedMedOrderClause(){
        return MMDataBaseSqlHelper.SCHED_MED_STRATEGY + " ASC, " +
               MMDataBaseSqlHelper.SCHED_MED_TIME_DUE + " ASC";

    }




    ArrayList<MMSchedule> getAllSchedMeds(long medicationID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllSchedMeds(medicationID);
    }



    boolean removeSchedMedFromDB(long schedMedID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        long returnCode = databaseManager.removeSchedMed(schedMedID);
        //I know lint is complaining about simplifying this if stmt.
        //I'd rather make it explicit rather than cryptic
        return ! (returnCode == MMDatabaseManager.sDB_ERROR_CODE);
    }




    //*******************************************/
    //********       Utility methods       ******/
    //*******************************************/
    int howManyDueAt(int minutesSinceMidnight){
        Cursor cursor = getAllSchedMedsCursor();
        if (cursor == null) return 0;

        int last = cursor.getCount();
        if (last == 0) return 0;

        int position = 0;
        int count    = 0;

        while (position < last){
            if (minutesSinceMidnight == getTimeDueFromCursor(cursor, position))count++;
            position++;
        }
        cursor.close();

        return count;
    }

    //*******************************************/
    //******** Object to/from DB methods   ******/
    //*******************************************/

    ContentValues getCVFromScheduleMedication(MMSchedule schedMed){
        ContentValues values = new ContentValues();
        values.put(MMDataBaseSqlHelper.SCHED_MED_ID,             schedMed.getSchedMedID());
        values.put(MMDataBaseSqlHelper.SCHED_MED_FOR_PERSON_ID,  schedMed.getForPersonID());
        values.put(MMDataBaseSqlHelper.SCHED_MED_OF_MEDICATION_ID,  schedMed.getOfMedicationID());
        values.put(MMDataBaseSqlHelper.SCHED_MED_TIME_DUE,       schedMed.getTimeDue());
        values.put(SCHED_MED_STRATEGY,       schedMed.getStrategy());

        return values;
    }

    //returns the ScheduleMedications characterized by the position within the Cursor
    //returns null if the position is larger than the size of the Cursor
    //NOTE    this routine does NOT add the ScheduleMedication to the list maintained by this ScheduleMedicationManager
    //        The caller of this routine is responsible for that.
    //        This is only a translation utility
    //WARNING As the app is not multi-threaded, this routine is not synchronized.
    //        If the app becomes multi-threaded, this routine must be made thread safe
    //WARNING The cursor is NOT closed by this routine. It assumes the caller will close the
    //         cursor when it is done with it
    MMSchedule getScheduleMedicationFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        MMSchedule scheduleMedications = new MMSchedule(); //filled with defaults

        cursor.moveToPosition(position);

        scheduleMedications.setSchedMedID
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.SCHED_MED_ID)));

        scheduleMedications.setForPersonID
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.SCHED_MED_FOR_PERSON_ID)));

        scheduleMedications.setOfMedicationID
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.SCHED_MED_OF_MEDICATION_ID)));

        scheduleMedications.setTimeDue
                (cursor.getInt(cursor.getColumnIndex(MMDataBaseSqlHelper.SCHED_MED_TIME_DUE)));

        scheduleMedications.setStrategy(
                (cursor.getInt(cursor.getColumnIndex(SCHED_MED_STRATEGY))));


        return scheduleMedications;
    }

    long getScheduleIDFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return MMUtilities.ID_DOES_NOT_EXIST;

        long scheduleID;

        cursor.moveToPosition(position);

        scheduleID = (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.SCHED_MED_ID)));

        return scheduleID;
    }


    private int getTimeDueFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return -1;

        int timeDue;

        cursor.moveToPosition(position);

        timeDue = (cursor.getInt(cursor.getColumnIndex(MMDataBaseSqlHelper.SCHED_MED_TIME_DUE)));

        return timeDue;
    }




}
