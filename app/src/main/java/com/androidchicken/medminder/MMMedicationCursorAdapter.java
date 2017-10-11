package com.androidchicken.medminder;

import android.content.Context;
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
    private Context mContext;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        EditText medicationBrandName, medicationGenericName, medicationNickName;
        EditText medicationDoseNum, medicationDoseAmt,   medicationDoseUnits;

        MyViewHolder(View v) {
            super(v);

            medicationNickName     = (EditText) v.findViewById(R.id.medicationNickNameInput);
            medicationBrandName    = (EditText) v.findViewById(R.id.medicationBrandNameInput);
            medicationGenericName  = (EditText) v.findViewById(R.id.medicationGenericNameInput);
            medicationDoseUnits    = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
            medicationDoseAmt      = (EditText) v.findViewById(R.id.medicationDoseAmountInput);
            medicationDoseNum      = (EditText) v.findViewById(R.id.medicationDoseNumInput);

            medicationNickName.setFocusable(false);
            medicationBrandName.setFocusable(false);
            medicationGenericName.setFocusable(false);
            medicationDoseUnits.setFocusable(false);
            medicationDoseAmt.setFocusable(false);
            medicationDoseNum.setFocusable(false);
        }

    } //end inner class MyViewHolder

    //Constructor for MMMedicationAdapter
    MMMedicationCursorAdapter(Context context, long personID, Cursor medicationCursor){
        this.mContext  = context;
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
        mMedicationCursor = medicationManager.getAllMedicationsCursor(mPersonID);

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
            mMedicationCursor = medicationManager.getAllMedicationsCursor(mPersonID);
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
        holder.medicationNickName.   setBackgroundColor(ContextCompat.getColor(mContext, newColor));
        holder.medicationBrandName.  setBackgroundColor(ContextCompat.getColor(mContext, newColor));
        holder.medicationGenericName.setBackgroundColor(ContextCompat.getColor(mContext, newColor));
        holder.medicationDoseAmt.    setBackgroundColor(ContextCompat.getColor(mContext, newColor));
        holder.medicationDoseUnits.  setBackgroundColor(ContextCompat.getColor(mContext, newColor));
        holder.medicationDoseNum.    setBackgroundColor(ContextCompat.getColor(mContext, newColor));
    }

    Cursor getCursor(){return mMedicationCursor;}

    void closeCursor(){
        if (mMedicationCursor != null)mMedicationCursor.close();
    }
}
