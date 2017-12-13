package com.androidchicken.medminder;

import java.util.ArrayList;

/**
 * Created by elisabethhuhn on 10/12/2016.
 * A person either is the patient whose medications are being tracked, or
 * is someone who will potentially recieve a notification about a dose being missed.
 */

class MMPerson {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/

    //Tags for attributes
    //static final String sPersonTag           = "PERSON_OBJECT";
    //static final String sPersonNicknameTag   = "PERSON_NAME";
    static final String sPersonIDTag         = "PERSON_ID";
   // static final String sPersonEmailTag      = "PERSON_EMAIL";
   // static final String sPersonTextTag       = "PERSON_TEXT";


    static final String sPersonMedicationPositionTag = "PERSON_MED_POSITION";


    //************************************/
    /*    Static (class) Variables       */
    //************************************/


    //************************************/
    /*    Member (instance) Variables    */
    //************************************/
    private long         mPersonID;
    private CharSequence mNickname;
    private CharSequence mEmailAddress;
    private CharSequence mTextAddress;
    private boolean      mCurrentlyExists;
    private ArrayList<MMMedication> mMedications;
    private boolean      mCurrentOnlyFlag;


    //************************************/
    /*         Static Methods            */
    //************************************/


    //************************************/
    /*         CONSTRUCTOR               */
    //************************************/

    MMPerson( ) {
        initializeDefaultVariables();
    }

    MMPerson( long tempPersonID) {
        initializeDefaultVariables();
        mPersonID = tempPersonID;
    }



    private void initializeDefaultVariables(){
        mPersonID     = MMUtilities.ID_DOES_NOT_EXIST;

        mNickname     = "";
        mEmailAddress = "";
        mTextAddress  = "";


        mCurrentlyExists = true;
        mMedications  = null;
    }



    //************************************/
    /*    Member setter/getter Methods   */
    //************************************/
    long getPersonID()            {  return mPersonID; }
    void setPersonID(long personID){ mPersonID = personID;}

    CharSequence getNickname()                      { return mNickname;  }
    void         setNickname(CharSequence nickname) { mNickname = nickname; }

    private String getNameString(){
        StringBuilder message = new StringBuilder();

        message.append("Patient:  (ID ");
        message.append(String.valueOf(mPersonID));
        message.append(")  ");
        message.append(mNickname);

        return message.toString();
    }



    CharSequence getEmailAddress()                          { return mEmailAddress;  }
    void         setEmailAddress(CharSequence emailAddress) { mEmailAddress = emailAddress; }

    CharSequence getTextAddress()                         { return mTextAddress;  }
    void         setTextAddress(CharSequence textAddress) { mTextAddress = textAddress; }

    boolean isCurrentlyExists()                   {return mCurrentlyExists;}
    void         setCurrentlyExists(boolean isExistant) {mCurrentlyExists = isExistant;}


    //Pay no mind to the DB, just return what is on the Person Object
    ArrayList<MMMedication> getMedications(){
        return mMedications;
    }

    ArrayList<MMMedication> getMedications(boolean currentOnly){
        if (!isMedListUpToDate(currentOnly)) {
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            mMedications = databaseManager.getAllMedications(mPersonID, currentOnly);
        }

        return mMedications;
    }
    void setMedications(ArrayList<MMMedication> medications) { mMedications = medications; }
    boolean isMedListUpToDate(boolean currentOnly){
        boolean oldCurrentOnly = mCurrentOnlyFlag;
        mCurrentOnlyFlag = currentOnly;
        if ((currentOnly != oldCurrentOnly) ||
            (mMedications == null)          ||
            (mMedications.size()==0)) {
            return false;
        }
        return true;
    }

    //reset is needed whenever settings are changed to show deleted meds
    void resetMedicationsChanged(){
        mMedications = null;
    }

    void setCurrentOnly(boolean currentOnlyFlag){mCurrentOnlyFlag = currentOnlyFlag;}

    //************************************/
    /*          Member Methods           */
    //************************************/


    String cdfHeaders(){
        return
            "PersonID, "      +
            "Nickname, "      +
            "EmailAddress, "  +
            "TextAddress, "   +
            "Current  "      +
            System.getProperty("line.separator");

    }

    //Convert point to comma delimited file for exchange
    String convertToCDF() {
        return String.valueOf(this.getPersonID())      + ", " +
                String.valueOf(this.getNickname())     + ", " +
                String.valueOf(this.getEmailAddress()) + ", " +
                String.valueOf(this.getTextAddress())  +
                System.getProperty("line.separator");
    }

    public String toString() {
        return
            System.getProperty("line.separator") +
            "PERSON:"        + System.getProperty("line.separator") +
            "PersonID:     " + String.valueOf(mPersonID)     + System.getProperty("line.separator") +
            "Nickname:     " + String.valueOf(mNickname)     + System.getProperty("line.separator") +
            "EmailAddress: " + String.valueOf(mEmailAddress) + System.getProperty("line.separator") +
            "TextAddress:  " + String.valueOf(mTextAddress)  + System.getProperty("line.separator") +
            "Current?      " + String.valueOf(mCurrentlyExists) + System.getProperty("line.separator");
    }

    String shortString(){
        StringBuilder message = new StringBuilder();
        String ls = System.getProperty("line.separator");

        message.append(getNameString());
        message.append(ls);

        if (!isCurrentlyExists()){
            message.append("NOT ");
        }
        message.append("Currently active.");
        message.append(ls);

        if (!mEmailAddress.toString().isEmpty()) {
            message.append(" Email Address: ");
            message.append(mEmailAddress);
        }
        message.append(ls);

        if (!mTextAddress.toString().isEmpty()) {
            message.append(" Text number: ");
            message.append(mTextAddress);
        }
        return message.toString();
    }

}
