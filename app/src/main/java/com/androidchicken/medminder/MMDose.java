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
    private int  mDoseID;
    private int  mOfMedicationID;
    private int  mForPersonID;
    private int  mContainedInConcurrentDoseID;
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
        mDoseID         = MMUtilities.getUniqueID();
        mOfMedicationID = 0;
        mForPersonID    = 0;
        mContainedInConcurrentDoseID = 0;
        mPositionWithinConcDose = 0;
        mTimeTaken      = 0;
        mAmountTaken    = 0;
    }

    public MMDose(int  ofMedicationID,
                  int  forPersonID,
                  int  containedInConcurrentDosesID,
                  int  positionWithinConcDose,
                  long timeTaken,
                  int  amountTaken) {
        mDoseID         = MMUtilities.getUniqueID();
        mOfMedicationID = ofMedicationID;
        mForPersonID    = forPersonID;
        mContainedInConcurrentDoseID = containedInConcurrentDosesID;
        mPositionWithinConcDose      = positionWithinConcDose;
        mTimeTaken      = timeTaken;
        mAmountTaken    = amountTaken;
    }

    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/

    public int  getDoseID() {return mDoseID;  }
    public void setDoseID(int doseID){mDoseID = doseID;}

    public int  getOfMedicationID()                    {   return mOfMedicationID;   }
    public void setOfMedicationID(int ofMedicationID) { mOfMedicationID = ofMedicationID; }

    public int  getForPersonID()                 {  return mForPersonID;    }
    public void setForPersonID(int forPersonID) {  mForPersonID = forPersonID;    }

    public int  getContainedInConcurrentDosesID() { return mContainedInConcurrentDoseID; }
    public void setContainedInConcurrentDosesID(int containedInConcurrentDosesID) {
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
