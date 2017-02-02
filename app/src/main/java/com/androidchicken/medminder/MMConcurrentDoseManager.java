package com.androidchicken.medminder;

import android.content.ContentValues;
import android.database.Cursor;

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

        //This is where we would read the list from the database
        // TODO: 10/17/2016   get the list of projects from the DB

        // TODO: 10/17/2016 create dummy concurrentDose data if none exists
        //but for now, just make up some data
    }

    /************************************/
    /********* Setters/Getters  *********/
    /************************************/


    /*******************************************/
    /********* CRUD Methods   *********/
    /*******************************************/

    //This routine not only adds from the UI. Adds the in memory objects to this managers list,
    // AND adds it to the DB
    public boolean add(MMConcurrentDose newConcurrentDose){
        //determine if already in list
        if (mConcurrentDosesList == null){
            mConcurrentDosesList = new ArrayList<>();
        }

        int position = 0;

        //determine whether the concurrentDose already exists
        position = findConcurrentDosePosition(newConcurrentDose.getConcurrentDoseID());
        if (position == DOSE_NOT_FOUND) {
            boolean addToDBToo = true;
            return addConcurrentDose (newConcurrentDose,addToDBToo);
        } else {
            return updateConcurrentDose (newConcurrentDose, position);
        }
    }//end public add()


    //return the cursor containing all the Concurrent Doses in the DB
    //that pertain to this personID
    public Cursor getAllConcurrentDosesCursor (int personID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        return databaseManager.getAllConcurrentDosesCursor(personID);
    }

    //This routine not only replaces in the in memory list, but also in the DB
    public void update(MMConcurrentDose concurrentDose){
        //The update functionality already exists in add
        //    as a ConcurrentDose can only appear once
        add(concurrentDose);
    }//end public update()

    //This routine not only removes from the in-memory list, but also from the DB
    public void removeConcurrentDose(int position) {
        if (!(position > mConcurrentDosesList.size())) {
            //Can't remove a position that the list isn't long enough for
            mConcurrentDosesList.remove(position);
        }
    }//end remove


    //find the concurrentDose instance that matches the argument concurrentDoseEmailAddr
    //returns null if the concurrentDose is not in the list
    public MMConcurrentDose findConcurrentDose(int concurrentDoseID){
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
        MMConcurrentDose concurrentDose;
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
    private boolean addConcurrentDose(MMConcurrentDose newConcurrentDose, boolean addToDBToo){
        boolean returnCode = true;
        returnCode = mConcurrentDosesList.add(newConcurrentDose);

        if (returnCode && addToDBToo){

            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            returnCode = databaseManager.addConcurrentDose(newConcurrentDose);

            ArrayList<MMDose> doses = newConcurrentDose.getDoses();
            if ((doses != null) && (returnCode = true)){
                int position = 0;
                int last = doses.size();
                while (position < last) {
                    returnCode = databaseManager.addDose(doses.get(position));
                    //// TODO: 1/25/2017 unfortunately if false, the DB is now corrupted
                    if (returnCode = false)return false;
                    position++;
                }

            }
        }
        return returnCode;
    }



    private boolean updateConcurrentDose(MMConcurrentDose newConcurrentDose, int atPosition){
        MMConcurrentDose listConcurrentDose = mConcurrentDosesList.get(atPosition);

        //update the list instance with the attributes from the new concurrentDose being added
        listConcurrentDose.setForPerson (newConcurrentDose.getForPerson());
        listConcurrentDose.setStartOfDay(newConcurrentDose.isStartOfDay());
        listConcurrentDose.setStartTime (newConcurrentDose.getStartTime());
        listConcurrentDose.setDoses     (newConcurrentDose.getDoses());

        // TODO: 10/18/2016 update the concurrentDose already in the DB
        return true;
    }

    /********************************************/
    /********* Public Member Methods    *********/
    /********************************************/

    public MMConcurrentDose getDosesForCDFromDB(MMConcurrentDose concurrentDose){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        ArrayList<MMDose> doses = databaseManager.getDosesForCD(concurrentDose);
        concurrentDose.setDoses(doses);
        return concurrentDose;
    }

    public ContentValues getCVFromConcurrentDose(MMConcurrentDose concurrentDose){
        ContentValues values = new ContentValues();
        values.put(MMSqliteOpenHelper.CONCURRENT_DOSE_ID,             concurrentDose.getConcurrentDoseID());
        values.put(MMSqliteOpenHelper.CONCURRENT_DOSE_FOR_PERSON_ID,  concurrentDose.getForPerson());
        int startOfDay = 0; //false
        if (concurrentDose.isStartOfDay())startOfDay = 1;
        values.put(MMSqliteOpenHelper.CONCURRENT_DOSE_IS_START_OF_DAY,startOfDay);
        values.put(MMSqliteOpenHelper.CONCURRENT_DOSE_START_TIME,     concurrentDose.getStartTime());

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

        MMConcurrentDose concurrentDoses = new MMConcurrentDose(); //filled with defaults

        cursor.moveToPosition(position);
        String tempIndexString = MMSqliteOpenHelper.CONCURRENT_DOSE_ID;
        int tempColumnIndex = cursor.getColumnIndex(tempIndexString);
        int tempID = cursor.getInt(tempColumnIndex);
        concurrentDoses.setConcurrentDoseID(tempID);
        concurrentDoses.setForPerson
                (cursor.getInt(cursor.getColumnIndex(MMSqliteOpenHelper.CONCURRENT_DOSE_FOR_PERSON_ID)));

        int startOfDay =
                (cursor.getInt(cursor.getColumnIndex(MMSqliteOpenHelper.CONCURRENT_DOSE_IS_START_OF_DAY)));
        boolean isStartOfDay = true;
        if (startOfDay < 1)isStartOfDay = false;
        concurrentDoses.setStartOfDay(isStartOfDay);

        concurrentDoses.setStartTime
                (cursor.getInt(cursor.getColumnIndex(MMSqliteOpenHelper.CONCURRENT_DOSE_START_TIME)));

        concurrentDoses.setDoses(new ArrayList<MMDose>());

        return concurrentDoses;
    }

}
