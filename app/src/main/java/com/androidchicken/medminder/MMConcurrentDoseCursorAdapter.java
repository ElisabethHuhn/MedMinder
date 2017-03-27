package com.androidchicken.medminder;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 10/19/2016.
 *
 * Serves as a liaison between a list RecyclerView and the ConcurrentDoseManager
 */

public class MMConcurrentDoseCursorAdapter extends RecyclerView.Adapter<MMConcurrentDoseCursorAdapter.MyViewHolder>{
    //The group to be listed is collected from a Cursor representing the DB rows
    private Cursor  mConcurrentDoseCursor;
    private long    mPersonID;
    private int     mNumberMeds;
    private Context mContext;


    //implement the ViewHolder as an inner class
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public EditText  doseTime;
        public TextView  doseDate;
        public ArrayList<EditText> doseMeds = new ArrayList<>();


        public MyViewHolder(View v) {
            super(v);

            //remember the views we know about at coding time
            doseDate = (TextView) v.findViewById(R.id.doseDateLabel);
            doseTime = (EditText) v.findViewById(R.id.doseTimeInput);

            //Add views for the medications that are contained in this concurrent dose:
            if (mContext == null)return;

            LinearLayout layout = (LinearLayout) v.findViewById(R.id.doseHistoryLine);
            EditText edtView;
            int sizeInDp = 2;
            int padding = MMUtilities.convertPixelsToDp(mContext, sizeInDp);
            int last = mNumberMeds;
            int position = 0;
            while (position < last){
                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(0,//width
                                                    ViewGroup.LayoutParams.WRAP_CONTENT);//height
                lp.weight = 3f;
                //lp.gravity = Gravity.CENTER;
                lp.setMarginEnd(padding);

                edtView = new EditText(mContext);
                edtView.setHint("0");
                edtView.setInputType(InputType.TYPE_CLASS_NUMBER);
                edtView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                edtView.setLayoutParams(lp);
                edtView.setPadding(0,0,padding,0);
                edtView.setGravity(Gravity.CENTER);
                edtView.setTextColor      (ContextCompat.getColor(mContext,R.color.colorTextBlack));
                edtView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorInputBackground));

                //add listener
                edtView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        return false;
                    }
                });

                doseMeds.add(edtView);
                layout.addView(edtView);
                position++;

            }
        }

    } //end inner class MyViewHolder

    //Constructor for MMConcurrentDosesAdapter
    public MMConcurrentDoseCursorAdapter(Context context,
                                         long personID,
                                         int numberMeds,
                                         Cursor concurrentDoseCursor){
        this.mPersonID   = personID;
        this.mNumberMeds = numberMeds;
        this.mContext    = context;
        this.mConcurrentDoseCursor = concurrentDoseCursor;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_dose_history, parent,false);
        return new MyViewHolder(itemView);

    }

    //remove doesn't work directly for a cursor,
    // must remove from the DB, then re-create the cursor
    public void removeItem(int position) {
        if (mConcurrentDoseCursor == null)return;

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();

        //get the concurrent dose to be removed
        MMConcurrentDose concurrentDose =
                concurrentDoseManager.getConcurrentDoseFromCursor(mConcurrentDoseCursor, position);

        //remove the concurrent dose from the DB
        concurrentDoseManager.removeConcurrentDoesFromDB(concurrentDose.getConcurrentDoseID());

       mConcurrentDoseCursor = reinitializeCursor();
    }

    public Cursor reinitializeCursor(){
        closeCursor();

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        //Create a new Cursor with the current contents of DB
        mConcurrentDoseCursor = concurrentDoseManager.getAllConcurrentDosesCursor(mPersonID);

        //Tell the RecyclerView to update the User Display
        notifyDataSetChanged();

        //notifyItemRangeChanged(position, getItemCount());
        return mConcurrentDoseCursor;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){

        if (mContext == null)return;

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        if (mConcurrentDoseCursor == null){
            mConcurrentDoseCursor = concurrentDoseManager.getAllConcurrentDosesCursor(mPersonID);
            //if there is no history for this person, just return
            if (mConcurrentDoseCursor == null)return;
        }


        //get the row indicated
        MMConcurrentDose concurrentDoses = concurrentDoseManager
                                .getConcurrentDoseFromCursor(mConcurrentDoseCursor, position);
        //and all of it's embedded Doses
        // TODO: 1/27/2017 These are not added to the permanent in memory list, so are transient
        // TODO: 2/3/2017 It might be better to create memory objects and save them rather than recreate from DB each time
        concurrentDoses = concurrentDoseManager.getDosesForCDFromDB(concurrentDoses);

        if (concurrentDoses != null) {
            //Get the individual doses in the row
            ArrayList<MMDose> doses = concurrentDoses.getDoses();

            if (doses != null) {
/*
                Date date = new Date(concurrentDoses.getStartTime());
                String stringDate = DateFormat.getDateInstance().format(date);
                //holder.doseDate.setText(stringDate);
                holder.doseDate.setText(MMUtilities.getTimeString(concurrentDoses.getStartTime()));

                stringDate = DateFormat.getTimeInstance().format(date);
                holder.doseTime.setText(stringDate);
*/
                holder.doseDate.setText(MMUtilities.getDateString(concurrentDoses.getStartTime()));
                holder.doseTime.setText(MMUtilities.getTimeString(concurrentDoses.getStartTime()));

                MMPersonManager personManager = MMPersonManager.getInstance();
                MMPerson person = personManager.getPerson(mPersonID);

                MMDose dose= null;
                int lastMedication  = person.getMedications().size();
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
                EditText editText;
                while (uiPosition < lastMedication){
                    if (dosePosition < doses.size()){
                        //There are still doses recorded, find out the position of the next one
                        dose = doses.get(dosePosition);
                        medPosition = dose.getPositionWithinConcDose();
                    } else {
                        //There are no more doses to be taken, print zero's in the rest of the fields
                        medPosition = lastMedication+1;
                    }
                    //print zeros in positions where no dose was taken,
                    // but don't exceed the number of total medications
                    // as medPosition may be used as flag (see above where it is set to last+1)
                    while ((uiPosition < medPosition) && (uiPosition < lastMedication)){
                        editText = holder.doseMeds.get(uiPosition);
                        editText.setText("0");
                        uiPosition++;
                    }

                    if ((uiPosition == medPosition) && (dose != null)){
                        editText = holder.doseMeds.get(uiPosition);
                        editText.setText(String.valueOf(dose.getAmountTaken()));
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


    public MMConcurrentDose getConcurrentDoseAt(int position){
        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        return concurrentDoseManager.getConcurrentDoseFromCursor(mConcurrentDoseCursor, position);
    }


    public Cursor getCursor(){return mConcurrentDoseCursor;}

    public void closeCursor(){
        if (mConcurrentDoseCursor != null)mConcurrentDoseCursor.close();
    }

}
