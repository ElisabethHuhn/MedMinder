package com.androidchicken.medminder;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by Elisabeth Huhn on 2/25/2017.
 *
 * Serves as a liaison between a list RecyclerView and the SchedMedManager
 * Adapted from the ConcurrentDoseAdapter as one needs to create views programmatically as
 * the number of schedule times varies in real time, and is not known at compile time
 */

class MMScheduleCursorAdapter extends RecyclerView.Adapter<MMScheduleCursorAdapter.MyViewHolder>{
    //The group to be listed is collected from a Cursor representing the DB rows
    private Cursor  mSchedMedCursor;
    private MMMainActivity mActivity;
    private long    mMedicationID;
    private int     mStrategy;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {

        EditText medicationTime;

        MyViewHolder(View v) {
            super(v);

            medicationTime   = (EditText) v.findViewById(R.id.scheduleTimeOutput);

        }

    } //end inner class MyViewHolder

    //Constructor for MMSchedMedsAdapter
    MMScheduleCursorAdapter(MMMainActivity activity, Cursor schedMedCursor, long medicationID){

        this.mSchedMedCursor = schedMedCursor;
        this.mActivity       = activity;
        this.mMedicationID   = medicationID;

        MMMedication medication = MMMedicationManager.getInstance().getMedicationFromID(medicationID);
        mStrategy = medication.getDoseStrategy();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_schedule, parent,false);
        return new MyViewHolder(itemView);

    }

    //This isn't going to work with a cursor, its going to have to be removed from the DB

    void removeItem(int position) {
        if (mSchedMedCursor == null)return;

        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();

        //get the row indicated which is the person to be removed
        MMSchedule schedMed =
                schedMedManager.getScheduleMedicationFromCursor(mSchedMedCursor, position);
        if (schedMed == null)return;

        //remove the person from the DB
        long schedMedID = schedMed.getSchedMedID();
        schedMedManager.removeSchedMedFromDB(schedMedID);
        //update the cursor for the adapter
        reinitializeCursor(schedMed.getOfMedicationID());
     }

    void removeAllItems(){
        //removes all items in the cursor
        if (mSchedMedCursor == null)return;

        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();

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


    Cursor reinitializeCursor(long medicationID){
        closeCursor();

        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();

        //Create a new Cursor with the current contents of DB
        mSchedMedCursor = schedMedManager.getAllSchedMedsCursor(medicationID);

        //Tell the adapter to update the User Display
        notifyDataSetChanged();

        notifyItemRangeChanged(0, getItemCount());


        return mSchedMedCursor;
    }

    void   resetStrategy(int strategy){
        mStrategy = strategy;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){

        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();

        if (mSchedMedCursor == null ) {

            mSchedMedCursor = schedMedManager.getAllSchedMedsCursor(mMedicationID);
            if (mSchedMedCursor == null) {
                holder.medicationTime.setText(mActivity.getString(R.string.default_hour));
                return;
            }
        }
        //get the medication indicated
        MMSchedule schedMed =
                schedMedManager.getScheduleMedicationFromCursor(mSchedMedCursor, position);

        String timeString = schedMed.getTimeDueString(mActivity);
        if (mStrategy == MMMedication.sAS_NEEDED){
            timeString = MMMedicationFragment.AS_NEEDED_STRATEGY;
        }

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

    Cursor getSchedMedCursor(){return mSchedMedCursor;}

    MMSchedule getScheduleAt(int position){
        MMScheduleManager scheduleManager = MMScheduleManager.getInstance();
        return scheduleManager.getScheduleMedicationFromCursor(mSchedMedCursor, position);
    }


    void closeCursor(){
        if (mSchedMedCursor != null)mSchedMedCursor.close();
    }
}
