package com.androidchicken.medminder;

import java.util.Comparator;

/**
 * Created by Elisabeth Huhn on 2/14/17.
 *
 * This class represents when a medication should be taken
 */

public class MMSchedule {
    // ***********************************/
    /*    Static (class) Constants       */
    // ***********************************/



    // ***********************************/
    /*    Static (class) Variables       */
    // ***********************************/


    // ***********************************/
    /*    Member (instance) Variables    */
    // ***********************************/
    private long mSchedMedID;
    private long mOfMedicationID;
    private long mForPersonID;
    private int  mTimeDue;  //number of minutes from midnight in GMT time zone
    private int  mStrategy; //whether as needed or scheduled



    // ***********************************/
    /*         Static Methods            */
    // ***********************************/


    // ***********************************/
    /*         CONSTRUCTOR               */
    // ***********************************/
    MMSchedule() {
        mSchedMedID     = MMUtilities.ID_DOES_NOT_EXIST;
        mOfMedicationID = 0;
        mForPersonID    = 0;
        mTimeDue        = 0;
        mStrategy       = MMMedication.sSET_SCHEDULE_FOR_MEDICATION;
    }

    MMSchedule(long  ofMedicationID,
                      long  forPersonID,
                      int   timeDue,
                      int   strategy) {
        mSchedMedID     = MMUtilities.ID_DOES_NOT_EXIST;
        mOfMedicationID = ofMedicationID;
        mForPersonID    = forPersonID;
        mTimeDue        = timeDue;
        mStrategy       = strategy;
    }

    // ***********************************/
    /*    Member setter/getter Methods   */
    // ***********************************/

    long getSchedMedID()              {return mSchedMedID;  }
    void setSchedMedID(long schedMedID){ mSchedMedID = schedMedID;}

    long getOfMedicationID()                   {   return mOfMedicationID;   }
    void setOfMedicationID(long ofMedicationID) { mOfMedicationID = ofMedicationID; }

    long getForPersonID()                {  return mForPersonID;    }
    void setForPersonID(long forPersonID) {  mForPersonID = forPersonID;    }

    int  getTimeDue()            { return mTimeDue; }
    void setTimeDue(int timeDue) {  mTimeDue = timeDue; }

    int  getStrategy()            { return mStrategy; }
    void setStrategy(int strategy) {  mStrategy = strategy; }


    // ***********************************/
    /*          Member Methods           */
    // ***********************************/

    String cdfHeaders(){
        return  "SchedMedID, "       +
                "MedicationID, "     +
                "PersonID, "         +
                "TimeDue, "          +
                "Strategy "          +

                System.getProperty("line.separator");

    }

    //Convert point to comma delimited file for exchange
    String convertToCDF() {
        return String.valueOf(this.getSchedMedID())      + ", " +
               String.valueOf(this.getOfMedicationID())  + ", " +
               String.valueOf(this.getForPersonID())     + ", " +
               String.valueOf(this.getTimeDue())         + ", " +
               String.valueOf(this.getStrategy())        +
               System.getProperty("line.separator");
    }

    String toString(MMMainActivity activity){
        //convert to milliseconds
        long timeDue = MMUtilitiesTime.convertMinutesToMs(getTimeDue());

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



    String getTimeDueString(MMMainActivity activity){
        int timeMinutes = getTimeDue();

        long timeMilliseconds = MMUtilitiesTime.convertMinutesToMs(timeMinutes);
        timeMilliseconds = MMUtilitiesTime.convertLocaltoGMT(timeMilliseconds);
        //set flag to time
        boolean isTimeFlag = true;
        return MMUtilitiesTime.convertTimeMStoString(activity, timeMilliseconds, isTimeFlag);

    }


    // ********************************************/
    // ******* Comparable based on timeDue   ******/
    // ********************************************/

    /* USAGE of the Comparator:
    //now sort the Array by timeDue
    Collections.sort(schedules, new MMScheduleMedication.MMScheduleTimeDueComparator());
 */


    static class MMScheduleTimeDueComparator implements Comparator<MMSchedule> {
        public int compare(MMSchedule schedule1, MMSchedule schedule2) {
            return schedule1.getTimeDue() - schedule2.getTimeDue();
        }
    }




}
