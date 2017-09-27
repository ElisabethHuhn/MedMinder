package com.androidchicken.medminder;

import java.util.ArrayList;

/**
 * Created by elisabethhuhn on 10/12/2016.
 * A person either is the patient whose medications are being tracked, or
 * is someone who will potentially recieve a notification about a dose being missed.
 */

public class MMPerson {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/

    //Tags for attributes
    public static final String sPersonTag           = "PERSON_OBJECT";
    public static final String sPersonNicknameTag   = "PERSON_NAME";
    public static final String sPersonIDTag         = "PERSON_ID";
    public static final String sPersonEmailTag      = "PERSON_EMAIL";
    public static final String sPersonTextTag       = "PERSON_TEXT";

    public static final int TEMP_PERSON = -1;


    public static final String sPersonMedicationPositionTag = "PERSON_MED_POSITION";


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


    //************************************/
    /*         Static Methods            */
    //************************************/


    //************************************/
    /*         CONSTRUCTOR               */
    //************************************/

    public MMPerson( ) {
        initializeDefaultVariables();
    }

    public MMPerson( long tempPersonID) {
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
    public long getPersonID()            {  return mPersonID; }
    public void setPersonID(long personID){ mPersonID = personID;}

    public CharSequence getNickname()                      { return mNickname;  }
    public void         setNickname(CharSequence nickname) { mNickname = nickname; }

    public String getNameString(){
        StringBuilder message = new StringBuilder();

        message.append("Patient:  (ID ");
        message.append(String.valueOf(mPersonID));
        message.append(")  ");
        message.append(mNickname);

        return message.toString();
    }



    public CharSequence getEmailAddress()                          { return mEmailAddress;  }
    public void         setEmailAddress(CharSequence emailAddress) { mEmailAddress = emailAddress; }

    public CharSequence getTextAddress()                         { return mTextAddress;  }
    public void         setTextAddress(CharSequence textAddress) { mTextAddress = textAddress; }

    public boolean isCurrentlyExists()                   {return mCurrentlyExists;}
    public void         setCurrentlyExists(boolean isExistant) {mCurrentlyExists = isExistant;}


    public ArrayList<MMMedication> getMedications(){
        if (!isMedicationsChanged()) {
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            mMedications = databaseManager.getAllMedications(mPersonID);
        }
        return mMedications;
    }
    public void setMedications(ArrayList<MMMedication> medications) { mMedications = medications; }
    public boolean isMedicationsChanged(){
        if ((mMedications == null) || (mMedications.size()==0)) return false;
        return true;
    }

    //************************************/
    /*          Member Methods           */
    //************************************/


    public String cdfHeaders(){
        return
            "PersonID, "      +
            "Nickname, "      +
            "EmailAddress, "  +
            "TextAddress, "   +
            "Current  "      +
            System.getProperty("line.separator");

    }

    //Convert point to comma delimited file for exchange
    public String convertToCDF() {
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

    public String shortString(){
        StringBuilder message = new StringBuilder();
        String ls = System.getProperty("line.separator");

        message.append(getNameString());
        message.append(" is ");
        if (!isCurrentlyExists()){
            message.append("NOT ");
        }
        message.append("currently active.");
        message.append(System.getProperty("line.separator"));

        if (!mEmailAddress.toString().isEmpty()) {
            message.append(" Email Address is ");
            message.append(mEmailAddress);
        }
        message.append(ls);
        if (!mEmailAddress.toString().isEmpty() && !mTextAddress.toString().isEmpty()){
            message.append(" and ");
        }
        if (!mTextAddress.toString().isEmpty()) {
            message.append(" Text number is ");
            message.append(mTextAddress);
        }
        return message.toString();
    }

}
