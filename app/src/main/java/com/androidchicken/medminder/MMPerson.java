package com.androidchicken.medminder;

import java.util.ArrayList;

/**
 * Created by elisabethhuhn on 10/12/2016.
 * A person either is the patient whose medications are being tracked, or
 * is someone who will potentially recieve a notification about a dose being missed.
 */

public class MMPerson {
    /*************************************/
    /*    Static (class) Constants       */
    /*************************************/
    public static final int DURATION_QUARTER_HOUR = 1;
    public static final int DURATION_THIRD_HOUR   = 2;
    public static final int DURATION_HALF_HOUR    = 3;
    public static final int DURATION_HOUR         = 4;
    public static final int DURATION_DAILY        = 5;


    public static final int ORDER_MEDICATION_NICKNAME   = 1;
    public static final int ORDER_MEDICATION_GENERIC    = 2;
    public static final int ORDER_MEDICATION_BRAND      = 3;
    public static final int ORDER_MEDICATION_BY_SETTING = 4;


    //Tags for attributes
    public static final String sPersonTag           = "PERSON_OBJECT";
    public static final String sPersonNicknameTag   = "PERSON_NAME";
    public static final String sPersonIDTag         = "PERSON_ID";
    public static final String sPersonEmailTag      = "PERSON_EMAIL";
    public static final String sPersonTextTag       = "PERSON_TEXT";

    public static final int TEMP_PERSON = -1;


    public static final String sPersonMedicationPositionTag = "PERSON_MED_POSITION";


    /*************************************/
    /*    Static (class) Variables       */
    /*************************************/


    /*************************************/
    /*    Member (instance) Variables    */
    /*************************************/
    private long         mPersonID;
    private CharSequence mNickname;
    private CharSequence mEmailAddress;
    private CharSequence mTextAddress;
    private ArrayList<MMMedication> mMedications;


    /*************************************/
    /*         Static Methods            */
    /*************************************/


    /*************************************/
    /*         CONSTRUCTOR               */
    /*************************************/
    public MMPerson( CharSequence nickname) {
        initializeDefaultVariables();
        mNickname     = nickname;
    }

    public MMPerson( ) {
        initializeDefaultVariables();
    }

    public MMPerson( long tempPersonID) {
        initializeDefaultVariables();
        mPersonID = tempPersonID;
    }



    private void initializeDefaultVariables(){
        mPersonID     = MMUtilities.getUniqueID();
        mNickname     = "Nickname";
        mEmailAddress = "Email@gmail.com";
        mTextAddress  = "6783587040";
        mMedications  = null;
    }

/*
    private void initializeDefaultVariables(){
        mPersonID     = MMUtilities.getUniqueID();
        mNickname     = "1";
        mEmailAddress = "2";
        mTextAddress  = "3";
        mMedications  = new ArrayList<>();
    }
*/


    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/
    public long getPersonID()            {  return mPersonID; }
    public void setPersonID(long personID){ mPersonID = personID;}

    public CharSequence getNickname()                      { return mNickname;  }
    public void         setNickname(CharSequence nickname) { mNickname = nickname; }

    public CharSequence getEmailAddress()                          { return mEmailAddress;  }
    public void         setEmailAddress(CharSequence emailAddress) { mEmailAddress = emailAddress; }

    public CharSequence getTextAddress()                         { return mTextAddress;  }
    public void         setTextAddress(CharSequence textAddress) { mTextAddress = textAddress; }


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

    /*************************************/
    /*          Member Methods           */
    /*************************************/


    public String cdfHeaders(){
        String msg =
            "PersonID, "      +
            "Nickname, "      +
            "EmailAddress, "  +
            "TextAddress, "   +
            "MedOrder  "      +
            System.getProperty("line.separator");
        return msg;
    }

    //Convert point to comma delimited file for exchange
    public String convertToCDF() {
        return String.valueOf(this.getPersonID())      + ", " +
                String.valueOf(this.getNickname())     + ", " +
                String.valueOf(this.getEmailAddress()) + ", " +
                String.valueOf(this.getTextAddress())  +
                System.getProperty("line.separator");
    }


}
