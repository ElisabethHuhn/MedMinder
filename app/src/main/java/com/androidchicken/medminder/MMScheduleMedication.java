package com.androidchicken.medminder;

/**
 * Created by Elisabeth Huhn on 2/14/17.
 *
 * This class represents when a medication should be taken
 */

public class MMScheduleMedication {
    /*************************************/
    /*    Static (class) Constants       */
    /*************************************/


    /*************************************/
    /*    Static (class) Variables       */
    /*************************************/


    /*************************************/
    /*    Member (instance) Variables    */
    /*************************************/
    private int mSchedMedID;
    private int mOfMedicationID;
    private int mForPersonID;
    private int mTimeDue;  //number of minutes from midnight



    /*************************************/
    /*         Static Methods            */
    /*************************************/


    /*************************************/
    /*         CONSTRUCTOR               */
    /*************************************/
    public MMScheduleMedication() {
        mSchedMedID     = MMUtilities.getUniqueID();
        mOfMedicationID = 0;
        mForPersonID    = 0;
        mTimeDue        = 0;
    }

    public MMScheduleMedication(int  ofMedicationID,
                                int  forPersonID,
                                int  timeDue) {
        mSchedMedID     = MMUtilities.getUniqueID();
        mOfMedicationID = ofMedicationID;
        mForPersonID    = forPersonID;
        mTimeDue        = timeDue;
    }

    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/

    public int  getSchedMedID()              {return mSchedMedID;  }
    public void setSchedMedID(int schedMedID){ mSchedMedID = schedMedID;}

    public int  getOfMedicationID()                   {   return mOfMedicationID;   }
    public void setOfMedicationID(int ofMedicationID) { mOfMedicationID = ofMedicationID; }

    public int  getForPersonID()                {  return mForPersonID;    }
    public void setForPersonID(int forPersonID) {  mForPersonID = forPersonID;    }

    public int  getTimeDue()            { return mTimeDue; }
    public void setTimeDue(int timeDue) {  mTimeDue = timeDue; }


    /*************************************/
    /*          Member Methods           */
    /*************************************/

    public String cdfHeaders(){
        return  "SchedMedID, "       +
                "MedicationID, "     +
                "PersonID, "         +
                "TimeDue "           +

                System.getProperty("line.separator");

    }

    //Convert point to comma delimited file for exchange
    public String convertToCDF() {
        return String.valueOf(this.getSchedMedID())      + ", " +
               String.valueOf(this.getOfMedicationID())  + ", " +
               String.valueOf(this.getForPersonID())     + ", " +
               String.valueOf(this.getTimeDue())         +
               System.getProperty("line.separator");
    }

}
