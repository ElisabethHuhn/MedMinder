package com.androidchicken.medminder;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 4/21/17
 *
 * The class in charge of maintaining the set of instances of Medication Alert
 *
 *
 */

class MMMedicationAlertManager {
    //***********************************/
    //******** Static Constants  ********/
    //***********************************/




    //***********************************/
    //******** Static Variables  ********/
    //***********************************/
    private static MMMedicationAlertManager ourInstance ;

    //*************************************/
    //******** Member Variables   *********/
    //*************************************/
    //The medication lists exist on the Persons, rather than on a list here
    //private ArrayList<MMMedicationAlert> mMedicationAlertList;

    //***********************************/
    //******** Static Methods   *********/
    //***********************************/
    static MMMedicationAlertManager getInstance() {
        if (ourInstance == null){
            ourInstance = new MMMedicationAlertManager();
        }
        return ourInstance;
    }


    //***********************************/
    //******** Constructors     *********/
    //***********************************/
    private MMMedicationAlertManager() {

        //The medication list already exists on the Person instance
        //mMedicationList = new ArrayList<>();

    }

    //******************************************/
    //******** Public Member Methods   *********/
    //******************************************/



    //******************************************/
    //********     CRUD Methods        *********/
    //******************************************/

    //***********************  CREATE **************************************

    //The routine that actually adds the instance to DB
    long addMedicationAlert(MMMedicationAlert medicationAlert){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.addMedicationAlert(medicationAlert);
    }




    //***********************  READ **************************************
    //return the cursor containing all the Concurrent Doses in the DB
    //that pertain to this personID
    Cursor getAllMedicationAlertsCursor (long personID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllMedicationAlertsCursor(personID);
    }

    ArrayList<MMMedicationAlert> getMedicationAlerts(long personID, long medicationID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        //if the personID is the MMUtilities.ID_DOES_NOT_EXIST, then
        // all MMMedicationAlerts in the DB will be in the returned list
        return databaseManager.getMedicationAlerts(personID, medicationID);
    }


    MMMedicationAlert getMedicationAlert(long medAlertID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getMedicationAlert(medAlertID);
    }


    //***********************  UPDATE **************************************


    //***********************  DELETE **************************************

    boolean removeMedicationAlertFromDB(long medicationAlertID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        long returnCode = databaseManager.removeMedicationAlert(medicationAlertID);
        return ! (returnCode == MMDatabaseManager.sDB_ERROR_CODE);
    }


    //*******************************************/
    //******** Private Member Methods   *********/
    //*******************************************/


    //**********************  COPY **************************************


    //*******************************************/
    //***  Translation utility Methods   ********/
    //*******************************************/
    ContentValues getCVFromMedicationAlert(MMMedicationAlert medicationAlert){
        ContentValues values = new ContentValues();
        values.put(MMDataBaseSqlHelper.MEDICATION_ALERT_ID,             medicationAlert.getMedicationAlertID());
        values.put(MMDataBaseSqlHelper.MEDICATION_ALERT_MEDICATION_ID,  medicationAlert.getMedicationID());
        values.put(MMDataBaseSqlHelper.MEDICATION_ALERT_FOR_PATIENT_ID, medicationAlert.getForPatientID());
        values.put(MMDataBaseSqlHelper.MEDICATION_ALERT_NOTIFY_PERSON_ID,medicationAlert.getNotifyPersonID());
        values.put(MMDataBaseSqlHelper.MEDICATION_ALERT_TYPE_NOTIFY,     medicationAlert.getNotifyType());
        values.put(MMDataBaseSqlHelper.MEDICATION_ALERT_OVERDUE_TIME, medicationAlert.getOverdueTime());

        return values;
    }

    //returns the MMMedicationAlert characterized by the position within the Cursor
    //returns null if the position is larger than the size of the Cursor
    //NOTE    this routine does NOT add the medicationAlert to the list maintained by this ,
    //        MedicationAlertManager. The caller of this routine is responsible for that.
    //        This is only a translation utility
    //WARNING As the app is not multi-threaded, this routine is not synchronized.
    //        If the app becomes multi-threaded, this routine must be made thread safe
    //WARNING The cursor is NOT closed by this routine. It assumes the caller will close the
    //         cursor when it is done with it
    MMMedicationAlert getMedicationAlertFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        //filled with defaults, no ID is assigned
        MMMedicationAlert medicationAlert = new MMMedicationAlert();

        cursor.moveToPosition(position);
        medicationAlert.setMedicationAlertID(cursor.getLong
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_ALERT_ID)));
        medicationAlert.setMedicationID(cursor.getLong
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_ALERT_MEDICATION_ID)));
        medicationAlert.setForPatientID(cursor.getLong
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_ALERT_FOR_PATIENT_ID)));
        medicationAlert.setNotifyPersonID(cursor.getLong
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_ALERT_NOTIFY_PERSON_ID)));
        medicationAlert.setNotifyType(cursor.getInt
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_ALERT_TYPE_NOTIFY)));
        medicationAlert.setOverdueTime(cursor.getInt
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_ALERT_OVERDUE_TIME)));


        return medicationAlert;
    }



}
