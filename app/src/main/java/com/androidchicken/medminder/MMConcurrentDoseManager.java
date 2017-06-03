package com.androidchicken.medminder;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

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
    private ArrayList<MMConcurrentDose> mConcurrentDosesList;


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

    }

    /************************************/
    /********* Setters/Getters  *********/
    /************************************/


    /*******************************************/
    /*********     CRUD Methods        *********/
    /*******************************************/

    //This routine not only adds from the UI. Adds the in memory objects to this managers list,
    // AND adds it to the DB
    public long add(MMConcurrentDose newConcurrentDose){
        long returnCode = MMDatabaseManager.sDB_ERROR_CODE;
        //determine if already in list
        if (mConcurrentDosesList == null){
            mConcurrentDosesList = new ArrayList<>();
        }

        int position = 0;

        //update or insert the concurrentDose
        boolean addToDBToo = true;
        returnCode = addConcurrentDose (newConcurrentDose,addToDBToo);
        return returnCode;

    }//end public add()


    //return the cursor containing all the Concurrent Doses in the DB
    //that pertain to this personID
    public Cursor getAllConcurrentDosesCursor (long personID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        String orderClause = MMDataBaseSqlHelper.CONCURRENT_DOSE_TIME;
        return databaseManager.getAllConcurrentDosesCursor(personID, orderClause);
    }

    public Cursor getAllConcurrentDosesCursor (long personID, long earliestDate){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        String orderClause = MMDataBaseSqlHelper.CONCURRENT_DOSE_TIME;
        return databaseManager.getAllConcurrentDosesCursor(personID, earliestDate, orderClause);
    }

    public Cursor getAllConcurrentDosesCursor (long personID, long earliestDate, long latestDate){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        String orderClause = MMDataBaseSqlHelper.CONCURRENT_DOSE_TIME;
        return databaseManager.getAllConcurrentDosesCursor(personID, earliestDate, latestDate, orderClause);
    }

    public boolean removeConcurrentDoesFromDB(long concurrentDoseID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        long returnCode = databaseManager.removeConcurrentDose(concurrentDoseID);
        if (returnCode == MMDatabaseManager.sDB_ERROR_CODE)return false;
        return true;
    }




    /********************************************/
    /********* Private Member Methods   *********/
    /********************************************/



    //The routine that actually adds the instance both to in memory list and to DB
    public long addConcurrentDose(MMConcurrentDose newConcurrentDose, boolean addToDBToo){
        long returnCode = MMDatabaseManager.sDB_ERROR_CODE;
        boolean listReturnCode = mConcurrentDosesList.add(newConcurrentDose);
        if (!listReturnCode)return returnCode;

        if (addToDBToo){
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            returnCode = databaseManager.addConcurrentDose(newConcurrentDose);
            if (returnCode == MMDatabaseManager.sDB_ERROR_CODE)return returnCode;

            //The ID is only set when the ConcurrentDose is saved in the DB
            //This new ID must also be set in the doses being saved now
            //This is done in the loop below
            long newConcurrentDoseID = newConcurrentDose.getConcurrentDoseID();

            ArrayList<MMDose> doses = newConcurrentDose.getDoses();
            MMDose dose;
            long concurrentDoseID;
            if (doses != null) {
                int position = 0;
                int last = doses.size();
                while (position < last) {
                    dose = doses.get(position);
                    if (dose != null) {
                        //set the ID of the newly saved ConcurrentDose in the current individual dose
                        dose.setContainedInConcurrentDosesID(newConcurrentDoseID);
                        returnCode = databaseManager.addDose(dose);
                        if (returnCode == MMDatabaseManager.sDB_ERROR_CODE) return returnCode;
                    }
                    position++;
                }
            }
        }
        return returnCode;
    }



    /********************************************/
    /********* Public Member Methods    *********/
    /********************************************/

    public MMConcurrentDose getDosesForCDFromDB(MMConcurrentDose concurrentDose){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        ArrayList<MMDose> doses = databaseManager.getAllDoses(concurrentDose.getConcurrentDoseID());
        concurrentDose.setDoses(doses);
        return concurrentDose;
    }

    public ContentValues getCVFromConcurrentDose(MMConcurrentDose concurrentDose){
        ContentValues values = new ContentValues();
        values.put(MMDataBaseSqlHelper.CONCURRENT_DOSE_ID,             concurrentDose.getConcurrentDoseID());
        values.put(MMDataBaseSqlHelper.CONCURRENT_DOSE_FOR_PERSON_ID,  concurrentDose.getForPerson());
        values.put(MMDataBaseSqlHelper.CONCURRENT_DOSE_TIME,     concurrentDose.getStartTime());

        return values;
    }

    //returns the ConcurrentDoses characterized by the position within the Cursor
    //returns null if the position is larger than the size of the Cursor
    //NOTE    this routine does NOT add the ConcurrentDose to the list maintained by this ConcurrentDoseManager
    //        The caller of this routine is responsible for that.
    //        This is only a translation utility
    //WARNING As the app is not multi-threaded, this routine is not synchronized.
    //        If the app becomes multi-threaded, this routine must be made thread safe
    //WARNING The cursor is NOT closed by this routine. It assumes the caller will close the
    //         cursor when it is done with it
    public MMConcurrentDose getConcurrentDoseFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        //filled with defaults, ID not assigned
        MMConcurrentDose concurrentDoses = new MMConcurrentDose(MMUtilities.ID_DOES_NOT_EXIST);

        cursor.moveToPosition(position);
        String tempIndexString = MMDataBaseSqlHelper.CONCURRENT_DOSE_ID;
        int tempColumnIndex = cursor.getColumnIndex(tempIndexString);
        long tempID = cursor.getLong(tempColumnIndex);
        concurrentDoses.setConcurrentDoseID(tempID);

        concurrentDoses.setForPerson
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.CONCURRENT_DOSE_FOR_PERSON_ID)));

        concurrentDoses.setStartTime
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.CONCURRENT_DOSE_TIME)));

        concurrentDoses.setDoses(new ArrayList<MMDose>());

        return concurrentDoses;
    }

}
