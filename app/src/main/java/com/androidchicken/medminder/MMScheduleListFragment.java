package com.androidchicken.medminder;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Elisabeth Huhn on 3/10/17, adpated from MMPersonListFragment
 *
 * Defines a fragment whose main purpose is to provide a list of Schedules to the UI
 * This is a debug effort to determine why this list isn't working on the Medication Screen
 */

public class MMScheduleListFragment extends Fragment {

    private static final String TAG = "LIST_SCHEDULES_FRAGMENT";
    /**
     *  variables
     *
     */


    //*********************************************************/
    //          Fragment Lifecycle Functions                  //
    //*********************************************************/

    //Constructor
    public MMScheduleListFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the personID this screen refers to
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_schedule_list, container, false);
        v.setTag(TAG);

        wireWidgets(v);
        wireListTitleWidgets(v);

        initializeRecyclerView(v);

        //get rid of soft keyboard if it is visible
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity != null) {
            MMUtilities utilities = MMUtilities.getInstance();
            utilities.hideSoftKeyboard(activity);

            activity.handleFabVisibility();
        }
        return v;
    }

    @Override
    public void onResume(){

        super.onResume();
        MMUtilities utilities = MMUtilities.getInstance();
        utilities.clearFocus(getActivity());

        //set the title bar subtitle
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity != null) {
            activity.setMMSubtitle(R.string.title_schedule_list);

            //Set the FAB visible
            activity.hideFAB();
        }
    }


    //*************************************************************/
    /*  Convenience Methods for accessing things on the Activity  */
    //*************************************************************/

    private long     getPatientID(){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null) return MMUtilities.ID_DOES_NOT_EXIST;

        return activity.getPatientID();
    }

    private MMPerson getPerson()    {
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null) return null;

        return activity.getPerson();
    }


    //****************************/
    /*  Initialization Methods   */
    //****************************/
    private void wireWidgets(View v){

        //Person ID and name
        TextView personLabel      = v.findViewById(R.id.personIDLabel);
        //EditText personIDOutput   = v.findViewById(R.id.personIdInput);
        EditText personNameOutput = v.findViewById(R.id.personNickNameInput);

        personLabel.setText(R.string.person_label);
        MMPerson person = null;
        String message =
                String.format(getString(R.string.person_does_not_exist), getPatientID());
       // personIDOutput.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorLightPink));
        if (getPatientID() != MMUtilities.ID_DOES_NOT_EXIST) {
            person = getPerson();
        }
        if (person != null){
            //personIDOutput.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));
            message = person.getNickname().toString();
        }
        //personIDOutput.setText(String.valueOf(getPatientID()));
        personNameOutput.setText(message);
    }

    private void wireListTitleWidgets(View v){
        View field_container;
        TextView label;

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //set up the labels for the medication list
        field_container = v.findViewById(R.id.scheduleTitleRow);

        label = (EditText) (field_container.findViewById(R.id.scheduleTimeOutput));
        label.setText(R.string.medication_dose_time);
        label.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.scheduleMedNameOutput));
        label.setText(R.string.medication_nick_name_label);
        label.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.scheduleMedAmtOutput));
        label.setText(R.string.medication_dose_amount_label);
        label.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.scheduleUnitsOutput));
        label.setText(R.string.medication_dose_units_label);
        label.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorHistoryLabelBackground));
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
        //      implemented in the caller: onCreateView()

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null) return;

            //2) find and remember the RecyclerView
        RecyclerView recyclerView = getRecyclerView(v);

        //3) create and assign a layout manager to the recycler view
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get the set of Schedule Instances from the Database
        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();
        Cursor cursor;
        if (getPatientID() == MMUtilities.ID_DOES_NOT_EXIST){
            //no particular person, get all schedules
            cursor = schedMedManager.getAllSchedulesCursor();
        } else {
            cursor = schedMedManager.getAllSchedulesForPersonCursor(getPatientID());
        }

        //5) Use the data to Create and set out schedule Adapter
        //     even though we're giving the Adapter the list,
        //     Adapter uses the ScheduleManager to maintain the list and
        //     the items in the list.

        boolean is24Format = MMSettings.getInstance().isClock24Format((MMMainActivity)activity);
        MMScheduleListCursorAdapter adapter =
                                    new MMScheduleListCursorAdapter(activity,
                                                                cursor,
                                                                getPatientID());
        recyclerView.setAdapter(adapter);

        //6) create and set the itemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //7) create and add the item decorator
        recyclerView.addItemDecoration(new DividerItemDecoration(activity,
                                                               DividerItemDecoration.VERTICAL));
/*
        recyclerView.addItemDecoration(new DividerItemDecoration(
                                                                activity,
                                                                LinearLayoutManager.VERTICAL));
*/

        //8) add event listeners to the recycler view
        recyclerView.addOnItemTouchListener(
            new MMHomeFragment.RecyclerTouchListener(activity,
                                                     recyclerView,
                                                     new MMHomeFragment.ClickListener() {

                @Override
                public void onClick(View view, int position) {
                    //onSelect(position);
                }

                @Override
                public void onLongClick(View view, int position) {

                }
            })
        );

    }



    public void onExit(){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //MMUtilities.getInstance().showStatus(activity, R.string.exit_label);

        MMScheduleListCursorAdapter adapter = getAdapter(getView());
        adapter.closeCursor();

        //switch to person screen
        // But the switching happens on the container Activity
        activity.switchToHomeScreen();
    }

    private RecyclerView getRecyclerView(View v){
        return (RecyclerView) v.findViewById(R.id.scheduleList);
    }

    private MMScheduleListCursorAdapter getAdapter(View v){
        return (MMScheduleListCursorAdapter) getRecyclerView(v).getAdapter();
    }


    //*********************************************************/
    //      Utility Functions used in handling events         //
    //*********************************************************/

    //called from onClick(), executed when a schedule is selected
    private void onSelect(int position){

        View v = getView();
        if (v == null)return;

        RecyclerView recyclerView = v.findViewById(R.id.scheduleList);
        MMScheduleListCursorAdapter adapter = (MMScheduleListCursorAdapter) recyclerView.getAdapter();
        adapter.notifyItemChanged(position);

        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();
        MMSchedule selectedSchedule =
                schedMedManager.getScheduleFromCursor(adapter.getSchedMedCursor(), position);

        // TODO: 3/10/2017 allow the user to change the value of the schedule
    }


}
