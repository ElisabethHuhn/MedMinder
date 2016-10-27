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
    private int mContainedInConcurrentDosesID;
    private int mTimeTaken;   //milliseconds since Jan1, 1970;
    private int mAmountTaken; //default can be overridden


    /*************************************/
    /*         Static Methods            */
    /*************************************/


    /*************************************/
    /*         CONSTRUCTOR               */
    /*************************************/
    public MMDose(int ofMedicationID,
                  int forPersonID,
                  int containedInConcurrentDosesID,
                  int timeTaken,
                  int amountTaken) {
        mDoseID         = MMUtilities.getUniqueID();
        mOfMedicationID = ofMedicationID;
        mForPersonID    = forPersonID;
        mContainedInConcurrentDosesID = containedInConcurrentDosesID;
        mTimeTaken      = timeTaken;
        mAmountTaken    = amountTaken;
    }

    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/
    //Getter only. Can not reset the ID once assigned
    public int getDoseID() {return mDoseID;  }


    public int getOfMedicationID()                    {   return mOfMedicationID;   }
    public void setOfMedicationID(int ofMedicationID) { mOfMedicationID = ofMedicationID; }

    public int getForPersonID()                 {  return mForPersonID;    }
    public void setForPersonID(int forPersonID) {  mForPersonID = forPersonID;    }

    public int getContainedInConcurrentDosesID() { return mContainedInConcurrentDosesID; }
    public void setContainedInConcurrentDosesID(int containedInConcurrentDosesID) {
        mContainedInConcurrentDosesID = containedInConcurrentDosesID;
    }

    public int getTimeTaken()               { return mTimeTaken;  }
    public void setTimeTaken(int timeTaken) {mTimeTaken = timeTaken;   }

    public int  getAmountTaken()                {return mAmountTaken;   }
    public void setAmountTaken(int amountTaken) {  mAmountTaken = amountTaken;  }

    /*************************************/
    /*          Member Methods           */
    /*************************************/


}
