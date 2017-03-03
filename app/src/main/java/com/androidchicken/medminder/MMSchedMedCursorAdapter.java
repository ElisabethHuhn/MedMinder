package com.androidchicken.medminder;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;

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
        public EditText  medicationNickName, medicationForPerson, medicationTime;
        public ArrayList<EditText> doseTimes = new ArrayList<EditText>();


        public MyViewHolder(View v) {
            super(v);

            //remember the views we know about at coding time
            medicationNickName  = (EditText) v.findViewById(R.id.medicationNickNameOutput);
            medicationForPerson = (EditText) v.findViewById(R.id.medicationForPersonOutput);
            medicationTime      = (EditText) v.findViewById(R.id.scheduleTimeOutput);
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


    public Cursor reinitializeCursor(long medicationID){
        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();

        //Create a new Cursor with the current contents of DB
        mSchedMedCursor = schedMedManager.getAllScheduleMedicationsCursor(medicationID);

        //Tell the RecyclerView to update the User Display
        notifyDataSetChanged();

        //notifyItemRangeChanged(position, getItemCount());

        return mSchedMedCursor;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){

        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();

        if (mSchedMedCursor == null ) {

            mSchedMedCursor = schedMedManager.getAllScheduleMedicationsCursor(mMedicationID);
            if (mSchedMedCursor == null) {
                holder.medicationNickName.setText("");
                holder.medicationForPerson.setText("");
                holder.medicationTime.setText("");
                return;
            }
        }
        //get the medication indicated
        MMScheduleMedication schedMed = schedMedManager.getScheduleMedicationFromCursor(mSchedMedCursor, position);

        long medicationID = schedMed.getOfMedicationID();
        MMMedication medication = MMUtilities.getMedication(medicationID);

        holder.medicationNickName. setText(medication.getMedicationNickname().toString().trim());
        holder.medicationForPerson.setText(String.valueOf(medication.getForPersonID()).trim());
        holder.medicationTime.     setText(String.valueOf(schedMed.getTimeDue()).trim());
    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mSchedMedCursor != null) {
            returnValue = mSchedMedCursor.getCount();
        }
        return returnValue;
    }

    public void setAdapterContext(long medicationID){
        mMedicationID = medicationID;
    }
}
