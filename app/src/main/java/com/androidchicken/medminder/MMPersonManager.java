package com.androidchicken.medminder;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 10/17/2016.
 *
 * The class in charge of maintaining the set of instances of Person
 *  both in-memory and in the DB
 */

public class MMPersonManager {
    /************************************/
    /********* Static Constants *********/
    /************************************/
    public static final int PERSON_NOT_FOUND = -1;


    /************************************/
    /********* Static Variables *********/
    /************************************/
    private static MMPersonManager ourInstance ;

    /************************************/
    /********* Member Variables *********/
    /************************************/
    private ArrayList<MMPerson> mPersonList;


    /************************************/
    /********* Static Methods   *********/
    /************************************/
    public static MMPersonManager getInstance() {
        if (ourInstance == null){
            ourInstance = new MMPersonManager();
        }
        return ourInstance;
    }


    /************************************/
    /********* Constructors     *********/
    /************************************/
    private MMPersonManager() {

        mPersonList = new ArrayList<>();

        //The DB isn't read until the first time a person is accessed

    }

    /************************************/
    /********* Setters/Getters  *********/
    /************************************/


    /*******************************************/
    /********* CRUD Methods            *********/
    /*******************************************/


    //******************  CREATE *******************************************

    //This routine not only adds to the in memory list, but also to the DB
    public void add(MMPerson newPerson){

        if (mPersonList == null){
            mPersonList = new ArrayList<>();
        }

        //determine whether the person already exists in the list
        int position = getPersonPosition(newPerson.getPersonID());
        if (position == PERSON_NOT_FOUND) {
            addPerson (newPerson, true);
        } else {
            updatePerson (newPerson, position, true);
        }

    }//end public add()

    //This routine ONLY adds to in memory list. It's coming from the DB
    public void addFromDB(MMPerson newPerson){
        //determine if already in list
        if (mPersonList == null){
            mPersonList = new ArrayList<>();
        }

         //determine whether the person already exists
        int position = getPersonPosition(newPerson.getPersonID());
        if (position == PERSON_NOT_FOUND) {
            addPerson (newPerson, false);
            //Need to check if any medications exist in the DB for this person
            MMMedicationManager medicationManager = MMMedicationManager.getInstance();
            medicationManager.getMedicationsFromDB(newPerson);
        } else {
            updatePerson (newPerson, position, false);
        }
    }//end public add()


    //The routine that actually adds the instance to in memory list and
    // potentially (third boolean parameter) to the DB
    private void addPerson(MMPerson newPerson, boolean addToDBToo){
        mPersonList.add(newPerson);

        if (addToDBToo){

            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            databaseManager.addPerson(newPerson);

            //also have to deal with any Medications on the person
            //add any medications to the DB
            //The ASSUMPTION is that if it didn't exist in memory, it doesn't exist in the DB
            //This is perhaps a risky assumption.......
            // TODO: 11/2/2016 Determine if add person assumption is too risky

            ArrayList<MMMedication> medications = newPerson.getMedications();
            if (medications != null){
                for (int position = 0; position < medications.size(); position++) {
                    databaseManager.addMedication(medications.get(position));
                }

            }
        }

    }



    //******************  READ *******************************************

    //Return the list of all Persons
    public ArrayList<MMPerson> getPersonList() {
        if (mPersonList == null){
            mPersonList = new ArrayList<>();
        }
        if (mPersonList.size() == 0){
            //get the Persons from the DB
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            databaseManager.getAllPersons();
        }
        return mPersonList;
    }


    //Return the person instance that matches the argument personID
    //returns null if the person is not in the list or in the DB
    public MMPerson getPerson(int personID)  {
        int atPosition = getPersonPosition(personID);

        if (atPosition == PERSON_NOT_FOUND) {

            //attempt to read the DB before giving up
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            MMPerson person = databaseManager.getPerson(personID);
            if (person != null) {
                //if a matching person was in the DB, add it to RAM
                mPersonList.add(person);
                //and go get the medications for this person
                MMMedicationManager medicationManager = MMMedicationManager.getInstance();
                medicationManager.getMedicationsFromDB(person);
            }
            return person;
        }
        return (mPersonList.get(atPosition));
    }



    //returns the position of the person instance that matches the argument personEmailAddr
    //returns constant = PERSON_NOT_FOUND if the person is not in the list
    //NOTE it is a RunTimeException to call this routine if the list is null or empty
    private int getPersonPosition(int personID){
        MMPerson person;
        int position        = 0;
        int last            = mPersonList.size();

        //Determine whether an instance of the person is already in the list
        //NOTE that if list is empty, while doesn't loop even once
        while (position < last){
            person = mPersonList.get(position);

            if (person.getPersonID() == personID){
                //Found the person in the list at this position
                return position;
            }
            position++;
        }
        return PERSON_NOT_FOUND;
    }



