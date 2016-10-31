package com.androidchicken.medminder;

import android.content.ContentValues;

import java.util.ArrayList;

/**
 * Created by elisabethhuhn on 10/17/2016.
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

        //This is where we would read the list from the database
        // TODO: 10/17/2016   get the list of projects from the DB

        // TODO: 10/17/2016 create dummy person data if none exists
        //but for now, just make up some data
    }

    /************************************/
    /********* Setters/Getters  *********/
    /************************************/


    /*******************************************/
    /********* Public Member Methods   *********/
    /*******************************************/

    //Return the list of all Persons
    public ArrayList<MMPerson> getPersonList() {
        return mPersonList;
    }


    //This routine not only adds to the in memory list, but also to the DB
    public void add(MMPerson newPerson){
        //determine if already in list
        if (mPersonList == null){
            mPersonList = new ArrayList<>();
        }

        int position = 0;

        //determine whether the person already exists
        position = getPersonPosition(newPerson.getPersonID());
        if (position == PERSON_NOT_FOUND) {
            addPerson (newPerson);
        } else {
            updatePerson (newPerson, position);
        }
    }//end public add()


    //This routine not only replaces in the in memory list, but also in the DB
    public void update(MMPerson person){
        //The update functionality already exists in add
        //    as a Person can only appear once
        add(person);
    }//end public add()

    //This routine not only removes from the in-memory list, but also from the DB
    public boolean removePerson(int position) {
        if (position > mPersonList.size()) {
            //Can't remove a position that the list isn't long enough for
            return false;
        }

        mPersonList.remove(position);
        return true;
    }//end public remove position




    //Return the person instance that matches the argument personID
    //returns null if the person is not in the list
    public MMPerson getPerson(int personID){
        int atPosition = getPersonPosition(personID);

        if (atPosition == PERSON_NOT_FOUND) return null;
        return (mPersonList.get(atPosition));
    }

    /********************************************/
    /********* Private Member Methods   *********/
    /********************************************/

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



    //The routine that actually adds the instance both to in memory list and to DB
    private void addPerson(MMPerson newPerson){
        mPersonList.add(newPerson);
        // TODO: 10/18/2016 add the person to the DB
    }



    private void updatePerson(MMPerson newPerson, int atPosition){
        MMPerson listPerson = mPersonList.get(atPosition);

        //update the list instance with the attributes from the new person being added
        listPerson.setNickname    (newPerson.getNickname());
        listPerson.setEmailAddress(newPerson.getEmailAddress());
        listPerson.setTextAddress (newPerson.getTextAddress());
        listPerson.setDuration    (newPerson.getDuration());
        listPerson.setMedOrder    (newPerson.getMedOrder());
        // TODO: 10/18/2016 update the person already in the DB
    }


    /********************************************/
    /********* Public Member Methods    *********/
    /********************************************/

    public ContentValues getPersonCV(MMPerson person){
        ContentValues values = new ContentValues();
        values.put(MMSqliteOpenHelper.PERSON_ID,       person.getPersonID());
        values.put(MMSqliteOpenHelper.PERSON_NICKNAME, person.getNickname().toString());
        values.put(MMSqliteOpenHelper.PERSON_EMAIL,    person.getEmailAddress().toString());
        values.put(MMSqliteOpenHelper.PERSON_TEXT,     person.getNickname().toString());
        values.put(MMSqliteOpenHelper.PERSON_DURATION, person.getDuration());
        values.put(MMSqliteOpenHelper.PERSON_ORDER,    person.getMedOrder());

        return values;
    }


}
