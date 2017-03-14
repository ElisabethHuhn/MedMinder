package com.androidchicken.medminder;

/**
 * Created by elisabethhuhn on 10/12/2016.
 *
 * This class represents the doses of medication a person has actually taken.
 * It is a history of prescription complience
 */

public class MMDose {
    /*************************************/
    /*    Static (class) Constants       */
    /*************************************/


    /*************************************/
    /*    Static (class) Variables       */
    /*************************************/


    /*************************************/
    /*    Member (instance) Variables    */
    /*************************************/
    private long mDoseID;
    private long mOfMedicationID;
    private long mForPersonID;
    private long mContainedInConcurrentDoseID;
    private int  mPositionWithinConcDose;
    private long mTimeTaken;   //milliseconds since Jan1, 1970;
    private int  mAmountTaken; //default can be overridden


    /*************************************/
    /*         Static Methods            */
    /*************************************/


    /*************************************/
    /*         CONSTRUCTOR               */
    /*************************************/
    public MMDose() {
        initializeVariables();
    }

    public MMDose(long tempID){
        initializeVariables();
        mDoseID = tempID;
    }

    public MMDose(long ofMedicationID,
                  long forPersonID,
                  long containedInConcurrentDosesID,
                  int  positionWithinConcDose,
                  long timeTaken,
                  int  amountTaken) {
        mDoseID         = MMUtilities.ID_DOES_NOT_EXIST;
        mOfMedicationID = ofMedicationID;
        mForPersonID    = forPersonID;
        mContainedInConcurrentDoseID = containedInConcurrentDosesID;
        mPositionWithinConcDose      = positionWithinConcDose;
        mTimeTaken      = timeTaken;
        mAmountTaken    = amountTaken;
    }


    private void initializeVariables(){
        mDoseID         = MMUtilities.ID_DOES_NOT_EXIST;
        mOfMedicationID = 0;
        mForPersonID    = 0;
        mContainedInConcurrentDoseID = 0;
        mPositionWithinConcDose = 0;
        mTimeTaken      = 0;
        mAmountTaken    = 0;

    }

    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/

    public long getDoseID() {return mDoseID;  }
    public void setDoseID(long doseID){mDoseID = doseID;}

    public long getOfMedicationID()                    {   return mOfMedicationID;   }
    public void setOfMedicationID(long ofMedicationID) { mOfMedicationID = ofMedicationID; }

    public long getForPersonID()                 {  return mForPersonID;    }
    public void setForPersonID(long forPersonID) {  mForPersonID = forPersonID;    }

    public long getContainedInConcurrentDosesID() { return mContainedInConcurrentDoseID; }
    public void setContainedInConcurrentDosesID(long containedInConcurrentDosesID) {
        mContainedInConcurrentDoseID = containedInConcurrentDosesID;
    }

    public int  getPositionWithinConcDose() {return mPositionWithinConcDose;}
    public void setPositionWithinConcDose(int position) {mPositionWithinConcDose = position;}

    public long getTimeTaken()               { return mTimeTaken;  }
    public void setTimeTaken(long timeTaken) {mTimeTaken = timeTaken;   }

    public int  getAmountTaken()                {return mAmountTaken;   }
    public void setAmountTaken(int amountTaken) {  mAmountTaken = amountTaken;  }

    /*************************************/
    /*          Member Methods           */
    /*************************************/

    public String cdfHeaders(){
        return
                "DoseID, "           +
                "MedicationID, "     +
                "PersonID, "         +
                "ConcurrentDoseID, " +
                "Position, "         +
                "TimeTaken, "        +
                "AmountTaken"        +
                System.getProperty("line.separator");

    }

    //Convert point to comma delimited file for exchange
    public String convertToCDF() {
        return String.valueOf(this.getDoseID())          + ", " +
               String.valueOf(this.getOfMedicationID())  + ", " +
               String.valueOf(this.getForPersonID())     + ", " +
               String.valueOf(this.getContainedInConcurrentDosesID()) + ", " +
               String.valueOf(this.getPositionWithinConcDose() )      + ", " +
               String.valueOf(this.getTimeTaken())       + ", " +
               String.valueOf(this.getAmountTaken())     +
               System.getProperty("line.separator");
    }

}
