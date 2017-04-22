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

public class MMSchedMedCursorAdapter extends RecyclerView.Adapter<MMSchedMedCursorAdapter.MyViewHolder>{
    //The group to be listed is collected from a Cursor representing the DB rows
    private Cursor  mSchedMedCursor;
    private boolean mIs24Format;
    private long    mPersonID;

    //implement the ViewHolder as an inner class
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public EditText medicationNickNameOutput;
        public EditText medicationTime;


        public MyViewHolder(View v) {
            super(v);

            medicationTime           = (EditText) v.findViewById(R.id.scheduleTimeOutput);
            medicationNickNameOutput = (EditText) v.findViewById(R.id.scheduleMedNameOutput);
        }

    } //end inner class MyViewHolder

    //Constructor for MMSchedMedsAdapter
    public MMSchedMedCursorAdapter(Cursor schedMedCursor, boolean is24Format, long personID){

        this.mSchedMedCursor = schedMedCursor;
        this.mIs24Format     = is24Format;
        this.mPersonID       = personID;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_schedule_med, parent,false);
        return new MyViewHolder(itemView);

    }



    public Cursor reinitializeCursor(long personID){
        closeCursor();

        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();

        //Create a new Cursor with the current contents of DB
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST){
            //get all schedules for all meds for all people
            mSchedMedCursor = schedMedManager.getAllSchedMedsCursor();
        } else {
            mSchedMedCursor = schedMedManager.getAllSchedMedsForPersonCursor(personID);
        }

        //Tell the adapter to update the User Display
        notifyDataSetChanged();

        notifyItemRangeChanged(0, getItemCount());


        return mSchedMedCursor;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){

        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();

        if (mSchedMedCursor == null ) {

            mSchedMedCursor = schedMedManager.getAllSchedMedsForPersonCursor(mPersonID);
            if (mSchedMedCursor == null) {
                holder.medicationTime          .setText("00:00 AM");
                holder.medicationNickNameOutput.setText("");
                return;
            }
        }
        //get the medication indicated
        MMScheduleMedication schedMed =
                schedMedManager.getScheduleMedicationFromCursor(mSchedMedCursor, position);

        int timeMinutes = schedMed.getTimeDue();
        long timeMilliseconds = timeMinutes * 60 * 1000;
        String timeString = MMUtilities.getTimeString(timeMilliseconds, mIs24Format);
        holder.medicationTime    .setText(timeString);

        long medicationID = schedMed.getOfMedicationID();
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        MMMedication medication = medicationManager.getMedicationFromID(medicationID);
        CharSequence msg;
        if (medication == null){
            msg = "DOESN'T EXIST";
        } else {
            msg = medication.getMedicationNickname();
        }
        holder.medicationNickNameOutput.setText(msg);

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
