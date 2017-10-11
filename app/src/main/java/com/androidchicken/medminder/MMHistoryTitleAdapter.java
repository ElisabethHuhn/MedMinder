package com.androidchicken.medminder;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 10/19/2016.
 *
 * Serves as a liaison between a list RecyclerView and the list of medications a patient takes
 */

class MMHistoryTitleAdapter extends RecyclerView.Adapter<MMHistoryTitleAdapter.MyViewHolder>{
    //The group to be listed is collected from a Cursor representing the DB rows
    private ArrayList<MMMedication> mMedications;
    private Context mActivity;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView  positionNumber, medicationName;

        MyViewHolder(View v) {
            super(v);

            //remember the views we know about at coding time
            positionNumber = (TextView) v.findViewById(R.id.titlePositionalIndicator);
            medicationName = (TextView) v.findViewById(R.id.titleMedicationName);
        }

    } //end inner class MyViewHolder

    //Constructor
    MMHistoryTitleAdapter(ArrayList<MMMedication> medications, Context activity){
        this.mMedications = medications;
        mActivity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_history_title, parent,false);

        return new MyViewHolder(itemView);

    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){

        MMMedication medication = mMedications.get(position);
        //programmers count from zero, but users count from 1
        holder.positionNumber.setText(String.valueOf(position+1));
        holder.medicationName.setText(medication.getMedicationNickname());

        int color = R.color.colorGray;
        if (medication.isCurrentlyTaken()){
            color = R.color.colorInputBackground;
        }
        holder.medicationName.setBackgroundColor(ContextCompat.getColor(mActivity, color));
    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mMedications != null) {
            returnValue = mMedications.size();
        }
        return returnValue;
    }


}
