package com.androidchicken.medminder;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Elisabeth Huhn on 4/21/2017, adapted from MMMedicationCursorAdapter
 * Uses a Cursor with rows from the DB rather than an ArrayList of objects from memory
 *
 * Serves as a liaison between a list RecyclerView and the MedicationManager
 */

class MMMedicationAlertCursorAdapter extends RecyclerView.Adapter<MMMedicationAlertCursorAdapter.MyViewHolder>{

    private Cursor  mMedicationAlertCursor;
   // private long    mPersonID;
    private Context mActivity;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView medName, personName, notifyType, notifyTime;


        MyViewHolder(View v) {
            super(v);

            medName     = v.findViewById(R.id.medAlertMedNickNameInput);
            personName  = v.findViewById(R.id.medAlertPersonInput);
            notifyType  = v.findViewById(R.id.medAlertTypeInput);
            notifyTime  = v.findViewById(R.id.medAlertOverdueTimeInput);

            medName.setFocusable(false);
            personName.setFocusable(false);
            notifyType.setFocusable(false);
            notifyTime.setFocusable(false);

        }

    } //end inner class MyViewHolder

    //Constructor for MMMedicationAdapter
    MMMedicationAlertCursorAdapter(Context activity, Cursor medicationAlertCursor){

        this.mActivity = activity;
        this.mMedicationAlertCursor = medicationAlertCursor;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_medication_alerts, parent,  false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();
        MMMedicationManager      medicationManager      = MMMedicationManager.getInstance();
        MMPersonManager          personManager          = MMPersonManager.getInstance();

        if (mMedicationAlertCursor == null ) return;

        //get the medication indicated
        MMMedicationAlert medicationAlert = medicationAlertManager.
                                    getMedicationAlertFromCursor(mMedicationAlertCursor, position);

        MMMedication      medication      = medicationManager.
                                    getMedicationFromID(medicationAlert.getMedicationID());

        MMPerson          person          = personManager.
                                    getPerson(medicationAlert.getNotifyPersonID());

        holder.medName   .setText(medication.getMedicationNickname().toString());
        holder.personName.setText(person.getNickname().toString());

        int notifyTypeInt = medicationAlert.getNotifyType();

        String type = MMMedicationAlert.sNotifyTextString;
        if (notifyTypeInt == MMMedicationAlert.sNOTIFY_BY_EMAIL) type = MMMedicationAlert.sNotifyEmailString;

        holder.notifyType.setText(type);

        //convert minutes into days/hours/minutes
        int days    = medicationAlert.getOverdueDays();
        int hours   = medicationAlert.getOverdueHours();
        int minutes = medicationAlert.getOverdueMinutes();
        String hmsMask = "%d:%d:%d";
        String overdueTimeString = String.format(hmsMask, days, hours, minutes);

        holder.notifyTime.setText(overdueTimeString);
    }

    Cursor reinitializeCursor(long personID){
        closeCursor();
        if (personID == MMUtilities.ID_DOES_NOT_EXIST)return null;

        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();
        Cursor cursor = medicationAlertManager.getAllMedicationAlertsCursor(personID);



        //Tell the adapter to update the User Display
        notifyDataSetChanged();

        notifyItemRangeChanged(0, getItemCount());


        return cursor;
    }


    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mMedicationAlertCursor != null) {
            returnValue = mMedicationAlertCursor.getCount();
        }
        return returnValue;
    }

    MMMedicationAlert getMedAlertAt(int position){
        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();
        return medicationAlertManager.getMedicationAlertFromCursor(mMedicationAlertCursor, position);
    }

    Cursor getCursor(){return mMedicationAlertCursor;}

    void closeCursor(){
        if (mMedicationAlertCursor != null)mMedicationAlertCursor.close();
    }
}
