package com.androidchicken.medminder;

import java.util.Comparator;

/**
 * Created by Elisabeth Huhn on 2/14/17.
 *
 * This class represents when a medication should be taken
 */

public class MMScheduleMedication {
    /*************************************/
    /*    Static (class) Constants       */
    /*************************************/

    public static final int MAX_TIME_DUE = 24 * 60;
    public static final int TIME_AS_NEEDED = MAX_TIME_DUE + 1;

    /*************************************/
    /*    Static (class) Variables       */
    /*************************************/


    /*************************************/
    /*    Member (instance) Variables    */
    /*************************************/
    private long mSchedMedID;
    private long mOfMedicationID;
    private long mForPersonID;
    private int  mTimeDue;  //number of minutes from midnight in GMT time zone
    private int  mStrategy; //whether as needed or scheduled



    /*************************************/
    /*         Static Methods            */
    /*************************************/


    /*************************************/
    /*         CONSTRUCTOR               */
    /*************************************/
    public MMScheduleMedication() {
        mSchedMedID     = MMUtilities.ID_DOES_NOT_EXIST;
        mOfMedicationID = 0;
        mForPersonID    = 0;
        mTimeDue        = 0;
        mStrategy       = MMMedication.sSET_SCHEDULE_FOR_MEDICATION;
    }

    public MMScheduleMedication(long  ofMedicationID,
                                long  forPersonID,
                                int   timeDue,
                                int   strategy) {
        mSchedMedID     = MMUtilities.ID_DOES_NOT_EXIST;
        mOfMedicationID = ofMedicationID;
        mForPersonID    = forPersonID;
        mTimeDue        = timeDue;
        mStrategy       = strategy;
    }

    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/

    public long getSchedMedID()              {return mSchedMedID;  }
    public void setSchedMedID(long schedMedID){ mSchedMedID = schedMedID;}

    public long getOfMedicationID()                   {   return mOfMedicationID;   }
    public void setOfMedicationID(long ofMedicationID) { mOfMedicationID = ofMedicationID; }

    public long getForPersonID()                {  return mForPersonID;    }
    public void setForPersonID(long forPersonID) {  mForPersonID = forPersonID;    }

    public int  getTimeDue()            { return mTimeDue; }
    public void setTimeDue(int timeDue) {  mTimeDue = timeDue; }

    public int  getStrategy()            { return mStrategy; }
    public void setStrategy(int strategy) {  mStrategy = strategy; }


    /*************************************/
    /*          Member Methods           */
    /*************************************/

    public String cdfHeaders(){
        return  "SchedMedID, "       +
                "MedicationID, "     +
                "PersonID, "         +
                "TimeDue, "          +
                "Strategy "          +

                System.getProperty("line.separator");

    }

    //Convert point to comma delimited file for exchange
    public String convertToCDF() {
        return String.valueOf(this.getSchedMedID())      + ", " +
               String.valueOf(this.getOfMedicationID())  + ", " +
               String.valueOf(this.getForPersonID())     + ", " +
               String.valueOf(this.getTimeDue())         + ", " +
               String.valueOf(this.getStrategy())        +
               System.getProperty("line.separator");
    }

    public String toString(MMMainActivity activity){
        //convert to milliseconds
        long timeDue = getTimeDue() * 60 * 1000;

        String clockTime = MMUtilities.getInstance().getTimeString(activity, timeDue);

        MMPerson person = MMPersonManager.getInstance().getPerson(mForPersonID);

        MMMedication medication =
                MMMedicationManager.getInstance().getMedicationFromID(mOfMedicationID);

        CharSequence msg = MMMedicationFragment.SCHEDULE_STRATEGY;
        if (mStrategy == MMMedication.sAS_NEEDED) msg = MMMedicationFragment.AS_NEEDED_STRATEGY;

        return
            System.getProperty("line.separator") +
            "SCHEDULE FOR MEDICATION:"    + System.getProperty("line.separator") +
            "SchedMedID:   " + String.valueOf(mForPersonID)      + System.getProperty("line.separator") +
            "MedicationID: " + medication.getMedicationNickname()+ System.getProperty("line.separator") +
            "PersonID:     " + person.getNickname()              + System.getProperty("line.separator") +
            "TimeDue:      " + clockTime                         + System.getProperty("line.separator") +
            "Strategy:     " + msg.toString()                    + System.getProperty("line.separator") ;
    }

    public String shortString(MMMainActivity activity){
        //convert to milliseconds
        long timeDue = getTimeDue() * 60 * 1000;

        String clockTime = MMUtilities.getInstance().getTimeString(activity, timeDue);

        return clockTime   ;
    }


    /**********************************************/
    /********* Comparable based on timeDue   ******/
    /**********************************************/

    /* USAGE of the Comparator:
    //now sort the Array by timeDue
    Collections.sort(schedules, new MMScheduleMedication.MMScheduleTimeDueComparator());
 */


    static class MMScheduleTimeDueComparator implements Comparator<MMScheduleMedication> {
        public int compare(MMScheduleMedication schedule1, MMScheduleMedication schedule2) {
            return schedule1.getTimeDue() - schedule2.getTimeDue();
        }
    }




}
