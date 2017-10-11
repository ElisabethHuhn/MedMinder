package com.androidchicken.medminder;

import android.content.Context;
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

class MMScheduleListCursorAdapter extends RecyclerView.Adapter<MMScheduleListCursorAdapter.MyViewHolder>{
    //The group to be listed is collected from a Cursor representing the DB rows
    private Cursor  mSchedMedCursor;
    private long    mPersonID;
    private Context mActivity;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        EditText medicationNickNameOutput;
        EditText medicationTime, medicationAmt, medicationUnits;


        MyViewHolder(View v) {
            super(v);

            medicationTime           = (EditText) v.findViewById(R.id.scheduleTimeOutput);
            medicationNickNameOutput = (EditText) v.findViewById(R.id.scheduleMedNameOutput);
            medicationAmt            = (EditText) v.findViewById(R.id.scheduleMedAmtOutput);
            medicationUnits          = (EditText) v.findViewById(R.id.scheduleUnitsOutput);
        }

    } //end inner class MyViewHolder

    //Constructor for MMSchedMedsAdapter
    MMScheduleListCursorAdapter(Context activity,
                                       Cursor schedMedCursor,
                                       long personID){

        this.mActivity       = activity;
        this.mSchedMedCursor = schedMedCursor;
        this.mPersonID       = personID;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_schedule_med, parent,false);
        return new MyViewHolder(itemView);

    }



    Cursor reinitializeCursor(long personID){
        closeCursor();

        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();

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

        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();

        if (mSchedMedCursor == null ) {

            mSchedMedCursor = schedMedManager.getAllSchedMedsForPersonCursor(mPersonID);
            if (mSchedMedCursor == null) {
                holder.medicationTime          .setText(mActivity.getString(R.string.default_hour));
                holder.medicationNickNameOutput.setText("");
                return;
            }
        }
        //get the medication indicated
        MMSchedule schedMed =
                schedMedManager.getScheduleMedicationFromCursor(mSchedMedCursor, position);

        String timeString = schedMed.getTimeDueString((MMMainActivity)mActivity);
        holder.medicationTime    .setText(timeString);

        long medicationID = schedMed.getOfMedicationID();
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        MMMedication medication = medicationManager.getMedicationFromID(medicationID);


        CharSequence msg;
        int strategy;
        int amount = 0;
        String amountString;
        String amountUnits ;
        if (medication == null){
            msg = mActivity.getString(R.string.sched_list_med_doesnt_exist);
            strategy = MMMedication.sAS_NEEDED;
            amountString = "0";
            amountUnits  = "mg";
        } else {
            msg = medication.getMedicationNickname();
            strategy = medication.getDoseStrategy();
            amount = medication.getDoseAmount();
            amountString = String.valueOf(amount);
            amountUnits = medication.getDoseUnits().toString();
        }

        holder.medicationNickNameOutput.setText(msg);
        //override the time if is to be taken as needed
        if (strategy == MMMedication.sAS_NEEDED){
            msg = mActivity.getString(R.string.sched_list_as_needed);
            holder.medicationTime    .setText(msg);
        }
        holder.medicationAmt.setText(amountString);
        holder.medicationUnits.setText(amountUnits);

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


    void closeCursor(){
        if (mSchedMedCursor != null)mSchedMedCursor.close();
    }
}
