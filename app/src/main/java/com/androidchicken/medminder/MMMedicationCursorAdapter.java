package com.androidchicken.medminder;

import android.database.Cursor;
import android.support.v4.content.ContextCompat;
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

class MMMedicationCursorAdapter extends RecyclerView.Adapter<MMMedicationCursorAdapter.MyViewHolder>{

    private Cursor  mMedicationCursor;
    private long    mPersonID;
    private MMMainActivity mActivity;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        EditText medicationBrandName, medicationGenericName, medicationNickName;
        EditText medicationDoseNum, medicationDoseAmt,   medicationDoseUnits;

        MyViewHolder(View v) {
            super(v);

            medicationNickName     = v.findViewById(R.id.medicationNickNameInput);
            medicationBrandName    = v.findViewById(R.id.medicationBrandNameInput);
            medicationGenericName  = v.findViewById(R.id.medicationGenericNameInput);
            medicationDoseUnits    = v.findViewById(R.id.medicationDoseUnitsInput);
            medicationDoseAmt      = v.findViewById(R.id.medicationDoseAmountInput);
            medicationDoseNum      = v.findViewById(R.id.medicationDoseNumInput);

            medicationNickName.setFocusable(false);
            medicationBrandName.setFocusable(false);
            medicationGenericName.setFocusable(false);
            medicationDoseUnits.setFocusable(false);
            medicationDoseAmt.setFocusable(false);
            medicationDoseNum.setFocusable(false);
        }

    } //end inner class MyViewHolder

    //Constructor for MMMedicationAdapter
    MMMedicationCursorAdapter(MMMainActivity activity, long personID, Cursor medicationCursor){
        this.mActivity = activity;
        this.mPersonID = personID;
        this.mMedicationCursor = medicationCursor;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_medication, parent,  false);
        return new MyViewHolder(itemView);
    }


    Cursor reinitializeCursor(long personID){
        closeCursor();

        mPersonID = personID;

        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        //Create a new Cursor with the current contents of DB
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST) return null;
        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(mActivity);
        mMedicationCursor = medicationManager.getAllMedicationsCursor(mPersonID, currentOnly);

        //Tell the RecyclerView to update the User Display
        notifyDataSetChanged();
        //notifyItemRangeChanged(position, getItemCount());
        
        return mMedicationCursor;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();

        if (mMedicationCursor == null ) {

            if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST) return;
            boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(mActivity);
            mMedicationCursor = medicationManager.getAllMedicationsCursor(mPersonID, currentOnly);
            if (mMedicationCursor == null) {
                holder.medicationNickName.setText("");
                holder.medicationBrandName.setText("");
                holder.medicationGenericName.setText("");
                holder.medicationDoseAmt.setText("");
                holder.medicationDoseUnits.setText("0");
                holder.medicationDoseNum.setText("0");

                setBackColor(holder, R.color.colorGray);
                return;
            }
        }
        //get the medication indicated
        MMMedication medication =
                medicationManager.getMedicationFromCursor(mMedicationCursor, position);

        holder.medicationNickName.   setText(medication.getMedicationNickname().toString().trim());
        holder.medicationBrandName.  setText(medication.getBrandName().toString().trim());
        holder.medicationGenericName.setText(medication.getGenericName().toString().trim());
        holder.medicationDoseAmt.    setText(String.valueOf(medication.getDoseAmount()).trim());
        holder.medicationDoseUnits.  setText(medication.getDoseUnits().toString().trim());
        holder.medicationDoseNum.    setText(String.valueOf(medication.getDoseNumPerDay()).trim());

        if (medication.isCurrentlyTaken()){
            setBackColor(holder, R.color.colorWhite);
        } else {
            setBackColor(holder, R.color.colorScreenDeletedBackground);
        }
    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mMedicationCursor != null) {
            returnValue = mMedicationCursor.getCount();
        }
        return returnValue;
    }



    private void setBackColor(MyViewHolder holder, int newColor){
        holder.medicationNickName.   setBackgroundColor(ContextCompat.getColor(mActivity, newColor));
        holder.medicationBrandName.  setBackgroundColor(ContextCompat.getColor(mActivity, newColor));
        holder.medicationGenericName.setBackgroundColor(ContextCompat.getColor(mActivity, newColor));
        holder.medicationDoseAmt.    setBackgroundColor(ContextCompat.getColor(mActivity, newColor));
        holder.medicationDoseUnits.  setBackgroundColor(ContextCompat.getColor(mActivity, newColor));
        holder.medicationDoseNum.    setBackgroundColor(ContextCompat.getColor(mActivity, newColor));
    }

    Cursor getCursor(){return mMedicationCursor;}

    void closeCursor(){
        if (mMedicationCursor != null)mMedicationCursor.close();
    }
}
