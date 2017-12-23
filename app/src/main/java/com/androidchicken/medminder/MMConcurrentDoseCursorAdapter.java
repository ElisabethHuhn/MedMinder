package com.androidchicken.medminder;

import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 10/19/2016.
 *
 * Serves as a liaison between a list RecyclerView and the ConcurrentDoseManager
 */

class MMConcurrentDoseCursorAdapter extends RecyclerView.Adapter<MMConcurrentDoseCursorAdapter.MyViewHolder>{
    //The group to be listed is collected from a Cursor representing the DB rows
    private Cursor          mConcurrentDoseCursor;
    private long            mPersonID;
    private int             mNumberMeds;
    private MMMainActivity  mActivity;


    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        EditText  doseTime;
        EditText  doseDate;
        ArrayList<TextView> doseMeds = new ArrayList<>();
        TextView  doseID;

        MyViewHolder(View v) {
            super(v);

            //remember the views we know about at coding time
            doseDate = v.findViewById(R.id.doseDateLabel);
            if (doseDate != null){
                doseDate.addTextChangedListener(new MMConDoseTextWatcher(mActivity, doseDate));
            }
            doseTime = v.findViewById(R.id.doseTimeInput);
            if (doseTime != null){
                doseTime.addTextChangedListener(new MMConDoseTextWatcher(mActivity, doseTime));
            }
            doseID   = v.findViewById(R.id.doseIDInvisible);

            doseID.setVisibility(View.GONE);

            //Add views for the medications that are contained in this concurrent dose:
            if (mActivity == null) return;

            LinearLayout layout = v.findViewById(R.id.doseHistoryLine);
            EditText medField;
            int sizeInDp = 2;
            int padding = MMUtilities.getInstance().convertPixelsToDp(mActivity, sizeInDp);
            int last = mNumberMeds;
            int position = 0;
            MMUtilities utilities = MMUtilities.getInstance();

            while (position < last) {
                medField = utilities.createDoseEditText(mActivity, padding);
                if (medField != null) {
                    medField.addTextChangedListener(new MMConDoseTextWatcher(mActivity, medField));

                    doseMeds.add(medField);
                    layout.addView(medField);
                }
                position++;

            }

         }

    } //end inner class MyViewHolder

    //Constructor for MMConcurrentDosesAdapter
    MMConcurrentDoseCursorAdapter(MMMainActivity activity,
                                 long personID,
                                 int numberMeds,
                                 Cursor concurrentDoseCursor){
        this.mPersonID   = personID;
        this.mNumberMeds = numberMeds;
        this.mActivity   = activity;
        this.mConcurrentDoseCursor = concurrentDoseCursor;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_dose_history_horz, parent,false);
        return new MyViewHolder(itemView);

    }

    //remove doesn't work directly for a cursor,
    // must remove from the DB, then re-create the cursor
    void removeItem(int position) {
        if (mConcurrentDoseCursor == null)return;

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();

        //get the concurrent dose to be removed
        MMConcurrentDose concurrentDose =
                concurrentDoseManager.getConcurrentDoseFromCursor(mConcurrentDoseCursor, position);

        //remove the concurrent dose from the DB
        concurrentDoseManager.removeConcurrentDoesFromDB(concurrentDose.getConcurrentDoseID());

       mConcurrentDoseCursor = reinitializeCursor();
    }

    Cursor reinitializeCursor(){
        closeCursor();

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        //Create a new Cursor with the current contents of DB
        long earliestDate = MMSettings.getInstance().getHistoryDate(mActivity);
        mConcurrentDoseCursor = concurrentDoseManager.getAllConcurrentDosesCursor(mPersonID, earliestDate);

        //Tell the RecyclerView to update the User Display
        notifyDataSetChanged();

        //notifyItemRangeChanged(position, getItemCount());
        return mConcurrentDoseCursor;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){

        if (mActivity == null)return;
        //set flag so we will know that it is not the user changing the med amounts
        MMSettings.getInstance().setUserInput(mActivity, false);

        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(mActivity);

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        if (mConcurrentDoseCursor == null){
            long earliestDate = MMSettings.getInstance().getHistoryDate(mActivity);
            mConcurrentDoseCursor =
                    concurrentDoseManager.getAllConcurrentDosesCursor(mPersonID, earliestDate);
            //if there is no history for this person, just return
            if (mConcurrentDoseCursor == null)return;
        }


        //get the row indicated
        MMConcurrentDose concurrentDoses = concurrentDoseManager
                                .getConcurrentDoseFromCursor(mConcurrentDoseCursor, position);
        //and all of it's embedded Doses
        concurrentDoses = concurrentDoseManager.getDosesForCDFromDB(concurrentDoses);

        if (concurrentDoses != null) {
            //Put ID in the invisible text view to hold the DB ID
            holder.doseID.setText(String.valueOf(concurrentDoses.getConcurrentDoseID()));


            //Get the individual doses in the row
            ArrayList<MMDose> doses = concurrentDoses.getDoses();

            if (doses != null) {
                //Convert Date from milliseconds to String
                long startTime = concurrentDoses.getStartTime();

                String startDateLocalString = MMUtilitiesTime.convertTimeMStoString(mActivity,
                                                                                    startTime,
                                                                                    false);
                holder.doseDate.setText(startDateLocalString);

                boolean homeShading = MMSettings.getInstance().isHomeShading(mActivity);

                if (homeShading){
                    if (MMUtilitiesTime.isDayOdd(startTime)){
                        holder.doseDate.setBackgroundColor(
                                ContextCompat.getColor(mActivity, R.color.colorWhite));
                    } else {
                        holder.doseDate.setBackgroundColor(
                                ContextCompat.getColor(mActivity,
                                                            R.color.colorScreenDeletedBackground));
                    }
                } else {
                    holder.doseDate.setBackgroundColor(
                                ContextCompat.getColor(mActivity, R.color.colorWhite));
                }

                String startTimeLocalString = MMUtilitiesTime.convertTimeMStoString(mActivity,
                                                                                    startTime,
                                                                                    true);
                holder.doseTime.setText(startTimeLocalString);
                if (homeShading) {
                    if (MMUtilitiesTime.isDayOdd(startTime)) {

                        holder.doseTime.setBackgroundColor(
                                ContextCompat.getColor(mActivity, R.color.colorWhite));

                    } else {
                        holder.doseTime.setBackgroundColor(
                                ContextCompat.getColor(mActivity,
                                                           R.color.colorScreenDeletedBackground));
                    }
                } else {
                    holder.doseTime.setBackgroundColor(
                                ContextCompat.getColor(mActivity, R.color.colorWhite));
                }

                MMPersonManager personManager = MMPersonManager.getInstance();
                MMPerson person = personManager.getPerson(mPersonID);
                ArrayList<MMMedication> medications = person.getMedications(currentOnly);

                MMDose dose= null;
                int lastMedication  = medications.size();
                int uiPosition   = 0;//The field within the UI ConcDose {0,1,2,3,4,5,6,...}
                int medPosition  = 0;//position within all medications the person takes, but read from the dose e.g.{1,3,5}
                int dosePosition = 0;//the position within doses taken at this time e.g.{0,1,2,3}

                //so in our example:
                // uiPosition 0 was not taken, so it has no dose. Put 0 in UI field
                // uiPosition 1 corresponds to medPosition 1, and is recorded in dosePosition 1
                // uiPosition 2 was not taken, so it has no dose. Put 0 in UI field
                // uiPosition 3 corresponds to medPosition 3, and is recorded in dosePosition 2
                // uiPosition 4 was not taken, so it has no dose. Put 0 in UI field
                // uiPosition 5 corresponds to medPosition 5, and is recorded in dosePosition 3
                // uiPosition 6 was not taken, so it has no dose. Put 0 in UI field
                //

                //If the person has not taken all medications at this time, then
                //   there will not be a dose record for all the medications
                //   only those that were actually taken
                TextView textField;
                MMMedication medication;
                boolean medIsCurrent; //indicator from the medication about whether it has been deleted

                while (uiPosition < lastMedication){
                    if (dosePosition < doses.size()){
                        //There are still doses recorded, find out the position of the next one
                        dose = doses.get(dosePosition);
                        medPosition = dose.getPositionWithinConcDose();
                    } else {
                        //There are no more doses to be taken, print zero's in the rest of the fields
                        medPosition = lastMedication+1;
                    }

                    //-----------------------------------------
                    //print zeros in positions where no dose was taken,
                    // but don't exceed the number of total medications
                    // as medPosition may be used as flag (see above where it is set to last+1)
                    while ((uiPosition < medPosition) && (uiPosition < lastMedication)){
                        medication = medications.get(uiPosition);
                        medIsCurrent = medication.isCurrentlyTaken();

                        textField = holder.doseMeds.get(uiPosition);
                        textField.setText("0");
                        if (medIsCurrent){
                            textField.setBackgroundColor(
                                    ContextCompat.getColor(mActivity, R.color.colorWhite));
                        } else {
                            //Gray the background on non-current meds
                            textField.setBackgroundColor(
                                    ContextCompat.getColor(mActivity,R.color.colorGray));
                        }

                        uiPosition++;
                    }

                    //----------------------------------------------------------
                    if ((uiPosition == medPosition) && (dose != null)){
                        medication = medications.get(uiPosition);
                        medIsCurrent = medication.isCurrentlyTaken();

                        textField = holder.doseMeds.get(uiPosition);
                        if (medIsCurrent){
                            int amtTaken = dose.getAmountTaken();
                            textField.setText(String.valueOf(amtTaken));

                             if (amtTaken > 0){
                                textField.setBackgroundColor(
                                        ContextCompat.getColor(mActivity, R.color.colorLightPink));
                            } else {
                                textField.setBackgroundColor(
                                        ContextCompat.getColor(mActivity, R.color.colorWhite));
                            }
                        } else {
                            textField.setBackgroundColor(
                                        ContextCompat.getColor(mActivity, R.color.colorGray));
                        }
                    }
                    uiPosition++;
                    dosePosition++;
                    medPosition++;
                }
            }
        }
    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mConcurrentDoseCursor != null) {
            returnValue = mConcurrentDoseCursor.getCount();
        }
        return returnValue;
    }

    MMConcurrentDose getConcurrentDoseAt(int position){
        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        return concurrentDoseManager.getConcurrentDoseFromCursor(mConcurrentDoseCursor, position);
    }

    Cursor getCursor(){return mConcurrentDoseCursor;}

    void closeCursor(){
        if (mConcurrentDoseCursor != null)mConcurrentDoseCursor.close();
    }

}
