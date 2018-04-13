package com.androidchicken.medminder;

import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 12/22/2017.
 * Modified the TextWatcher so can get at the view it is listening on
 */

public class MMConDoseTextWatcher implements TextWatcher {
    private MMMainActivity mActivity;
    private EditText       mEditText;

    MMConDoseTextWatcher(MMMainActivity activity, EditText editText) {
        mActivity = activity;
        mEditText = editText;
    }

    private EditText getEditTextView() {
        return mEditText;
    }

    private ViewParent getParentView() {
        return mEditText.getParent();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        //This tells you that text is about to change.
        // Starting at character "start", the next "count" characters
        // will be changed with "after" number of characters

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        //This tells you where the text has changed
        //Starting at character "start", the "before" number of characters
        // has been replaced with "count" number of characters
        if (MMSettings.getInstance().isUserInput(mActivity)) {
            EditText medAmtView = getEditTextView();
            if (medAmtView == null)return;

            LinearLayout parent = (LinearLayout)getParentView();
            if (parent == null)return;

            EditText dateView = (EditText)parent.getChildAt(0);
            if (dateView == null)return;
            EditText timeView = (EditText)parent.getChildAt(1);
            if (timeView == null)return;
            TextView idView   = (TextView) parent.getChildAt(2);
            if (idView == null)  return;
            String idString   =  idView.getText().toString();
            if (idString.isEmpty())return;

            long cDoseID = Long.parseLong(idString);

            //convert date and time to milliseconds in epoc
            String   dateString = dateView.getText().toString().trim();
            String   timeString =  timeView.getText().toString().trim();

            //convert the local string to local ms
            //first convert the date
            long dateMs = MMUtilitiesTime.convertStringToTimeMs(mActivity,
                                                                dateString,
                                                                false);
            //set to convert time string
            long timeMs = MMUtilitiesTime.convertStringToTimeMs(mActivity,
                                                                timeString,
                                                                true);

            //if there was an error parsing either the date or the time, don't try save
            if ((dateMs == 0) || (timeMs == 0)){
                //MMUtilities.getInstance().errorHandler(mActivity, R.string.error_parsing_date_time);
                //MMUtilities.getInstance().errorHandler(mActivity, R.string.change_not_saved);
                return;
            }

            //add together to get the new time for the concurrent dose
            long cdTimeMs = MMUtilitiesTime.getTotalTime(dateMs, timeMs);

            //Get the selected concurrent dose from the DB
            MMConcurrentDose cDose = MMConcurrentDoseManager.getInstance().getConcurrentDose(cDoseID);
            if (cDose == null){
                MMUtilities.getInstance().errorHandler(mActivity, R.string.error_updating_cdose);
                return;
            }
            MMDose           dose;

            //store the time in the concurrent dose
            cDose.setStartTime(cdTimeMs);

            //Now store the new medication amounts in individual doses
            int last = parent.getChildCount();
            int viewPosition = 3; //remember the date, time, and id fields
            int dosePosition = 0; //There may be doses missing as they had zero amt initially
            String amtString = null;
            int amt;
            //Get the old dose amounts from the DB
            ArrayList<MMDose> doses = cDose.getDoses();

            int medPosition;
            int backgroundColor;

            while (viewPosition < last){
                medPosition = viewPosition - 3;//3 extra fields for date, time, id

                //First get the amount for this med from the UI
                medAmtView = (EditText)parent.getChildAt(viewPosition);
                if (medAmtView != null) {
                    amtString = medAmtView.getText().toString().trim();
                }

                amt = 0;
                if ((amtString != null) && (!amtString.isEmpty())){
                    amt = Integer.valueOf(amtString);
                }
                //set screen color according to dose amount
                backgroundColor = ContextCompat.getColor(mActivity, R.color.colorInputBackground);
                if (amt > 0){
                    backgroundColor = ContextCompat.getColor(mActivity, R.color.colorLightPink);
                }
                medAmtView.setBackgroundColor(backgroundColor);

                //Now, update the dose object corresponding to this medication, if one exists
                //  if the original amount was zero, it might not exist
                dose = doses.get(dosePosition);

                if (dose.getPositionWithinConcDose() > medPosition){
                    //if here, there was not a dose for this medication
                    //But only need to create one if the amount is greater than zero
                    if (amt > 0){
                        MMPerson patient = mActivity.getPerson();
                        if (patient == null)return;

                        MMMedication medication =
                                              patient.getMedicationAt(false, medPosition);
                        long medID = medication.getMedicationID();

                        dose = new MMDose(  medID,
                                            patient.getPersonID(),
                                            cDoseID,
                                            medPosition,
                                            cdTimeMs,
                                            amt);

                        //write out the new dose to the DB
                        //DB update happens when concurrent doses instance written out to DB
                        //MMDatabaseManager.getInstance().addDose(dose);

                        //add the new dose to the concurrent dose container
                        doses.add(dose);
                        //force the concurrent dose to pull from the DB on next access of doses.
                        cDose.setDosesChanged();
                    }
                } else {
                    //if here, want to update the dose with the new amount and write to the DB
                    dose.setAmountTaken(amt);
                    //  assure the time at the concurrent dose level matches the time at the dose level
                    dose.setTimeTaken(cdTimeMs);

                    //DB update happens when concurrent doses instance written out to DB



                    dosePosition++;
                }
                viewPosition++;
            }

            //write everything back out to the DB
            //Update the concurrent Dose and it's contained doses in the DB
            MMConcurrentDoseManager.getInstance().addConcurrentDose(cDose, true);

        }

    }

    @Override
    public void afterTextChanged(Editable editable) {
        //This tells you that somewhere within editable, it's text has changed

    }
}
