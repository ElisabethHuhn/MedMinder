package com.androidchicken.medminder;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Elisabeth Huhn on 3/10/17, adpated from MMPersonListFragment
 *
 * Defines a fragment whose main purpose is to provide a list of Schedules to the UI
 * This is a debug effort to determine why this list isn't working on the Medication Screen
 */

public class MMScheduleListFragment extends Fragment {

    private static final String TAG = "LIST_SCHEDULES_FRAGMENT";
    /**
     * Create variables for all the widgets
     *
     */


    /**********************************************************/
    //          Fragment Lifecycle Functions                  //
    /**********************************************************/

    //Constructor
    public MMScheduleListFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_schedule_list, container, false);
        v.setTag(TAG);

        wireWidgets(v);
        wireListTitleWidgets(v);

        initializeRecyclerView(v);

        //get rid of soft keyboard if it is visible
        MMUtilities.hideSoftKeyboard(getActivity());

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_schedule_list);


        //9) return the view
        return v;
    }

    private void wireWidgets(View v){
        //Exit Button
        Button exitButton = (Button) v.findViewById(R.id.exitButton);
        exitButton.setText(R.string.exit_label);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onExit();
            }
        });

    }

    private void wireListTitleWidgets(View v){
        View field_container;
        TextView label;

        MainActivity myActivity = (MainActivity)getActivity();

        //set up the labels for the medication list
        field_container = v.findViewById(R.id.scheduleTitleRow);

        label = (EditText) (field_container.findViewById(R.id.scheduleTimeHourOutput));
        label.setText(R.string.medication_dose_hours);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.scheduleTimeMinutesOutput));
        label.setText(R.string.medication_dose_minutes);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));
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

        //2) find and remember the RecyclerView
        RecyclerView recyclerView = getRecyclerView(v);

        //3) create and assign a layout manager to the recycler view
        //RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get the set of Schedule Instances from the Database
        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        Cursor cursor = schedMedManager.getAllSchedMedsCursor();

        int size = cursor.getCount();

        //5) Use the data to Create and set out schedule Adapter
        //     even though we're giving the Adapter the list,
        //     Adapter uses the ScheduleManager to maintain the list and
        //     the items in the list.
        MMSchedMedCursorAdapter adapter =
                new MMSchedMedCursorAdapter(cursor, MMUtilities.is24Format());
        recyclerView.setAdapter(adapter);

        //6) create and set the itemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //7) create and add the item decorator
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                                                               DividerItemDecoration.VERTICAL));
/*
        recyclerView.addItemDecoration(new DividerItemDecoration(
                                                                getActivity(),
                                                                LinearLayoutManager.VERTICAL));
*/

        //8) add event listeners to the recycler view
        recyclerView.addOnItemTouchListener(
            new MMHomeFragment.RecyclerTouchListener(getActivity(),
                                                     recyclerView,
                                                     new MMHomeFragment.ClickListener() {

                @Override
                public void onClick(View view, int position) {
                    onSelect(position);
                }

                @Override
                public void onLongClick(View view, int position) {

                }
            })
        );

    }



    private void onExit(){

        Toast.makeText(getActivity(),
                R.string.exit_label,
                Toast.LENGTH_SHORT).show();

        MMSchedMedCursorAdapter adapter = getAdapter(getView());
        adapter.closeCursor();

        //switch to person screen
        // But the switching happens on the container Activity
       // ((MainActivity) getActivity()).switchToPopBackstack();
        ((MainActivity) getActivity()).switchToHomeScreen();
    }

    private RecyclerView getRecyclerView(View v){
        return (RecyclerView) v.findViewById(R.id.scheduleList);
    }

    private MMSchedMedCursorAdapter getAdapter(View v){
        return (MMSchedMedCursorAdapter) getRecyclerView(v).getAdapter();
    }


    /**********************************************************/
    //      Utility Functions used in handling events         //
    /**********************************************************/

    //called from onClick(), executed when a schedule is selected
    private void onSelect(int position){
        //todo need to update selection visually

        View v = getView();
        if (v == null)return;

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.scheduleList);
        MMSchedMedCursorAdapter adapter = (MMSchedMedCursorAdapter) recyclerView.getAdapter();

        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        MMScheduleMedication selectedSchedule =
                schedMedManager.getScheduleMedicationFromCursor(adapter.getSchedMedCursor(), position);


        Toast.makeText(getActivity(),
                String.valueOf(selectedSchedule.getTimeDue()) + " is selected!",
                Toast.LENGTH_SHORT).show();

        // TODO: 3/10/2017 allow the user to change the value of the schedule
    }


}