package com.androidchicken.medminder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.List;

/**
 * Created by Elisabeth Huhn on 10/19/2016.
 *
 * Serves as a liason between a list RecyclerView and the MedicationManager
 */

public class MMMedicationAdapter extends RecyclerView.Adapter<MMMedicationAdapter.MyViewHolder>{
    //This list is copied from the MedicationManager.
    // Only the MedicationManager may
    //      alter the contents of the list, or
    //      alter an element of the list
    private List<MMMedication> mMedicationList;

    //implement the ViewHolder as an inner class
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public EditText medicationBrandName, medicationGenericName, medicationNickName;
        public EditText medicationForPersonID;
        public EditText medicationOrder, medicationDoseAmt,   medicationDoseUnits;
        public EditText medicationWhenDue, medicationNum;

        public MyViewHolder(View v) {
            super(v);

            medicationBrandName    = (EditText) v.findViewById(R.id.medicationBrandNameInput);
            medicationGenericName  = (EditText) v.findViewById(R.id.medicationGenericNameInput);
            medicationNickName     = (EditText) v.findViewById(R.id.medicationNickNameInput);
            medicationForPersonID  = (EditText) v.findViewById(R.id.medicationForInput);
            medicationOrder        = (EditText) v.findViewById(R.id.medicationOrderInput);
            medicationDoseUnits    = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
            medicationDoseAmt      = (EditText) v.findViewById(R.id.medicationDoseAmountInput);
            medicationNum          = (EditText) v.findViewById(R.id.medicationDoseNumInput);
            medicationWhenDue      = (EditText) v.findViewById(R.id.medicationDoseDueWhenInput);


        }

    } //end inner class MyViewHolder

    //Constructor for MMMedicationAdapter
    public MMMedicationAdapter(List<MMMedication> medicationList){
        this.mMedicationList = medicationList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_medication, parent,  false);
        return new MyViewHolder(itemView);

    }

    public void removeMedication(int personID, int position) {
        //This list actually lives on the Person object
        //The list is "owned" by the MedicationManager,
        // so call it to remove the item from the list
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        medicationManager.removeMedication(personID, position);


        //this is for the particular item removed
        notifyItemRemoved(position);

        //this line is for all the items above position in the list
        //this line below gives you the animation and also updates the
        //list items after the deleted item
        notifyItemRangeChanged(position, getItemCount());


    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        if (mMedicationList != null ) {
            //get the medication indicated
            MMMedication medication = mMedicationList.get(position);

            holder.medicationBrandName.  setText(medication.getBrandName().toString().trim());
            holder.medicationGenericName.setText(medication.getGenericName().toString().trim());
            holder.medicationNickName.   setText(medication.getMedicationNickname().toString().trim());
            holder.medicationForPersonID.setText(String.valueOf(medication.getForPersonID()).trim());
            holder.medicationOrder.      setText(String.valueOf(medication.getOrder()).trim());
            holder.medicationDoseAmt.    setText(String.valueOf(medication.getDoseAmount()).trim());
            holder.medicationDoseUnits.  setText(medication.getDoseUnits().toString().trim());
            holder.medicationNum.        setText(String.valueOf(medication.getNum()).trim());
            holder.medicationWhenDue.    setText(medication.getWhenDue().toString().trim());

        } else {
            holder.medicationBrandName.  setText("");
            holder.medicationGenericName.setText("");
            holder.medicationNickName.   setText("");
            holder.medicationForPersonID.setText("");
            holder.medicationOrder.      setText("0");
            holder.medicationDoseAmt.    setText("");
            holder.medicationDoseUnits.  setText("0");
            holder.medicationNum.        setText("0");
            holder.medicationWhenDue.    setText("");
        }

    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mMedicationList != null) {
            returnValue = mMedicationList.size();
        }
        return returnValue;
    }

}
