package com.androidchicken.medminder;

import java.util.ArrayList;

/**
 * Created by elisabethhuhn on 10/14/2016.
 * Represents a row on the Doses Taken screen.
 * A way of Aggregating history of doses into clock durations,
 * for example: all doses taken within a given quarter hour
 */

class MMConcurrentDose {
    //-***********************************/
    /*    Static (class) Constants       */
    //-***********************************/


    //-***********************************/
    /*    Static (class) Variables       */
    //-***********************************/

    private long              mConcurrentDoseID;
    private long              mForPerson;
    private long              mStartTime; //Time of dose in milliseconds since 1970
    private ArrayList<MMDose> mDoses;


    //-***********************************/
    /*    Member (instance) Variables    */
    //-***********************************/



    //-***********************************/
    /*         Static Methods            */
    //-***********************************/


    //-***********************************/
    /*         CONSTRUCTOR               */
    //-***********************************/


    MMConcurrentDose(long forPerson,  long startTime) {
        mConcurrentDoseID = MMUtilities.ID_DOES_NOT_EXIST;
        mForPerson    = forPerson;
        mStartTime    = startTime;
        mDoses        = new ArrayList<>();
    }

    MMConcurrentDose(long tempID){
        initializeVariables();
        mConcurrentDoseID = tempID;
    }

    private void initializeVariables(){
        mConcurrentDoseID = MMUtilities.ID_DOES_NOT_EXIST;
        mForPerson    = 0;
        mStartTime    = 0;
        mDoses        = new ArrayList<>();
    }

    //-***********************************/
    /*    Member setter/getter Methods   */
    //-***********************************/
    long getConcurrentDoseID() { return mConcurrentDoseID;  }
    void setConcurrentDoseID(long concurrentDoseID){mConcurrentDoseID = concurrentDoseID;}

    long getForPerson()              { return mForPerson;  }
    void setForPerson(long forPerson) {  mForPerson = forPerson;   }

    long getStartTime()              { return mStartTime;  }
    void setStartTime(long startTime) { mStartTime = startTime; }


    ArrayList<MMDose> getDoses()                        {
        if (!isDosesChanged()) {
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            mDoses = databaseManager.getAllDoses(mConcurrentDoseID);
        }
        return mDoses;
    }
    void              setDoses(ArrayList<MMDose> doses) { mDoses = doses; }
    private boolean isDosesChanged(){
        if ((mDoses == null) || (mDoses.size() == 0))return false;
        return true;
    }
    //The doses have changed, force next read to pull doses from the DB
    void setDosesChanged(){
        mDoses = null;
    }

    //-***********************************/
    /*          Member Methods           */
    //-************************************/

    String cdfHeaders(MMMainActivity activity){
        //Header from the concatenated doses object
        StringBuilder msg = new StringBuilder();
        msg.append(
                    "ConcurrentDoseID, " +
                    "PersonID, "         +
                    "Time"               );

        //Names of the possible medications that this patient takes
        long personID = getForPerson();
        MMPersonManager personManager = MMPersonManager.getInstance();
        MMPerson person = personManager.getPerson(personID);
        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
        ArrayList<MMMedication> medications = person.getMedications(currentOnly);
        int last        = medications.size();
        int position = 0;
        MMMedication medication;
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        while (position < last){
            medication = medications.get(position);
            msg.append(", ");
            msg.append( medication.getMedicationNickname());
        }
        msg.append( System.getProperty("line.separator"));
        return msg.toString();
    }

    //Convert point to comma delimited file for exchange
    String convertToCDF(MMMainActivity activity) {
        //values from the concurrent dose object
        StringBuilder msg = new StringBuilder();
        msg.append(String.valueOf(this.getConcurrentDoseID()));
        msg.append(", ");
        msg.append(String.valueOf( this.getForPerson())) ;
        msg.append(", ");
        msg.append(MMUtilities.getInstance().getDateTimeString(getStartTime()) )    ;

        //concatenate dose values

        long personID = getForPerson();
        MMPersonManager personManager = MMPersonManager.getInstance();
        MMPerson person = personManager.getPerson(personID);

        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
        int lastMedication = person.getMedications(currentOnly).size();

        int uiPosition   = 0;//The field within the UI ConcDose {0,1,2,3,4,5,6,...}
        int medPosition  = 0;//position within all medications the person takes, but read from the dose e.g.{1,3,5}
        int dosePosition = 0;//the position within doses taken at this time e.g.{0,1,2,3}
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
                msg.append(", 0");
                uiPosition++;
            }

            if ((uiPosition == medPosition) && (dose != null)){
                msg.append(", " );
                msg.append(String.valueOf(dose.getAmountTaken()));
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
        msg.append(System.getProperty("line.separator"));

        return msg.toString();
    }


}
