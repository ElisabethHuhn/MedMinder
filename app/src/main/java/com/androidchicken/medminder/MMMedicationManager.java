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

public class MMMedicationManager {
    /************************************/
    /********* Static Constants  ********/
    /************************************/

    private static final int MEDICATION_NOT_FOUND = -1;


    /************************************/
    /********* Static Variables  ********/
    /************************************/
    private static MMMedicationManager ourInstance ;

    /**************************************/
    /********* Member Variables   *********/
    /**************************************/
    //The medication lists exist on the Persons, rather than on a list here
    //private ArrayList<MMMedication> mMedicationList;

    /************************************/
    /********* Static Methods   *********/
    /************************************/
    public static MMMedicationManager getInstance() {
        if (ourInstance == null){
            ourInstance = new MMMedicationManager();
        }
        return ourInstance;
    }


    /************************************/
    /********* Constructors     *********/
    /************************************/
    private MMMedicationManager() {

        //The medication list already exists on the Person instance
        //mMedicationList = new ArrayList<>();

    }

    /*******************************************/
    /********* Public Member Methods   *********/
    /*******************************************/



    /*******************************************/
    /*********     CRUD Methods        *********/
    /*******************************************/

    //***********************  CREATE **************************************

    //This routine not only adds to the in memory list,
    // but has an argument, that if true,  also adds to the DB
    //returns FALSE if for any reason the medication can not be added
    //Use this is you already have the Person object in hand
    public boolean addToPerson(MMPerson person, MMMedication newMedication, boolean addToDB){
        //  Can not add a medication to a person who does not exist
        if ((person == null) || (newMedication == null)) return false;

        //Find the person the medication is for
        long medPersonID = newMedication.getForPersonID();
        long personID    = person.getPersonID();
        //The medication and the person must point at each other
        if (medPersonID != personID) return false;

        //determine if the medication already is associated with this person
        ArrayList<MMMedication> medicationList = person.getMedications();

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


    //***********************  READ **************************************
    //return the cursor containing all the Concurrent Doses in the DB
    //that pertain to this personID
    public Cursor getAllMedicationsCursor (long personID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllMedicationsCursor(personID);
    }

    public ArrayList<MMMedication> getMedicationsFromDB(MMPerson person){
        long personID = person.getPersonID();

        //get all medications in the DB that are linked to this Person
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllMedications(personID);
    }

    public MMMedication getMedicationFromID(long medicationID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getMedication(medicationID);
    }

    //***********************  UPDATE **************************************


    //***********************  DELETE **************************************

    //Because the list is on one person instance, we must also have the person ID
    //of the instance being manipulated
    //This routine not only removes from the in-memory list, but also from the DB
    public boolean removeMedication(long personID, int position) {
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


    public boolean removeMedicationFromDB(long medicationID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        long returnCode = databaseManager.removeMedication(medicationID);
        if (returnCode == MMDatabaseManager.sDB_ERROR_CODE)return false;
        return true;
    }


    /********************************************/
    /********* Private Member Methods   *********/
    /********************************************/

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



    //***********************  COPY **************************************


    /********************************************/
    /****  Translation utility Methods   ********/
    /********************************************/
    public ContentValues getCVFromMedication(MMMedication medication){
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
    public MMMedication getMedicationFromCursor(Cursor cursor, int position){

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

        boolean booleanValue = false;
        int currentlyTaken = (cursor.getInt
                (cursor.getColumnIndex(MMDataBaseSqlHelper.MEDICATION_CURRENTLY_TAKEN)));
        if (currentlyTaken == 1)booleanValue = true;
        medication.setCurrentlyTaken(booleanValue);

        return medication;
    }



}
