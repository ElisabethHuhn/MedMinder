package com.androidchicken.medminder;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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

public class MMSchedMedCursorAdapter extends RecyclerView.Adapter<MMSchedMedCursorAdapter.MyViewHolder>{
    //The group to be listed is collected from a Cursor representing the DB rows
    private Cursor  mSchedMedCursor;
    private long    mMedicationID;

    //implement the ViewHolder as an inner class
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public EditText  medicationNickName, medicationForPerson;
        public EditText medicationTimeHours, medicationTimeMinutes;

        public MyViewHolder(View v) {
            super(v);

            medicationTimeHours   = (EditText) v.findViewById(R.id.scheduleTimeHourOutput);
            medicationTimeMinutes = (EditText) v.findViewById(R.id.scheduleTimeMinutesOutput);

            medicationTimeHours.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                    //This tells you that text is about to change.
                    // Starting at character "start", the next "count" characters
                    // will be changed with "after" number of characters

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    //This tells you where the text has changed
                    //Starting at character "start", the "before" number of characters
                    // has been replaced with "count" number of characters


                }

                @Override
                public void afterTextChanged(Editable editable) {
                    //This tells you that somewhere within editable, it's text has changed

                }
            });


            medicationTimeHours.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                    //This tells you that text is about to change.
                    // Starting at character "start", the next "count" characters
                    // will be changed with "after" number of characters

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    //This tells you where the text has changed
                    //Starting at character "start", the "before" number of characters
                    // has been replaced with "count" number of characters


                }

                @Override
                public void afterTextChanged(Editable editable) {
                    //This tells you that somewhere within editable, it's text has changed

                }
            });

        }

    } //end inner class MyViewHolder

    //Constructor for MMSchedMedsAdapter
    public MMSchedMedCursorAdapter(Cursor schedMedCursor){
        this.mSchedMedCursor = schedMedCursor;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_schedule_med, parent,false);
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
                holder.medicationTimeHours.setText("0");
                holder.medicationTimeMinutes.setText("0");
                return;
            }
        }
        //get the medication indicated
        MMScheduleMedication schedMed =
                schedMedManager.getScheduleMedicationFromCursor(mSchedMedCursor, position);

        int hours = schedMed.getTimeDue() / 60;
        int minutes = (schedMed.getTimeDue()) - (hours * 60);

        holder.medicationTimeHours  .setText(String.valueOf(hours));
        holder.medicationTimeMinutes.setText(String.valueOf(minutes));
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

    public void setAdapterContext(long medicationID){
        mMedicationID = medicationID;
    }
}
