package com.androidchicken.medminder;

import java.util.ArrayList;

/**
 * Created by elisabethhuhn on 10/14/2016.
 * Represents a row on the Doses Taken screen.
 * A way of Aggregating history of doses into clock durations,
 * for example: all doses taken within a given quarter hour
 */

public class MMConcurrentDoses {
    /*************************************/
    /*    Static (class) Constants       */
    /*************************************/


    /*************************************/
    /*    Static (class) Variables       */
    /*************************************/

    private int               mConcurrentDoseID;
    private int               mForPerson;
    private boolean           mIsStartOfDay;
    private int               mStartTime;
    private ArrayList<MMDose> mDoses;


    /*************************************/
    /*    Member (instance) Variables    */
    /*************************************/



    /*************************************/
    /*         Static Methods            */
    /*************************************/


    /*************************************/
    /*         CONSTRUCTOR               */
    /*************************************/
    public MMConcurrentDoses(int forPerson, boolean isStartOfDay, int startTime) {
        mConcurrentDoseID = MMUtilities.getUniqueID();
        mForPerson    = forPerson;
        mIsStartOfDay = isStartOfDay;
        mStartTime    = startTime;
        mDoses        = new ArrayList<MMDose>();
    }

    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/
    //getter only
    public int getConcurrentDoseID() { return mConcurrentDoseID;  }

    public int  getForPerson()              { return mForPerson;  }
    public void setForPerson(int forPerson) {  mForPerson = forPerson;   }

    public boolean isStartOfDay()                    {  return mIsStartOfDay;  }
    public void    setStartOfDay(boolean startOfDay) {  mIsStartOfDay = startOfDay; }

    public int  getStartTime()              { return mStartTime;  }
    public void setStartTime(int startTime) { mStartTime = startTime; }

    public ArrayList<MMDose> getDoses()                        { return mDoses; }
    public void              setDoses(ArrayList<MMDose> doses) { mDoses = doses; }

    /*************************************/
    /*          Member Methods           */
    /*************************************/
    public boolean addDose(MMDose dose){
        if (mDoses == null){
            mDoses = new ArrayList<MMDose>();
        }
        mDoses.add(dose);
        return true;
    }

}
