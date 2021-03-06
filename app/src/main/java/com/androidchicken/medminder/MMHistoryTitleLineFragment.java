package com.androidchicken.medminder;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Identifies the medications by position in the home fragment history
 */
public class MMHistoryTitleLineFragment extends Fragment {

    private static final String TAG = "MMHistoryTitleLineFrag";

    //**********************************************/
    /*          Member Variables                   */
    /*    These variables will need to survive     */
    /*          configuration change               */
    //**********************************************/



    //These do not need to be restored over configuration change.
    // They will be recreated in wireWidgets()

    private ArrayList<MMMedication> mMedications;


    //**********************************************/
    /*          Static Methods                     */
    //**********************************************/


    //**********************************************/
    /*          Constructor                        */
    //**********************************************/
    public MMHistoryTitleLineFragment() {
    }

    //**********************************************/
    /*          Lifecycle Methods                  */
    //**********************************************/

    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the personID this screen refers to
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //Initialize the DB if necessary
        try {
            //initialize the database
            MMDatabaseManager.getInstance(getActivity());
        }catch (Exception e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }


    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {


        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return null;

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history_title, container, false);


        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);
        initializeRecyclerView(v);
        initializeUI(v);
        activity.handleFabVisibility();

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //set the title bar subtitle
        activity.setMMSubtitle(R.string.title_history_title_line);

        //Set the FAB invisible
        activity.hideFAB();

    }

    //*************************************************************/
    /*  Convenience Methods for accessing things on the Activity  */
    //*************************************************************/

    private long     getPatientID(){

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return MMUtilities.ID_DOES_NOT_EXIST;

        return activity.getPatientID();}
    private MMPerson getPerson()    {
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return null;
        return activity.getPerson();
    }


    //**********************************************/
    /*   Initialization Methods                    */
    //**********************************************/
    private void wireWidgets(View v){

        //Fill the sample ConcurrentDose title line with the position Indicators
        if (getPatientID() == MMUtilities.ID_DOES_NOT_EXIST)return;

        if (getPerson() == null)return;

        MMMainActivity activity = (MMMainActivity)getActivity();
        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
        mMedications = getPerson().getMedications(currentOnly);

        int last = mMedications.size();
        if (last == 0)return;

        int sizeInDp = MMHomeFragment.sPaddingBetweenViews;
        addDateTimeFieldsToView(v, sizeInDp);

        int position = 0;
        while (position < last) {
            MMMedication medication = mMedications.get(position);
            if (medication != null)  {
                boolean isGrayed = !(medication.isCurrentlyTaken());
                addMedPositionToView( v, position, sizeInDp, isGrayed);
            }
            position++;
        }

    }

    private void addDateTimeFieldsToView(View v, int sizeInDp){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        int padding = MMUtilities.getInstance().convertPixelsToDp(activity, sizeInDp);

        LinearLayout layout = v.findViewById(R.id.medHistoryLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0,//width
                ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 4f;
        //lp.gravity = Gravity.CENTER;//set below too
        lp.setMarginEnd(padding);

        EditText mTimeInput = new EditText(activity);

        mTimeInput.setInputType(InputType.TYPE_CLASS_TEXT);
        mTimeInput.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mTimeInput.setLayoutParams(lp);
        mTimeInput.setPadding(0,0,padding,0);
        mTimeInput.setGravity(Gravity.CENTER);
        mTimeInput.setTextColor      (ContextCompat.getColor(activity,R.color.colorTextBlack));
        mTimeInput.setBackgroundColor(ContextCompat.getColor(activity,R.color.colorInputBackground));
        mTimeInput.setFocusable(false);


        String timeString = MMUtilitiesTime.getTimeString(activity);

        mTimeInput.setText(timeString);

        layout.addView(mTimeInput);

    }

    private void addMedPositionToView(View v, int position, int sizeInDp, boolean isGrayed){

        EditText edtView;

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        int padding = MMUtilities.getInstance().convertPixelsToDp(activity, sizeInDp);

        //
        //add EditText to the dose layout
        //
        LinearLayout layout = v.findViewById(R.id.medHistoryLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0,//width
                ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 1f;

        lp.setMarginEnd(padding);


        edtView = new EditText(activity);
        edtView.setFreezesText(true);
        edtView.setHint("0");
        edtView.setInputType(InputType.TYPE_CLASS_TEXT);
        edtView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edtView.setLayoutParams(lp);
        edtView.setPadding(0,0,padding,0);
        edtView.setGravity(Gravity.CENTER);
        edtView.setTextColor      (ContextCompat.getColor(activity,R.color.colorTextBlack));
        int color = R.color.colorInputBackground;
        if (isGrayed){
            color = R.color.colorGray;
        }
        edtView.setBackgroundColor(ContextCompat.getColor(activity, color));

        edtView.setFocusable(false);

        //users don't count from zero but programmers do. So increment the zero, etc.
        edtView.setText(String.valueOf(position+1));

        layout.addView(edtView);

    }

    private void initializeRecyclerView(View v){
            /*
             * The steps for doing recycler view in onCreateView() of a fragment are:
             * 1) inflate the .xml
             *
             * the special recycler view stuff is:
             * 2) get and store a reference to the recycler view widget that you created in xml
             * 3) create and assign a layout manager to the recycler view
             * 4) assure that there is data for the recycler view to show.
             * 5) use the data to create and set an adapter in the recycler view
             * 6) create and set an item animator (if desired)
             * 7) create and set a line item decorator
             * 8) add event listeners to the recycler view
             *
             * 9) return the view
             */
        //1) Inflate the layout for this fragment
        //      done in the caller


        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //2) find and remember the RecyclerView
        RecyclerView recyclerView = getRecyclerView(v);

        //3) create and assign a layout manager to the recycler view
/*
        boolean reverseLayout = true;

        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(activity,
                                                                        LinearLayoutManager.VERTICAL,
                                                                        reverseLayout);
 */
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(mLayoutManager);

        //4) the list of Medication Instances was created in wireWidgets and is in mMedications



        //5) Use the data to Create and set out medicationTitle Adapter
        //     even though we're giving the Adapter the list,
        //     The list is not maintained. This fragment is write only
        MMHistoryTitleAdapter adapter = new MMHistoryTitleAdapter(mMedications, activity);

        recyclerView.setAdapter(adapter);

        //6) create and set the itemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //7) create and add the item decorator
        recyclerView.addItemDecoration(new DividerItemDecoration(activity,
                                                                   DividerItemDecoration.VERTICAL));


        //8) add event listeners to the recycler view
        recyclerView.addOnItemTouchListener(
            new MMHomeFragment.RecyclerTouchListener(activity,
                                                     recyclerView,
                                                     new MMHomeFragment.ClickListener() {

                @Override
                public void onClick(View view, int position) {
                    onSelect(position);
                }

                @Override
                public void onLongClick(View view, int position) {

                }
            }));

    }

    private void initializeUI(View v){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;
        //determine if a person is yet associated with the fragment
        if (getPatientID() != MMUtilities.ID_DOES_NOT_EXIST){
            //if there is a person corresponding to the patientID, put the name up on the screen
            MMPerson person = getPerson();

            if (person != null) {
                TextView patientNickName = v.findViewById(R.id.patientNickNameLabel);
                patientNickName.setText(person.getNickname().toString().trim());

                if (person.isCurrentlyExists()){
                    v.setBackgroundColor(ContextCompat.
                                        getColor(activity, R.color.colorScreenBackground));
                } else {
                    v.setBackgroundColor(ContextCompat.
                                        getColor(activity, R.color.colorScreenDeletedBackground));
                }
            }
        } else {
            v.setBackgroundColor(ContextCompat.
                                        getColor(activity, R.color.colorScreenDeletedBackground));
        }
    }



    //***********************************************************/
    //*********  RecyclerView / Adapter related Methods  ********/
    //***********************************************************/
    private RecyclerView getRecyclerView(View v){
        return  (RecyclerView) v.findViewById(R.id.historyTitleList);
    }


    //*********************************************************/
    //      Utility Functions used in handling events         //
    //*********************************************************/

    //called from onClick(), executed when a concurrent dose is selected from list
    private void onSelect(int position){

    }


}
