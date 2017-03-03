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

    //This routine is called from the UI fragment and adds to memory then to the DB
    public boolean add(MMPerson newPerson){

        //we may need to read the persons in from the DB
        if ((mPersonList == null) || (mPersonList.size() == 0)){
            getPersonList();
        }

        //add first attempts to add. If that fails, it attempts an update
        return addPerson (newPerson, true);

    }//end public add()


    //The routine that actually adds the instance to in memory list and
    // potentially (third boolean parameter) to the DB
    private boolean addPerson(MMPerson newPerson, boolean addToDBToo){
        boolean returnCode = true;
        returnCode = mPersonList.add(newPerson);

        if (returnCode && addToDBToo){

            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            returnCode = databaseManager.addPerson(newPerson);

            //we need to put this check in as getMedications() will read from the DB if
            //there are no medications listed locally.
            // So to stop the loop of reading from DB just to write to the DB, add this check
            if (newPerson.isMedicationsChanged()) {
                ArrayList<MMMedication> medications = newPerson.getMedications();
                if ((medications != null) && (returnCode = true)) {
                    int position = 0;
                    int last = medications.size();
                    while (position < last) {
                        returnCode = databaseManager.addMedication(medications.get(position));
                        //// TODO: 1/25/2017 unfortunately if false, the DB is now corrupted
                        if (returnCode = false) return false;
                        position++;
                    }

                }
            }
        }
        return returnCode;
    }



    //******************  READ *******************************************
    //return the cursor containing all the Concurrent Doses in the DB
    //that pertain to this personID
    public Cursor getAllPersonsCursor (){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllPersonsCursor();
    }


    //Return the list of all Persons
    public ArrayList<MMPerson> getPersonList() {
        //Assumption is that if any person is already in the list, it must be up to date
        if ((mPersonList == null) || (mPersonList.size() == 0)){
            //get the Persons from the DB
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            mPersonList = databaseManager.getAllPersons();
        }
        return mPersonList;
    }


    //Return the person instance that matches the argument personID
    //returns null if the person is not in the list or in the DB
    public MMPerson getPerson(int personID)  {
        //Assumption is that if any person is already in the list, it must be up to date
        if ((mPersonList == null) || (mPersonList.size() == 0)){
            getPersonList();
        }
        int atPosition = getPersonPosition(personID);

        if (atPosition == PERSON_NOT_FOUND) {

            //attempt to read the DB before giving up
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            MMPerson person = databaseManager.getPerson(personID);
            if (person != null) {
                //if a matching person was in the DB, add it to RAM
                mPersonList.add(person);
                //Do not do the cascading get for the medications here

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

    public boolean removePersonFromDB(int personID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        long returnCode = databaseManager.removePerson(personID);
        if (returnCode == MMDatabaseManager.sDB_ERROR_CODE)return false;
        return true;
    }





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

    }

    //returns the ContentValues object needed to add/update the person to/in the DB
    public ContentValues getCVFromPerson(MMPerson person){
        ContentValues values = new ContentValues();
        values.put(MMDataBaseSqlHelper.PERSON_ID,       person.getPersonID());
        values.put(MMDataBaseSqlHelper.PERSON_NICKNAME, person.getNickname().toString());
        values.put(MMDataBaseSqlHelper.PERSON_EMAIL,    person.getEmailAddress().toString());
        values.put(MMDataBaseSqlHelper.PERSON_TEXT,     person.getTextAddress().toString());

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

        MMPerson person = new MMPerson(0); //filled with defaults, no ID assigned

        cursor.moveToPosition(position);
        person.setPersonID
                (cursor.getInt   (cursor.getColumnIndex(MMDataBaseSqlHelper.PERSON_ID)));
        person.setNickname
                (cursor.getString(cursor.getColumnIndex(MMDataBaseSqlHelper.PERSON_NICKNAME)));
        person.setEmailAddress
                (cursor.getString(cursor.getColumnIndex(MMDataBaseSqlHelper.PERSON_EMAIL)));
        person.setTextAddress
                (cursor.getString(cursor.getColumnIndex(MMDataBaseSqlHelper.PERSON_TEXT)));

        return person;
    }

}
