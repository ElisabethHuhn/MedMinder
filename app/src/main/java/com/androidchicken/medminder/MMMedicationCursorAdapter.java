package com.androidchicken.medminder;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by Elisabeth Huhn on 2/24/2017, adapted from MMMedicationAdapter
 * Uses a Cursor with rows from the DB rather than an ArrayList of objects from memory
 *
 * Serves as a liaison between a list RecyclerView and the MedicationManager
 */

public class MMMedicationCursorAdapter extends RecyclerView.Adapter<MMMedicationCursorAdapter.MyViewHolder>{

    private Cursor mMedicationCursor;
    private long mPersonID;

    //implement the ViewHolder as an inner class
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public EditText medicationBrandName, medicationGenericName, medicationNickName;
        public EditText medicationForPersonID;
        public EditText medicationDoseStrategy, medicationDoseAmt,   medicationDoseUnits;
        public EditText medicationDoseNum;

        public MyViewHolder(View v) {
            super(v);

            medicationForPersonID  = (EditText) v.findViewById(R.id.medicationForPersonInput);
            medicationNickName     = (EditText) v.findViewById(R.id.medicationNickNameInput);
            medicationBrandName    = (EditText) v.findViewById(R.id.medicationBrandNameInput);
            medicationGenericName  = (EditText) v.findViewById(R.id.medicationGenericNameInput);
            medicationDoseStrategy = (EditText) v.findViewById(R.id.medicationDoseStrategyInput);
            medicationDoseUnits    = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
            medicationDoseAmt      = (EditText) v.findViewById(R.id.medicationDoseAmountInput);
            medicationDoseNum      = (EditText) v.findViewById(R.id.medicationDoseNumInput);
        }

    } //end inner class MyViewHolder

    //Constructor for MMMedicationAdapter
    public MMMedicationCursorAdapter(Cursor medicationCursor){
        this.mMedicationCursor = medicationCursor;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_medication, parent,  false);
        return new MyViewHolder(itemView);
    }

    public void removeMedication(int position) {
        if (mMedicationCursor == null)return;

        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST)return;

        MMMedicationManager medicationManager = MMMedicationManager.getInstance();

        //get the row indicated which is the person to be removed
        MMMedication medication = 
                medicationManager.getMedicationFromCursor(mMedicationCursor, position);
        if (medication == null)return;

        //remove the medication from the DB
        medicationManager.removeMedicationFromDB(medication.getMedicationID());
        // TODO: 3/8/2017 May need to worry about cascading schedules from the medicationID

        mMedicationCursor = reinitializeCursor();
    }

    public Cursor reinitializeCursor(){
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        //Create a new Cursor with the current contents of DB
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST) return null;
        mMedicationCursor = medicationManager.getAllMedicationsCursor(mPersonID);

        //Tell the RecyclerView to update the User Display
        notifyDataSetChanged();;
        //notifyItemRangeChanged(position, getItemCount());
        
        return mMedicationCursor;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();

        if (mMedicationCursor == null ) {

            if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST) return;
            mMedicationCursor = medicationManager.getAllMedicationsCursor(mPersonID);
            if (mMedicationCursor == null) {
                holder.medicationForPersonID.setText("");
                holder.medicationNickName.setText("");
                holder.medicationBrandName.setText("");
                holder.medicationGenericName.setText("");
                holder.medicationDoseStrategy.setText("0");
                holder.medicationDoseAmt.setText("");
                holder.medicationDoseUnits.setText("0");
                holder.medicationDoseNum.setText("0");
                return;
            }
        }
        //get the medication indicated
        MMMedication medication =
                medicationManager.getMedicationFromCursor(mMedicationCursor, position);

        holder.medicationForPersonID.setText(String.valueOf(medication.getForPersonID()).trim());
        holder.medicationNickName.   setText(medication.getMedicationNickname().toString().trim());
        holder.medicationBrandName.  setText(medication.getBrandName().toString().trim());
        holder.medicationGenericName.setText(medication.getGenericName().toString().trim());
        holder.medicationDoseStrategy.setText(String.valueOf(medication.getDoseStrategy()).trim());
        holder.medicationDoseAmt.    setText(String.valueOf(medication.getDoseAmount()).trim());
        holder.medicationDoseUnits.  setText(medication.getDoseUnits().toString().trim());
        holder.medicationDoseNum.    setText(String.valueOf(medication.getDoseNumPerDay()).trim());
    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mMedicationCursor != null) {
            returnValue = mMedicationCursor.getCount();
        }
        return returnValue;
    }

    public void setAdapterContext(long personID){
        mPersonID = personID;
    }
    public Cursor getCursor(){return mMedicationCursor;}
}
