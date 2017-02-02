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
    private int mDoseID;
    private int mOfMedicationID;
    private int mForPersonID;
    private int mContainedInConcurrentDoseID;
    private int mPositionWithinConcDose;
    private int mTimeTaken;   //milliseconds since Jan1, 1970;
    private int mAmountTaken; //default can be overridden


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

    public MMDose(int ofMedicationID,
                  int forPersonID,
                  int containedInConcurrentDosesID,
                  int positionWithinConcDose,
                  int timeTaken,
                  int amountTaken) {
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

    public int  getTimeTaken()               { return mTimeTaken;  }
    public void setTimeTaken(int timeTaken) {mTimeTaken = timeTaken;   }

    public int  getAmountTaken()                {return mAmountTaken;   }
    public void setAmountTaken(int amountTaken) {  mAmountTaken = amountTaken;  }

    /*************************************/
    /*          Member Methods           */
    /*************************************/


}
