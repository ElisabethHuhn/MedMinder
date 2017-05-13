package com.androidchicken.medminder;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Elisabeth Huhn on 2/25/2017.
 *
 * Serves as a liaison between a list RecyclerView and the SchedMedManager
 * Adapted from the ConcurrentDoseAdapter as one needs to create views programmatically as
 * the number of schedule times varies in real time, and is not known at compile time
 */

public class MMSchedCursorAdapter extends RecyclerView.Adapter<MMSchedCursorAdapter.MyViewHolder>{
    //The group to be listed is collected from a Cursor representing the DB rows
    private Cursor  mSchedMedCursor;
    private boolean mIs24Format;
    private long    mMedicationID;

    //implement the ViewHolder as an inner class
    public class MyViewHolder extends RecyclerView.ViewHolder {

        public EditText medicationTime;

        public MyViewHolder(View v) {
            super(v);

            medicationTime   = (EditText) v.findViewById(R.id.scheduleTimeOutput);

        }

    } //end inner class MyViewHolder

    //Constructor for MMSchedMedsAdapter
    public MMSchedCursorAdapter(Cursor schedMedCursor, boolean is24Format, long medicationID){

        this.mSchedMedCursor = schedMedCursor;
        this.mIs24Format     = is24Format;
        this.mMedicationID   = medicationID;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_schedule, parent,false);
        return new MyViewHolder(itemView);

    }

    //This isn't going to work with a cursor, its going to have to be removed from the DB

    public void removeItem(int position) {
        if (mSchedMedCursor == null)return;

        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();

        //get the row indicated which is the person to be removed
        MMScheduleMedication schedMed =
                schedMedManager.getScheduleMedicationFromCursor(mSchedMedCursor, position);
        if (schedMed == null)return;

        //remove the person from the DB
        long schedMedID = schedMed.getSchedMedID();
        schedMedManager.removeSchedMedFromDB(schedMedID);
        //update the cursor for the adapter
        reinitializeCursor(schedMed.getOfMedicationID());
     }

    public void removeAllItems(){
        //removes all items in the cursor
        if (mSchedMedCursor == null)return;

        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();

        int last = mSchedMedCursor.getCount();
        int position = 0;
        long scheduleID;
        while (position < last){
            //get the schedule ID from the cursor row
            scheduleID = schedMedManager.getScheduleIDFromCursor(mSchedMedCursor, position);
            if (scheduleID != MMUtilities.ID_DOES_NOT_EXIST) {
                //remove the schedule from the DB
                schedMedManager.removeSchedMedFromDB(scheduleID);
            }
            position++;
        }

        //update the cursor for the adapter
        reinitializeCursor(mMedicationID);

    }


    public Cursor reinitializeCursor(long medicationID){
        closeCursor();

        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();

        //Create a new Cursor with the current contents of DB
        mSchedMedCursor = schedMedManager.getAllSchedMedsCursor(medicationID);

        //Tell the adapter to update the User Display
        notifyDataSetChanged();

        notifyItemRangeChanged(0, getItemCount());


        return mSchedMedCursor;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){

        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();

        if (mSchedMedCursor == null ) {

            mSchedMedCursor = schedMedManager.getAllSchedMedsCursor(mMedicationID);
            if (mSchedMedCursor == null) {
                holder.medicationTime.setText("00:00 AM");
                return;
            }
        }
        //get the medication indicated
        MMScheduleMedication schedMed =
                schedMedManager.getScheduleMedicationFromCursor(mSchedMedCursor, position);

        int timeMinutes = schedMed.getTimeDue();
        //I tried just using milliseconds, but somewhere it reset the timezone,
        // causing the time I got back to be Greenwich.
        // Thus the need to use the Calendar object kludge
        //If you can do this better, have at it.
        long timeMilliseconds = timeMinutes * 60 * 1000;

        int hours   = timeMinutes / 60;
        int minutes = timeMinutes - (hours*60);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.SECOND, 0);
        Date d1 = c.getTime();

        String timeString = MMUtilities.getInstance()
                .getTimeString(MMUtilities.getInstance().getTimeFormatString(mIs24Format), d1);

        holder.medicationTime    .setText(timeString);
    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mSchedMedCursor != null) {
            returnValue = mSchedMedCursor.getCount();
        }
        return returnValue;
    }

    public Cursor getSchedMedCursor(){return mSchedMedCursor;}

    public MMScheduleMedication getScheduleAt(int position){
        MMSchedMedManager scheduleManager = MMSchedMedManager.getInstance();
        return scheduleManager.getScheduleMedicationFromCursor(mSchedMedCursor, position);
    }


    public void closeCursor(){
        if (mSchedMedCursor != null)mSchedMedCursor.close();
    }
}
