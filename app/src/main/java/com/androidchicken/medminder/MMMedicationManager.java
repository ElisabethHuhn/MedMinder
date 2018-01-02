package com.androidchicken.medminder;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 10/17/2016.
 *
 * The class in charge of maintaining the set of instances of Medication
 *  both in-memory and in the DB
 *
 */

class MMMedicationManager {
    //-**********************************/
    //-******* Static Constants  ********/
    //-**********************************/

    private static final int MEDICATION_NOT_FOUND = -1;


    //-**********************************/
    //-******* Static Variables  ********/
    //-**********************************/
    private static MMMedicationManager ourInstance ;

    //-************************************/
    //-******* Member Variables   *********/
    //-************************************/
    //The medication lists exist on the Persons, rather than on a list here
    //private ArrayList<MMMedication> mMedicationList;

    //-**********************************/
    //-******* Static Methods   *********/
    //-**********************************/
    static MMMedicationManager getInstance() {
        if (ourInstance == null){
            ourInstance = new MMMedicationManager();
        }
        return ourInstance;
    }


    //-**********************************/
    //-******* Constructors     *********/
    //-**********************************/
    private MMMedicationManager() {

        //The medication list already exists on the Person instance
        //mMedicationList = new ArrayList<>();

    }

    //-*****************************************/
    //-******* Public Member Methods   *********/
    //-*****************************************/



    //-*****************************************/
    //-*******     CRUD Methods        *********/
    //-*****************************************/

    ///-*********************  CREATE **************************************

    //This routine not only adds to the in memory list,
    // but has an argument, that if true,  also adds to the DB
    //returns FALSE if for any reason the medication can not be added
    //Use this is you already have the Person object in hand
    boolean addToPerson(MMPerson person, MMMedication newMedication, boolean addToDB){
        //  Can not add a medication to a person who does not exist
        if ((person == null) || (newMedication == null)) return false;

        //Find the person the medication is for
        long medPersonID = newMedication.getForPersonID();
        long personID    = person.getPersonID();
        //The medication and the person must point at each other
        if (medPersonID != personID) return false;

        //determine if the medication already is associated with this person
        //Get all the medications, deleted or not
        ArrayList<MMMedication> medicationList = person.getMedications(false);

        //If this is the first, the list will be empty
        if (medicationList == null){
            medicationList = new ArrayList<>();
            person.setMedications(medicationList);
        }

        //Get the DB Manager to help with DB
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();

        //determine whether the medication already exists in the list
        int atPosition = findMedicationPosition(medicationList, newMedication.getMedicationID());
        if (atPosition == MEDICATION_NOT_FOUND){//The medication does not already exist. Add it
            medicationList.add(newMedication);
        } else {
            //The medication does exist, replace the instance already in the list with this instance
            medicationList.set(atPosition, newMedication);
        }
        if (addToDB) {
            //  Add or update the medication to/in the DB
            databaseManager.addMedication(newMedication);
        }


        return true;
    }

    //just add to the DB. Assume already on person
    long addMedicationToDB(MMMedication medication){
        return MMDatabaseManager.getInstance().addMedication(medication);
    }


    ///-*********************  READ **************************************
    //return the cursor containing all the Concurrent Doses in the DB
    //that pertain to this personID
    Cursor getAllMedicationsCursor (long personID, boolean currentOnly){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllMedicationsCursor(personID, currentOnly);
    }


    MMMedication getMedicationFromID(long medicationID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getMedication(medicationID);
    }

    ///-*********************  UPDATE **************************************


    ///-*********************  DELETE **************************************



    //-******************************************/
    //-******* Private Member Methods   *********/
    //-******************************************/

    //Find the position of the medication instance
    //     that matches the argument medicationID
    //     within the argument list medicationList
    //returns constant = MEDICATION_NOT_FOUND if the medication is not in the list
    //NOTE it is a RunTimeException to call this routine if the list is null or empty
    private int findMedicationPosition(ArrayList<MMMedication> medicationList, long medicationID){
        MMMedication medication;
        int position        = 0;
        int last            = medicationList.size();

        //Determine whether an instance of the medication is already in the list
        //NOTE that if list is empty, while doesn't loop even once
        while (position < last){
            medication = medicationList.get(position);

            if (medication.getMedicationID() == medicationID){
                //Found the medication in the list at this position
                return position;
            }
            position++;
        }
        return MEDICATION_NOT_FOUND;
    }



