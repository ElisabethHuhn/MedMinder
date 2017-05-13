package com.androidchicken.medminder;

import java.util.ArrayList;

/**
 * Created by elisabethhuhn on 10/14/2016.
 * Represents a row on the Doses Taken screen.
 * A way of Aggregating history of doses into clock durations,
 * for example: all doses taken within a given quarter hour
 */

public class MMConcurrentDose {
    /*************************************/
    /*    Static (class) Constants       */
    /*************************************/


    /*************************************/
    /*    Static (class) Variables       */
    /*************************************/

    private long              mConcurrentDoseID;
    private long              mForPerson;
    private long              mStartTime; //Time of dose in milliseconds since 1970
    private ArrayList<MMDose> mDoses;


    /*************************************/
    /*    Member (instance) Variables    */
    /*************************************/



    /*************************************/
    /*         Static Methods            */
    /*************************************/


    /*************************************/
    /*         CONSTRUCTOR               */
    /*************************************/
    public MMConcurrentDose() {
        initializeVariables();
    }

    public MMConcurrentDose(long forPerson,  long startTime) {
        mConcurrentDoseID = MMUtilities.ID_DOES_NOT_EXIST;
        mForPerson    = forPerson;
        mStartTime    = startTime;
        mDoses        = new ArrayList<>();
    }

    public MMConcurrentDose(long tempID){
        initializeVariables();
        mConcurrentDoseID = tempID;
    }

    private void initializeVariables(){
        mConcurrentDoseID = MMUtilities.ID_DOES_NOT_EXIST;
        mForPerson    = 0;
        mStartTime    = 0;
        mDoses        = new ArrayList<>();
    }

    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/
    public long getConcurrentDoseID() { return mConcurrentDoseID;  }
    public void setConcurrentDoseID(long concurrentDoseID){mConcurrentDoseID = concurrentDoseID;}

    public long getForPerson()              { return mForPerson;  }
    public void setForPerson(long forPerson) {  mForPerson = forPerson;   }

    public long getStartTime()              { return mStartTime;  }
    public void setStartTime(long startTime) { mStartTime = startTime; }

    public ArrayList<MMDose> getDoses()                        {
        if (!isDosesChanged()) {
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            mDoses = databaseManager.getAllDoses(mConcurrentDoseID);
        }
        return mDoses;
    }
    public void              setDoses(ArrayList<MMDose> doses) { mDoses = doses; }
    public boolean isDosesChanged(){
        if ((mDoses == null) || (mDoses.size() == 0))return false;
        return true;
    }

    /*************************************/
    /*          Member Methods           */
    /*************************************/

    public String cdfHeaders(){
        //Header from the concatenated doses object
        String msg =
                    "ConcurrentDoseID, " +
                    "PersonID, "         +
                    "Time"               ;

        //Names of the possible medications that this patient takes
        long personID = getForPerson();
        MMPersonManager personManager = MMPersonManager.getInstance();
        MMPerson person = personManager.getPerson(personID);
        ArrayList<MMMedication> medications = person.getMedications();
        int last        = medications.size();
        int position = 0;
        MMMedication medication;
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        while (position < last){
            medication = medications.get(position);
            msg = msg + ", " + medication.getMedicationNickname();
        }
        msg = msg + System.getProperty("line.separator");
        return msg;
    }

    //Convert point to comma delimited file for exchange
    public String convertToCDF() {
        //values from the concurrent dose object
        String msg =
                       String.valueOf(this.getConcurrentDoseID()) + ", " +
                       String.valueOf( this.getForPerson())       + ", " +
                               MMUtilities.getInstance().getDateTimeString(getStartTime())     ;

        //concatenate dose values

        long personID = getForPerson();
        MMPersonManager personManager = MMPersonManager.getInstance();
        MMPerson person = personManager.getPerson(personID);

        int lastDose       = mDoses.size();
        int lastMedication = person.getMedications().size();

        int uiPosition   = 0;//The field within the UI ConcDose {0,1,2,3,4,5,6,...}
        int medPosition  = 0;//position within all medications the person takes, but read from the dose e.g.{1,3,5}
        int dosePosition = 0;//the position within doses taken at this time e.g.{0,1,2,3}
        MMMedication medication;
        MMDose dose = null;
        //so in our example:
        // uiPosition 0 was not taken, so it has no dose. Put 0 in UI field
        // uiPosition 1 corresponds to medPosition 1, and is recorded in dosePosition 1
        // uiPosition 2 was not taken, so it has no dose. Put 0 in UI field
        // uiPosition 3 corresponds to medPosition 3, and is recorded in dosePosition 2
        // uiPosition 4 was not taken, so it has no dose. Put 0 in UI field
        // uiPosition 5 corresponds to medPosition 5, and is recorded in dosePosition 3
        // uiPosition 6 was not taken, so it has no dose. Put 0 in UI field
        //

        //If the person has not taken all medications at this time, then
        //   there will not be a dose record for all the medications
        //   only those that were actually taken

        while (uiPosition < lastMedication){
            if (dosePosition < mDoses.size()){
                //There are still doses recorded, find out the position of the next one
                dose = mDoses.get(dosePosition);
                medPosition = dose.getPositionWithinConcDose();
            } else {
                //There are no more doses to be taken, output zero's in the rest of the fields
                medPosition = lastMedication+1;
            }
            //print zeros in positions where no dose was taken,
            // but don't exceed the number of total medications
            // as medPosition may be used as flag (see above where it is set to last+1)
            while ((uiPosition < medPosition) && (uiPosition < lastMedication)){
                msg = msg + ", 0";
                uiPosition++;
            }

            if ((uiPosition == medPosition) && (dose != null)){
                msg = msg + ", " + String.valueOf(dose.getAmountTaken());
            }
            uiPosition++;
            dosePosition++;
            medPosition++;
        }
/*
        int last = mDoses.size();
        int position = 0;
        MMMedication medication;
        MMDose dose;
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        while (position < last){
            dose = mDoses.get(position);
            medication = medicationManager.getMedicationFromID(dose.getForPersonID(), dose.getOfMedicationID());
            msg = msg + ", " + medication.get
        }
 */
        msg = msg + System.getProperty("line.separator");

        return msg;
    }


}
