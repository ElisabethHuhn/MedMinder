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

public class MMMedicationAlertManager {
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
    public static MMMedicationAlertManager getInstance() {
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
    public long addMedicationAlert(MMMedicationAlert medicationAlert){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.addMedicationAlert(medicationAlert);
    }




    //***********************  READ **************************************
    //return the cursor containing all the Concurrent Doses in the DB
    //that pertain to this personID
    public Cursor getAllMedicationAlertsCursor (long personID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllMedicationAlertsCursor(personID);
    }

    public ArrayList<MMMedicationAlert> getMedicationAlerts(long personID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        //if the personID is the MMUtilities.ID_DOES_NOT_EXIST, then
        // all MMMedicationAlerts in the DB will be in the returned list
        return databaseManager.getMedicationAlerts(personID);
    }

    public ArrayList<MMMedicationAlert> getMedicationAlerts(long personID, long medicationID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        //if the personID is the MMUtilities.ID_DOES_NOT_EXIST, then
        // all MMMedicationAlerts in the DB will be in the returned list
        return databaseManager.getMedicationAlerts(personID, medicationID);
    }


    public MMMedicationAlert getMedicationAlert(long medAlertID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getMedicationAlert(medAlertID);
    }


    //***********************  UPDATE **************************************


    //***********************  DELETE **************************************

    //Because the list is on one person instance, we must also have the person ID
    //of the instance being manipulated
    //This routine not only removes from the in-memory list, but also from the DB
    public boolean removeMedicationAlert(long personID, int position) {
        //Now find that person using the PersonManager
        MMPersonManager   personManager =  MMPersonManager.getInstance();
        MMPerson person = personManager.getPerson(personID);

        //If person not found, return false.
        //  Can not add a medication to a person who does not exist
        if (person == null) return false;

        //determine if the medication already is associated with this person
        //start with the list of points contained in this project
        ArrayList<MMMedication> medicationList = person.getMedications();

        //if not, create one
        if (medicationList == null){
            medicationList = new ArrayList<>();
            person.setMedications(medicationList);
        }

        if (position > medicationList.size()) {
            //Can't remove a position that the list isn't long enough for
            return false;
        }

        //need to know the medication to be able to remove it from the DB
        MMMedication medication = medicationList.get(position);

        if (medication == null) return false; //it's already gone

        //remove it from memory
        boolean returnCode = medicationList.remove(medication);

        if (returnCode) {
            //ask the databaseManager to remove it from the DB as well
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            databaseManager.removeMedication(medication.getMedicationID());
        }
        return returnCode;
    }//end public remove position


    public boolean removeMedicationAlertFromDB(long medicationAlertID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        long returnCode = databaseManager.removeMedicationAlert(medicationAlertID);
        if (returnCode == MMDatabaseManager.sDB_ERROR_CODE)return false;
        return true;
    }


    //*******************************************/
    //******** Private Member Methods   *********/
    //*******************************************/


    //**********************  COPY **************************************


    //*******************************************/
    //***  Translation utility Methods   ********/
    //*******************************************/
    public ContentValues getCVFromMedicationAlert(MMMedicationAlert medicationAlert){
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
    public MMMedicationAlert getMedicationAlertFromCursor(Cursor cursor, int position){

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