    //******************  UPDATE *******************************************


    //This routine not only replaces in the in memory list, but also in the DB
    public void update(MMPerson person){
        //The update functionality already exists in add
        //    as a Person can only appear once
        add(person);
    }//end public add()


    //This routine  only replaces in the in memory list
    public void updateFromDB(MMPerson person){
        //The update functionality already exists in add
        //    as a Person can only appear once
        addFromDB(person);
    }//end public add()



    private void updatePerson(MMPerson newPerson, int atPosition, boolean addToDBToo){
        MMPerson listPerson = mPersonList.get(atPosition);

        //update the list instance with the attributes from the new person being added
        //        don't copy the ID
        copyPersonAttributes (newPerson, listPerson, false);

        if (addToDBToo) {
            // update the person already in the DB
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            databaseManager.updatePerson(newPerson);

            //also have to deal with any Medications on the person
            //add any medications to the DB
            //The ASSUMPTION is that if it didn't exist in memory, it doesn't exist in the DB
            //This is perhaps a risky assumption.......
            // TODO: 11/2/2016 Determine if add person assumption is too risky

            ArrayList<MMMedication> medications = newPerson.getMedications();
            if (medications != null){
                for (int position = 0; position < medications.size(); position++) {
                    databaseManager.updateMedication(medications.get(position));
                }

            }

        }
    }



    //******************  DELETE *******************************************


    //This routine not only removes from the in-memory list, but also from the DB
    public boolean removePerson(int position) {
        if (position > mPersonList.size()) {
            //Can't remove a position that the list isn't long enough for
            return false;
        }

        mPersonList.remove(position);
        return true;
    }//end public remove position





    /********************************************/
    /********* Private Member Methods   *********/
    /********************************************/


    /********************************************/
    /********* Translation Utility Methods  *****/
    /********************************************/

    public void copyPersonAttributes(MMPerson fromPerson, MMPerson toPerson, boolean copyID){
        if (copyID){
            toPerson.setPersonID(fromPerson.getPersonID());
        }
        toPerson.setNickname    (fromPerson.getNickname());
        toPerson.setEmailAddress(fromPerson.getEmailAddress());
        toPerson.setTextAddress (fromPerson.getTextAddress());
        toPerson.setDuration    (fromPerson.getDuration());
        toPerson.setMedOrder    (fromPerson.getMedOrder());

    }

    //returns the ContentValues object needed to add/update the person to/in the DB
    public ContentValues getCVFromPerson(MMPerson person){
        ContentValues values = new ContentValues();
        values.put(MMSqliteOpenHelper.PERSON_ID,       person.getPersonID());
        values.put(MMSqliteOpenHelper.PERSON_NICKNAME, person.getNickname().toString());
        values.put(MMSqliteOpenHelper.PERSON_EMAIL,    person.getEmailAddress().toString());
        values.put(MMSqliteOpenHelper.PERSON_TEXT,     person.getTextAddress().toString());
        values.put(MMSqliteOpenHelper.PERSON_DURATION, person.getDuration());
        values.put(MMSqliteOpenHelper.PERSON_ORDER,    person.getMedOrder());

        return values;
    }


    //returns the MMPerson characterized by the position within the Cursor
    //returns null if the position is larger than the size of the Cursor
    //NOTE    this routine does NOT add the person to the list maintained by this PersonManager
    //        The caller of this routine is responsible for that.
    //        This is only a translation utility
    //WARNING As the app is not multi-threaded, this routine is not synchronized.
    //        If the app becomes multi-threaded, this routine must be made thread safe
    //WARNING The cursor is NOT closed by this routine. It assumes the caller will close the
    //         cursor when it is done with it
    public MMPerson getPersonFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        MMPerson person = new MMPerson(); //filled with defaults

        cursor.moveToPosition(position);
        person.setPersonID    (cursor.getInt   (cursor.getColumnIndex(MMSqliteOpenHelper.PERSON_ID)));
        person.setNickname    (cursor.getString(cursor.getColumnIndex(MMSqliteOpenHelper.PERSON_NICKNAME)));
        person.setEmailAddress(cursor.getString(cursor.getColumnIndex(MMSqliteOpenHelper.PERSON_EMAIL)));
        person.setTextAddress (cursor.getString(cursor.getColumnIndex(MMSqliteOpenHelper.PERSON_TEXT)));
        person.setDuration    (cursor.getInt   (cursor.getColumnIndex(MMSqliteOpenHelper.PERSON_DURATION)));
        person.setMedOrder    (cursor.getInt   (cursor.getColumnIndex(MMSqliteOpenHelper.PERSON_ORDER)));

        return person;
    }

}