    ///-*********************  COPY **************************************


    //-******************************************/
    //-**  Translation utility Methods   ********/
    //-******************************************/
    ContentValues getCVFromMedication(MMMedication medication){
        ContentValues values = new ContentValues();
        values.put(MMDataBaseSqlHelper.MEDICATION_ID,            medication.getMedicationID());
        values.put(MMDataBaseSqlHelper.MEDICATION_FOR_PERSON_ID, medication.getForPersonID());
        values.put(MMDataBaseSqlHelper.MEDICATION_BRAND_NAME,    medication.getBrandName().toString());
        values.put(MMDataBaseSqlHelper.MEDICATION_GENERIC_NAME,  medication.getGenericName().toString());
        values.put(MMDataBaseSqlHelper.MEDICATION_NICK_NAME,     medication.getMedicationNickname().toString());
        values.put(MMDataBaseSqlHelper.MEDICATION_DOSE_STRATEGY, medication.getDoseStrategy());
        values.put(MMDataBaseSqlHelper.MEDICATION_DOSE_AMOUNT,   medication.getDoseAmount());
        values.put(MMDataBaseSqlHelper.MEDICATION_DOSE_UNITS,    medication.getDoseUnits().toString());
        values.put(MMDataBaseSqlHelper.MEDICATION_DOSE_NUM_PER_DAY,medication.getDoseNumPerDay());
        values.put(MMDataBaseSqlHelper.MEDICATION_NOTES,         medication.getNotes().toString());
        values.put(MMDataBaseSqlHelper.MEDICATION_SIDE_EFFECTS,  medication.getSideEffects().toString());
        int booleanValue = 0; //default is false
        if (medication.isCurrentlyTaken())booleanValue = 1; //true
        values.put(MMDataBaseSqlHelper.MEDICATION_CURRENTLY_TAKEN, booleanValue);

        return values;
    }

    //returns the MMMedication characterized by the position within the Cursor
    //returns null if the position is larger than the size of the Cursor
    //NOTE    this routine does NOT add the medication to the list maintained by this MedicationManager
    //        The caller of this routine is responsible for that.
    //        This is only a translation utility
    //WARNING As the app is not multi-threaded, this routine is not synchronized.
    //        If the app becomes multi-threaded, this routine must be made thread safe
    //WARNING The cursor is NOT closed by this routine. It assumes the caller will close the
    //         cursor when it is done with it
    MMMedication getMedicationFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        //filled with defaults, no ID is assigned
        MMMedication medication = new MMMedication(MMUtilities.ID_DOES_NOT_EXIST);

        cursor.moveToPosition(position);
        medication.setMedicationID(cursor.getLong
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_ID)));
        medication.setForPersonID(cursor.getLong
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_FOR_PERSON_ID)));
        medication.setBrandName(cursor.getString
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_BRAND_NAME)));
        medication.setGenericName(cursor.getString
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_GENERIC_NAME)));
        medication.setMedicationNickname(cursor.getString
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_NICK_NAME)));
        medication.setDoseStrategy(cursor.getInt
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_DOSE_STRATEGY)));
        medication.setDoseAmount(cursor.getInt
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_DOSE_AMOUNT)));
        medication.setDoseUnits(cursor.getString
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_DOSE_UNITS)));
        medication.setDoseNumPerDay(cursor.getInt
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_DOSE_NUM_PER_DAY)));
        medication.setNotes(cursor.getString
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_NOTES)));
        medication.setSideEffects(cursor.getString
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_SIDE_EFFECTS)));

        boolean booleanValue = false;
        int currentlyTaken = (cursor.getInt
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_CURRENTLY_TAKEN)));
        if (currentlyTaken == 1)booleanValue = true;
        medication.setCurrentlyTaken(booleanValue);

        return medication;
    }



}
