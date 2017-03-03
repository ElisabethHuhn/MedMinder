package com.androidchicken.medminder;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 5/15/17
 *
 * The class in charge of maintaining the set of instances of Schedule times for a medication
 *  both in-memory and in the DB
 */

public class MMSchedMedManager {
    /************************************/
    /********* Static Constants *********/
    /************************************/
    public static final int SCHED_NOT_FOUND = -1;


    /************************************/
    /********* Static Variables *********/
    /************************************/
    private static MMSchedMedManager ourInstance ;

    /************************************/
    /********* Member Variables *********/
    /************************************/
    //The list is kept on each medication, so no need for a list here
    //private ArrayList<MMScheduleMedication> mScheduleMedicationList;


    /************************************/
    /********* Static Methods   *********/
    /************************************/
    public static MMSchedMedManager getInstance() {
        if (ourInstance == null){
            ourInstance = new MMSchedMedManager();
        }
        return ourInstance;
    }


    /************************************/
    /********* Constructors     *********/
    /************************************/
    private MMSchedMedManager() {    }

    /************************************/
    /********* Setters/Getters  *********/
    /************************************/


    /*******************************************/
    /********* CRUD Methods   *********/
    /*******************************************/




    //return the cursor containing all the Concurrent Doses in the DB
    //that pertain to this personID
    public Cursor getAllScheduleMedicationsCursor (long medicationID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllSchedMedsCursor(medicationID);
    }





    public boolean removeSchedMedFromDB(long schedMedID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        long returnCode = databaseManager.removeSchedMed(schedMedID);
        if (returnCode == MMDatabaseManager.sDB_ERROR_CODE)return false;
        return true;
    }



    /********************************************/
    /********* Private Member Methods   *********/
    /********************************************/



    //The routine that actually adds the instance to DB
    public long addScheduleMedication(MMScheduleMedication scheduleMedication){
        long returnCode = MMDatabaseManager.sDB_ERROR_CODE;

        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        returnCode = databaseManager.addSchedMed(scheduleMedication);

        return returnCode;
    }


    /********************************************/
    /********* Public Member Methods    *********/
    /********************************************/

    public ArrayList<MMScheduleMedication> getAllSchedMeds(long medicationID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        ArrayList<MMScheduleMedication> times = databaseManager.getAllSchedMeds(medicationID);
        return times;
    }



    public ContentValues getCVFromScheduleMedication(MMScheduleMedication schedMed){
        ContentValues values = new ContentValues();
        values.put(MMDataBaseSqlHelper.SCHED_MED_ID,             schedMed.getSchedMedID());
        values.put(MMDataBaseSqlHelper.SCHED_MED_FOR_PERSON_ID,  schedMed.getForPersonID());
        values.put(MMDataBaseSqlHelper.SCHED_MED_OF_MEDICATION_ID,  schedMed.getOfMedicationID());
        values.put(MMDataBaseSqlHelper.SCHED_MED_TIME_DUE,       schedMed.getTimeDue());

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
    public MMScheduleMedication getScheduleMedicationFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        MMScheduleMedication scheduleMedications = new MMScheduleMedication(); //filled with defaults

        cursor.moveToPosition(position);

        scheduleMedications.setSchedMedID
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.SCHED_MED_ID)));

        scheduleMedications.setForPersonID
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.SCHED_MED_FOR_PERSON_ID)));

        scheduleMedications.setOfMedicationID
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.SCHED_MED_OF_MEDICATION_ID)));

        scheduleMedications.setTimeDue
                (cursor.getInt(cursor.getColumnIndex(MMDataBaseSqlHelper.SCHED_MED_TIME_DUE)));

        return scheduleMedications;
    }

}
