package com.androidchicken.medminder;

import android.content.ContentValues;

import java.util.ArrayList;

/**
 * Created by elisabethhuhn on 10/17/2016.
 *
 * The class in charge of maintaining the set of instances of Dose
 *  both in-memory and in the DB
 */

public class MMDoseManager {
    /************************************/
    /********* Static Constants *********/
    /************************************/
    public static final int DOSE_NOT_FOUND = -1;


    /************************************/
    /********* Static Variables *********/
    /************************************/
    private static MMDoseManager ourInstance ;

    /************************************/
    /********* Member Variables *********/
    /************************************/
    private ArrayList<MMDose> mDoseList;


    /************************************/
    /********* Static Methods   *********/
    /************************************/
    public static MMDoseManager getInstance() {
        if (ourInstance == null){
            ourInstance = new MMDoseManager();
        }
        return ourInstance;
    }


    /************************************/
    /********* Constructors     *********/
    /************************************/
    private MMDoseManager() {
        //todo determine whether this should be initialized here as it lives on each ConcurrentDose instance
        mDoseList = new ArrayList<>();
    }

    /************************************/
    /********* Setters/Getters  *********/
    /************************************/

    //Return the list of all Doses
    public ArrayList<MMDose> getDoseList()                           {return mDoseList; }
    public void              setDoseList(ArrayList<MMDose> doseList) { mDoseList = doseList; }

    /*******************************************/
    /********* Public Member Methods   *********/
    /*******************************************/



    //This routine not only adds to the in memory list, but also to the DB
    public void add(MMDose newDose){
        //determine if already in list
        if (mDoseList == null){
            mDoseList = new ArrayList<>();
        }

        int position = 0;

        //determine whether the dose already exists
        position = getDosePosition(newDose.getDoseID());
        if (position == DOSE_NOT_FOUND) {
            addDose (newDose);
        } else {
            updateDose (newDose, position);
        }
    }//end public add()


    //This routine not only replaces in the in memory list, but also in the DB
    public void update(MMDose dose){
        //The update functionality already exists in add
        //    as a Dose can only appear once
        add(dose);
    }//end public add()

    //This routine not only removes from the in-memory list, but also from the DB
    public boolean removeDose(int position) {
        if (position > mDoseList.size()) {
            //Can't remove a position that the list isn't long enough for
            return false;
        }

        mDoseList.remove(position);
        return true;
    }//end public remove position




    //Return the dose instance that matches the argument doseID
    //returns null if the dose is not in the list
    public MMDose getDose(int doseID){
        int atPosition = getDosePosition(doseID);

        if (atPosition == DOSE_NOT_FOUND) return null;
        return (mDoseList.get(atPosition));
    }

    /********************************************/
    /********* Private Member Methods   *********/
    /********************************************/

    //returns the position of the dose instance that matches the argument doseEmailAddr
    //returns constant = DOSE_NOT_FOUND if the dose is not in the list
    //NOTE it is a RunTimeException to call this routine if the list is null or empty
    private int getDosePosition(int doseID){
        MMDose dose;
        int position        = 0;
        int last            = mDoseList.size();

        //Determine whether an instance of the dose is already in the list
        //NOTE that if list is empty, while doesn't loop even once
        while (position < last){
            dose = mDoseList.get(position);

            if (dose.getDoseID() == doseID){
                //Found the dose in the list at this position
                return position;
            }
            position++;
        }
        return DOSE_NOT_FOUND;
    }



    //The routine that actually adds the instance both to in memory list and to DB
    private void addDose(MMDose newDose){
        mDoseList.add(newDose);
        // TODO: 10/18/2016 add the dose to the DB
    }



    private void updateDose(MMDose newDose, int atPosition){
        MMDose listDose = mDoseList.get(atPosition);

        //update the list instance with the attributes from the new dose being added

        listDose.setOfMedicationID               (newDose.getOfMedicationID());
        listDose.setForPersonID                  (newDose.getForPersonID());
        listDose.setContainedInConcurrentDosesID (newDose.getContainedInConcurrentDosesID());
        listDose.setTimeTaken                    (newDose.getTimeTaken());
        listDose.setAmountTaken                  (newDose.getAmountTaken());
        // TODO: 10/18/2016 update the dose already in the DB
    }




    /********************************************/
    /********* Public Member Methods    *********/
    /********************************************/



    public ContentValues getDoseCV(MMDose dose){
        ContentValues values = new ContentValues();
        values.put(MMSqliteOpenHelper.DOSE_ID,                           dose.getDoseID());
        values.put(MMSqliteOpenHelper.DOSE_OF_MEDICATION_ID,             dose.getOfMedicationID());
        values.put(MMSqliteOpenHelper.DOSE_FOR_PERSON_ID,                dose.getForPersonID());
        values.put(MMSqliteOpenHelper.DOSE_CONTAINED_IN_CONCURRENT_DOSE, dose.getContainedInConcurrentDosesID());
        values.put(MMSqliteOpenHelper.DOSE_TIME_TAKEN,                   dose.getTimeTaken());
        values.put(MMSqliteOpenHelper.DOSE_AMOUNT_TAKEN,                 dose.getAmountTaken());

        return values;
    }


}
