package com.androidchicken.medminder;

import java.util.ArrayList;
import java.util.StringTokenizer;

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


    //Tags for fragment arguments
    public static final String sPersonTag           = "PERSON_OBJECT";
    public static final String sPersonNicknameTag   = "PERSON_NAME";
    public static final String sPersonIDTag         = "PERSON_ID";
    public static final String sPersonEmailTag      = "PERSON_EMAIL";
    public static final String sPersonTextTag       = "PERSON_TEXT";
    public static final String sPersonDurationTag   = "PERSON_DURATION";
    public static final String sPersonOrderTag      = "PERSON_ORDER";

    public static final String sPersonMedicationPositionTag = "PERSON_MED_POSITION";


    /*************************************/
    /*    Static (class) Variables       */
    /*************************************/


    /*************************************/
    /*    Member (instance) Variables    */
    /*************************************/
    private int          mPersonID;
    private CharSequence mNickname;
    private CharSequence mEmailAddress;
    private CharSequence mTextAddress;
    private int          mDuration = DURATION_QUARTER_HOUR;
    private int          mMedOrder = ORDER_MEDICATION_NICKNAME;
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
        mPersonID     = MMUtilities.getUniqueID();
    }

/*
    private void initializeDefaultVariables(){
        mPersonID     = MMUtilities.getUniqueID();
        mNickname     = "Nickname";
        mEmailAddress = "Email@gmail.com";
        mTextAddress  = "6783587040";
        mDuration     = DURATION_QUARTER_HOUR;
        mMedOrder     = ORDER_MEDICATION_GENERIC;
        mMedications  = new ArrayList<>();
    }
*/
private void initializeDefaultVariables(){
    mPersonID     = MMUtilities.getUniqueID();
    mNickname     = "1";
    mEmailAddress = "2";
    mTextAddress  = "3";
    mDuration     = 4;
    mMedOrder     = 5;
    mMedications  = new ArrayList<>();
}

    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/
    public int  getPersonID()            {  return mPersonID; }
    public void setPersonID(int personID){ mPersonID = personID;}

    public CharSequence getNickname()                      { return mNickname;  }
    public void         setNickname(CharSequence nickname) { mNickname = nickname; }

    public CharSequence getEmailAddress()                          { return mEmailAddress;  }
    public void         setEmailAddress(CharSequence emailAddress) { mEmailAddress = emailAddress; }

    public CharSequence getTextAddress()                         { return mTextAddress;  }
    public void         setTextAddress(CharSequence textAddress) { mTextAddress = textAddress; }

    public int  getDuration()              { return mDuration; }
    public void setDuration(int duration)  { mDuration = duration; }

    public int  getMedOrder()              {return mMedOrder; }
    public void setMedOrder(int medOrder)  { mMedOrder = medOrder; }

    public ArrayList<MMMedication> getMedications()                 { return mMedications; }
    public void setMedications(ArrayList<MMMedication> medications) { mMedications = medications; }

    /*************************************/
    /*          Member Methods           */
    /*************************************/



}
