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

    public static final int MEDICATION_NOT_FOUND = -1;


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

    //A medication that is from the DB needs to be incorporated into memory version
    public boolean addFromDB(MMMedication medication){
        if (medication == null) return false;

        int personID = medication.getForPersonID();
        if (personID == 0) return false; //there is no person

        //We have a medication from the DB, now add it to it's person
        MMPersonManager personManager = MMPersonManager.getInstance();
        MMPerson person = personManager.getPerson(personID);

        //check if the medication already exists on the person
        //    private int findMedicationPosition(ArrayList<MMMedication> medicationList, int medicationID)
        ArrayList<MMMedication> medications = person.getMedications();
        if (medications == null) {
            medications = new ArrayList<>();
            person.setMedications(medications);
        }

        int position = findMedicationPosition(medications, medication.getMedicationID());
        if (position == MEDICATION_NOT_FOUND) {
            //So the medication was in the DB, but not in memory
            //just add it to the person array
            medications.add(medication);
        } else {
            //It existed in memory AND in the db
            /*
            // TODO: 11/3/2016 Check that the assumption that the db version is more up to date is valid
            MMMedication toMedication = medications.get(position);
            if (toMedication == null) return false; //This shouldn't happen, we just checked it
            copyMedicationAttributes(medication, toMedication, true );//copy the ID as well
            */
            // TODO: 11/4/2016 What we really need to do is throw an exception!!! 
            String message = "Database is corrupt! In memory different from DB. Person = "+ 
                              person.getNickname() +
                             " for medication "+medication.getMedicationNickname();

            throw new RuntimeException(message);
            // TODO: 11/4/2016 But should the exception be a fatal one????
        }
        return true;

    }

    //This routine not only adds to the in memory list,
    // but has an argument, that if true,  also adds to the DB
    //returns FALSE if for any reason the medication can not be added
    //Use this is you already have the Person object in hand
    public boolean addToPerson(MMPerson person, MMMedication newMedication, boolean addToDB){
        //  Can not add a medication to a person who does not exist
        if ((person == null) || (newMedication == null)) return false;

        //Find the person the medication is for
        int medPersonID = newMedication.getForPersonID();
        int personID    = person.getPersonID();
        //The medication and the person must point at each other
        if (medPersonID != personID) return false;

        //determine if the medication already is associated with this person
        ArrayList<MMMedication> medicationList = person.getMedications();

        //Assert that the person has a medications list
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
            if (addToDB) {
                //  Add the medication to the DB
                databaseManager.addMedication(newMedication);
            }
        } else { //The medication does exist, Update it
            MMMedication listMedication = medicationList.get(atPosition);

            //update the list instance with the attributes from the new medication being added
            //copy the medication attributes, but not the ID
            copyMedicationAttributes(newMedication, listMedication, false);

            if (addToDB) {
                //Update the medication in the DB
                databaseManager.updateMedication(newMedication);
            }
        }

        return true;
    }


    //***********************  READ **************************************

    public int getMedicationsFromDB(MMPerson person){
        int personID = person.getPersonID();

        //get all medications in the DB that are linked to this Person
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllMedications(personID);
    }

    //***********************  UPDATE **************************************

    //This routine not only replaces in the in memory list, but also in the DB
    public void update(MMPerson person, MMMedication medication){
        //The update functionality already exists in add
        //    as a Medication can only appear once
        //The third parameter indicates whether to affect DB
        addToPerson(person, medication, true);
    }//end public add()


    //***********************  DELETE **************************************

    //Because the list is on one person instance, we must also have the person ID
    //of the instance being manipulated
    //This routine not only removes from the in-memory list, but also from the DB
    public boolean removeMedication(int personID, int position) {
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
            // TODO: 11/5/2016 removePoint returns int of the # removed. May want to adjust this returnCode based on it
            databaseManager.removeMedication(medication.getMedicationID(), medication.getForPersonID());
        }
        return returnCode;
    }//end public remove position




    /********************************************/
    /********* Private Member Methods   *********/
    /********************************************/

    //Find the position of the medication instance
    //     that matches the argument medicationID
    //     within the argument list medicationList
    //returns constant = MEDICATION_NOT_FOUND if the medication is not in the list
    //NOTE it is a RunTimeException to call this routine if the list is null or empty
    private int findMedicationPosition(ArrayList<MMMedication> medicationList, int medicationID){
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

    public void copyMedicationAttributes(MMMedication fromMedication,
                                         MMMedication toMedication,
                                         boolean copyID) {


        if (copyID) {
            toMedication.setMedicationID(fromMedication.getMedicationID());
        }
        toMedication.setBrandName(fromMedication.getBrandName());
        toMedication.setGenericName(fromMedication.getGenericName());
        toMedication.setMedicationNickname(fromMedication.getMedicationNickname());
        toMedication.setOrder(fromMedication.getOrder());
        toMedication.setDoseAmount(fromMedication.getDoseAmount());
        toMedication.setDoseUnits(fromMedication.getDoseUnits());
        toMedication.setWhenDue(fromMedication.getWhenDue());
        toMedication.setNum(fromMedication.getNum());

    }


    /********************************************/
    /****  Translation utility Methods   ********/
    /********************************************/
    public ContentValues getMedicationCV(MMMedication medication){
        ContentValues values = new ContentValues();
        values.put(MMSqliteOpenHelper.MEDICATION_ID,            medication.getMedicationID());
        values.put(MMSqliteOpenHelper.MEDICATION_FOR_PERSON_ID, medication.getForPersonID());
        values.put(MMSqliteOpenHelper.MEDICATION_BRAND_NAME,    medication.getBrandName().toString());
        values.put(MMSqliteOpenHelper.MEDICATION_GENERIC_NAME,  medication.getGenericName().toString());
        values.put(MMSqliteOpenHelper.MEDICATION_NICK_NAME,     medication.getMedicationNickname().toString());
        values.put(MMSqliteOpenHelper.MEDICATION_ORDER,         medication.getOrder());
        values.put(MMSqliteOpenHelper.MEDICATION_DOSE_AMOUNT,   medication.getDoseAmount());
        values.put(MMSqliteOpenHelper.MEDICATION_DOSE_UNITS,    medication.getDoseUnits().toString());
        values.put(MMSqliteOpenHelper.MEDICATION_WHEN_DUE,      medication.getWhenDue().toString());
        values.put(MMSqliteOpenHelper.MEDICATION_NUMBER_PER_DAY,medication.getNum());

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

        MMMedication medication = new MMMedication(); //filled with defaults

        cursor.moveToPosition(position);
        medication.setMedicationID      (cursor.getInt   (cursor.getColumnIndex(MMSqliteOpenHelper.MEDICATION_ID)));
        medication.setForPersonID       (cursor.getInt   (cursor.getColumnIndex(MMSqliteOpenHelper.MEDICATION_FOR_PERSON_ID)));
        medication.setBrandName         (cursor.getString(cursor.getColumnIndex(MMSqliteOpenHelper.MEDICATION_BRAND_NAME)));
        medication.setGenericName       (cursor.getString(cursor.getColumnIndex(MMSqliteOpenHelper.MEDICATION_GENERIC_NAME)));
        medication.setMedicationNickname(cursor.getString(cursor.getColumnIndex(MMSqliteOpenHelper.MEDICATION_NICK_NAME)));
        medication.setOrder             (cursor.getInt   (cursor.getColumnIndex(MMSqliteOpenHelper.MEDICATION_ORDER)));
        medication.setDoseAmount        (cursor.getInt   (cursor.getColumnIndex(MMSqliteOpenHelper.MEDICATION_DOSE_AMOUNT)));
        medication.setDoseUnits         (cursor.getString(cursor.getColumnIndex(MMSqliteOpenHelper.MEDICATION_DOSE_UNITS)));
        medication.setWhenDue           (cursor.getString(cursor.getColumnIndex(MMSqliteOpenHelper.MEDICATION_WHEN_DUE)));
        medication.setNum               (cursor.getInt   (cursor.getColumnIndex(MMSqliteOpenHelper.MEDICATION_NUMBER_PER_DAY)));

        return medication;
    }



}
