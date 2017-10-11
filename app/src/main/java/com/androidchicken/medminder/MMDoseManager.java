package com.androidchicken.medminder;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by elisabethhuhn on 10/17/2016.
 *
 * The class in charge of maintaining the set of instances of Dose
 *  both in-memory and in the DB
 */

class MMDoseManager {
    //***********************************/
    //******** Static Constants *********/
    //***********************************/



    //***********************************/
    //******** Static Variables *********/
    //***********************************/
    private static MMDoseManager ourInstance ;

    //***********************************/
    //******** Member Variables *********/
    //***********************************/


    //***********************************/
    //******** Static Methods   *********/
    //***********************************/
    static MMDoseManager getInstance() {
        if (ourInstance == null){
            ourInstance = new MMDoseManager();
        }
        return ourInstance;
    }


    //***********************************/
    //******** Constructors     *********/
    //***********************************/
    private MMDoseManager() {
    }

    //***********************************/
    //******** Setters/Getters  *********/
    //***********************************/


    //******************************************/
    //******** Public Member Methods   *********/
    //******************************************/

    //This routine not only adds to the in memory list, but also to the DB
    void add(MMDose newDose){

        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        databaseManager.addDose(newDose);

    }//end add()

    //get the most recent time this medication was taken
    MMDose getMostRecentDose(long medicationID){
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        //get all doses for this medication, and order by time taken
        Cursor cursor = databaseManager.getAllDosesCursor(medicationID,
                                                          MMDataBaseSqlHelper.DOSE_TIME_TAKEN);
        int count = cursor.getCount();

        if (count == 0){
            cursor.close();
            return null;
        }

        //we want the most recent, so get the last one
        MMDose recentDose = getDoseFromCursor(cursor, (cursor.getCount()-1));

        cursor.close();
        return recentDose;
    }



    //*******************************************/
    //******** Private Member Methods   *********/
    //*******************************************/






    //*******************************************/
    //******** Member Methods    *********/
    //*******************************************/



    ContentValues getCVFromDose(MMDose dose){
        ContentValues values = new ContentValues();
        values.put(MMDataBaseSqlHelper.DOSE_ID,              dose.getDoseID());
        values.put(MMDataBaseSqlHelper.DOSE_OF_MEDICATION_ID,dose.getOfMedicationID());
        values.put(MMDataBaseSqlHelper.DOSE_FOR_PERSON_ID,   dose.getForPersonID());
        values.put(MMDataBaseSqlHelper.DOSE_CONTAINED_IN_CONCURRENT_DOSE,
                                                            dose.getContainedInConcurrentDosesID());
        values.put(MMDataBaseSqlHelper.DOSE_POSITION_WITHIN_CONCURRENT_DOSE,
                                                            dose.getPositionWithinConcDose());
        values.put(MMDataBaseSqlHelper.DOSE_TIME_TAKEN,      dose.getTimeTaken());
        values.put(MMDataBaseSqlHelper.DOSE_AMOUNT_TAKEN,    dose.getAmountTaken());

        return values;
    }




    //returns the Dose characterized by the position within the Cursor
    //returns null if the position is larger than the size of the Cursor
    //NOTE    this routine does NOT add the Dose to the list maintained by this DoseManager
    //        The caller of this routine is responsible for that.
    //        This is only a translation utility
    //WARNING As the app is not multi-threaded, this routine is not synchronized.
    //        If the app becomes multi-threaded, this routine must be made thread safe
    //WARNING The cursor is NOT closed by this routine. It assumes the caller will close the
    //         cursor when it is done with it
    MMDose getDoseFromCursor(Cursor cursor, int position){

        int last = cursor.getCount();
        if (position >= last) return null;

        MMDose dose = new MMDose(MMUtilities.ID_DOES_NOT_EXIST); //filled with defaults

        cursor.moveToPosition(position);
        dose.setDoseID
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.DOSE_ID)));
        dose.setForPersonID
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.DOSE_FOR_PERSON_ID)));

        dose.setOfMedicationID
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.DOSE_OF_MEDICATION_ID)));

        dose.setContainedInConcurrentDosesID
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.DOSE_CONTAINED_IN_CONCURRENT_DOSE)));

        dose.setPositionWithinConcDose
                (cursor.getInt(cursor.getColumnIndex(MMDataBaseSqlHelper.DOSE_POSITION_WITHIN_CONCURRENT_DOSE)));

        dose.setTimeTaken
                (cursor.getLong(cursor.getColumnIndex(MMDataBaseSqlHelper.DOSE_TIME_TAKEN)));

        dose.setAmountTaken
                (cursor.getInt(cursor.getColumnIndex(MMDataBaseSqlHelper.DOSE_AMOUNT_TAKEN)));

        return dose;
    }

}
