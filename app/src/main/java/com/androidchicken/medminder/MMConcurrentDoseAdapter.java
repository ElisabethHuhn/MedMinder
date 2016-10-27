package com.androidchicken.medminder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elisabethhuhn on 10/19/2016.
 *
 * Serves as a liason between a list RecyclerView and the ConcurrentDoseManager
 */

public class MMConcurrentDoseAdapter extends RecyclerView.Adapter<MMConcurrentDoseAdapter.MyViewHolder>{
    //This list is copied from the ConcurrentDoseManager.
    // Only the Manager may
    //      alter the contents of the list, or
    //      alter an element of the list
    private List<MMConcurrentDoses> mMMConcurrentDosesList;

    //implement the ViewHolder as an inner class
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public EditText doseTime;
        public EditText doseMed1, doseMed2, doseMed3, doseMed4, doseMed5;


        public MyViewHolder(View v) {
            super(v);
            doseTime  = (EditText) v.findViewById(R.id.doseTimeInput);
            doseMed1  = (EditText) v.findViewById(R.id.doseMed1Input);
            doseMed2  = (EditText) v.findViewById(R.id.doseMed2Input);
            doseMed3  = (EditText) v.findViewById(R.id.doseMed3Input);
            doseMed4  = (EditText) v.findViewById(R.id.doseMed4Input);
            doseMed5  = (EditText) v.findViewById(R.id.doseMed5Input);
        }

    } //end inner class MyViewHolder

    //Constructor for MMConcurrentDosesAdapter
    public MMConcurrentDoseAdapter(List<MMConcurrentDoses> concurrentDosesList){
        this.mMMConcurrentDosesList = concurrentDosesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_dose_history, parent,  false);
        return new MyViewHolder(itemView);

    }

    public void removeItem(int position) {
        //This list is used locally as well as in the person container,
        //The list is "owned" by the PersonManager, so call it to remove the item
        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        concurrentDoseManager.removeConcurrentDose(position);
        //mMMConcurrentDosesList.remove(position);

        //this is for the particulary item removed
        notifyItemRemoved(position);

        //this line is for all the items above position in the list
        //this line below gives you the animation and also updates the
        //list items after the deleted item
        notifyItemRangeChanged(position, getItemCount());


    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        holder.doseTime.setText("0");
        holder.doseMed1.setText("0");
        holder.doseMed2.setText("0");
        holder.doseMed3.setText("0");
        holder.doseMed4.setText("0");
        holder.doseMed5.setText("0");
        if (mMMConcurrentDosesList != null ) {
            //get the row indicated
            MMConcurrentDoses concurrentDoses = mMMConcurrentDosesList.get(position);

            if (concurrentDoses != null) {
                //Get the individual doses in the row
                ArrayList<MMDose> doses = concurrentDoses.getDoses();

                holder.doseTime.setText(String.valueOf(concurrentDoses.getStartTime()));
                holder.doseMed1.setText(String.valueOf(doses.get(1).getAmountTaken()));
                holder.doseMed2.setText(String.valueOf(doses.get(2).getAmountTaken()));
                holder.doseMed3.setText(String.valueOf(doses.get(3).getAmountTaken()));
                holder.doseMed4.setText(String.valueOf(doses.get(4).getAmountTaken()));
                holder.doseMed5.setText(String.valueOf(doses.get(5).getAmountTaken()));
            }

        }
    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mMMConcurrentDosesList != null) {
            returnValue = mMMConcurrentDosesList.size();
        }
        return returnValue;
    }

}
