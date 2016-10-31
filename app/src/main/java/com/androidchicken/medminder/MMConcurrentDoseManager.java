package com.androidchicken.medminder;

import android.content.ContentValues;

import java.util.ArrayList;

import static com.androidchicken.medminder.MMPersonManager.PERSON_NOT_FOUND;

/**
 * Created by elisabethhuhn on 10/17/2016.
 *
 * The class in charge of maintaining the set of instances of Concurrent Doses
 *  both in-memory and in the DB
 */

public class MMConcurrentDoseManager {
    /************************************/
    /********* Static Constants *********/
    /************************************/
    public static final int DOSE_NOT_FOUND = -1;


    /************************************/
    /********* Static Variables *********/
    /************************************/
    private static MMConcurrentDoseManager ourInstance ;

    /************************************/
    /********* Member Variables *********/
    /************************************/
    private ArrayList<MMConcurrentDoses> mConcurrentDosesList;


    /************************************/
    /********* Static Methods   *********/
    /************************************/
    public static MMConcurrentDoseManager getInstance() {
        if (ourInstance == null){
            ourInstance = new MMConcurrentDoseManager();
        }
        return ourInstance;
    }


    /************************************/
    /********* Constructors     *********/
    /************************************/
    private MMConcurrentDoseManager() {

        mConcurrentDosesList = new ArrayList<>();

        //This is where we would read the list from the database
        // TODO: 10/17/2016   get the list of projects from the DB

        // TODO: 10/17/2016 create dummy concurrentDose data if none exists
        //but for now, just make up some data
    }

    /************************************/
    /********* Setters/Getters  *********/
    /************************************/


    /*******************************************/
    /********* Public Member Methods   *********/
    /*******************************************/

    //This routine not only adds to the in memory list, but also to the DB
    public void add(MMConcurrentDoses newConcurrentDose){
        //determine if already in list
        if (mConcurrentDosesList == null){
            mConcurrentDosesList = new ArrayList<>();
        }

        int position = 0;

        //determine whether the concurrentDose already exists
        position = findConcurrentDosePosition(newConcurrentDose.getConcurrentDoseID());
        if (position == DOSE_NOT_FOUND) {
            addConcurrentDose (newConcurrentDose);
        } else {
            updateConcurrentDose (newConcurrentDose, position);
        }
    }//end public add()


    //This routine not only replaces in the in memory list, but also in the DB
    public void update(MMConcurrentDoses concurrentDose){
        //The update functionality already exists in add
        //    as a ConcurrentDose can only appear once
        add(concurrentDose);
    }//end public add()

    //This routine not only removes from the in-memory list, but also from the DB
    public void removeConcurrentDose(int position) {
        if (!(position > mConcurrentDosesList.size())) {
            //Can't remove a position that the list isn't long enough for
            mConcurrentDosesList.remove(position);
        }
    }//end remove


    //find the concurrentDose instance that matches the argument concurrentDoseEmailAddr
    //returns null if the concurrentDose is not in the list
    public MMConcurrentDoses findConcurrentDose(int concurrentDoseID){
        int      atPosition = findConcurrentDosePosition(concurrentDoseID);

        if (atPosition == DOSE_NOT_FOUND) return null;
        return (mConcurrentDosesList.get(atPosition));
    }

    /********************************************/
    /********* Private Member Methods   *********/
    /********************************************/

    //Find the position of the concurrentDose instance that matches the argument concurrentDoseEmailAddr
    //returns constant = PERSON_NOT_FOUND if the concurrentDose is not in the list
    //NOTE it is a RunTimeException to call this routine if the list is null or empty
    private int findConcurrentDosePosition(int concurrentDoseID){
        MMConcurrentDoses concurrentDose;
        int position        = 0;
        int last            = mConcurrentDosesList.size();

        //Determine whether an instance of the concurrentDose is already in the list
        //NOTE that if list is empty, while doesn't loop even once
        while (position < last){
            concurrentDose = mConcurrentDosesList.get(position);

            if (concurrentDose.getConcurrentDoseID() == concurrentDoseID){
                //Found the concurrentDose in the list at this position
                return position;
            }
            position++;
        }
        return PERSON_NOT_FOUND;
    }



    //The routine that actually adds the instance both to in memory list and to DB
    private void addConcurrentDose(MMConcurrentDoses newConcurrentDose){
        mConcurrentDosesList.add(newConcurrentDose);
        // TODO: 10/18/2016 add the concurrentDose to the DB
    }



    private void updateConcurrentDose(MMConcurrentDoses newConcurrentDose, int atPosition){
        MMConcurrentDoses listConcurrentDose = mConcurrentDosesList.get(atPosition);

        //update the list instance with the attributes from the new concurrentDose being added
        listConcurrentDose.setForPerson (newConcurrentDose.getForPerson());
        listConcurrentDose.setStartOfDay(newConcurrentDose.isStartOfDay());
        listConcurrentDose.setStartTime (newConcurrentDose.getStartTime());
        listConcurrentDose.setDoses     (newConcurrentDose.getDoses());

        // TODO: 10/18/2016 update the concurrentDose already in the DB
    }

    /********************************************/
    /********* Public Member Methods    *********/
    /********************************************/


    public ContentValues getConcurrentDoseCV(MMConcurrentDoses concurrentDose){
        ContentValues values = new ContentValues();
        values.put(MMSqliteOpenHelper.CONCURRENT_DOSE_ID,             concurrentDose.getConcurrentDoseID());
        values.put(MMSqliteOpenHelper.CONCURRENT_DOSE_FOR_PERSON_ID,  concurrentDose.getForPerson());
        values.put(MMSqliteOpenHelper.CONCURRENT_DOSE_IS_START_OF_DAY,concurrentDose.isStartOfDay());
        values.put(MMSqliteOpenHelper.CONCURRENT_DOSE_START_TIME,     concurrentDose.getStartTime());

        return values;
    }

}
