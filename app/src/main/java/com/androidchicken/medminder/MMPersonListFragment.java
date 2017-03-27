package com.androidchicken.medminder;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Elisabeth Huhn on 2/14/17, adpated from MMPersonAdapter
 *
 * Defines a fragment whose main purpose is to provide a list of Persons to the UI
 */

public class MMPersonListFragment extends Fragment {

    private static final String TAG = "LIST_PERSONS_FRAGMENT";
    private static final String RETURN_TAG = "RETURN_TAG";
    /**
     * Create variables for all the widgets
     *
     */


    private CharSequence mReturnFragmentTag = null;
    private long         mPersonID = MMUtilities.ID_DOES_NOT_EXIST;


    /***********************************************/
    /*          Static Methods                     */
    /***********************************************/
    //need to pass a return destination into the fragment
    public static MMPersonListFragment newInstance(CharSequence returnTag, long personID){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putCharSequence  (RETURN_TAG, returnTag);
        args.putLong(MMPerson.sPersonIDTag, personID);

        MMPersonListFragment fragment = new MMPersonListFragment();

        fragment.setArguments(args);
        return fragment;
    }



    /**********************************************************/
    //          Fragment Lifecycle Functions                  //
    /**********************************************************/

    //Constructor
    public MMPersonListFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    //This is where parameters are unbundled
    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the personID this screen refers to
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //Initialize the DB if necessary

        try {
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance(getActivity());
        }catch (Exception e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }

        Bundle args = getArguments();

        if (args != null) {
            mReturnFragmentTag = args.getCharSequence(RETURN_TAG);
            mPersonID          = args.getLong(MMPerson.sPersonIDTag);

        } else {
            //Do not really know what to do here. Go to the Home fragment, but its arbitrary
            mReturnFragmentTag = MainActivity.sHomeTag;
            mPersonID = MMUtilities.ID_DOES_NOT_EXIST;

        }

    }




    //set up the recycler view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_person_list, container, false);
        v.setTag(TAG);

        wireWidgets(v);
        wireListTitleWidgets(v);

        initializeRecyclerView(v);

        //get rid of soft keyboard if it is visible
        MMUtilities.hideSoftKeyboard(getActivity());

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_person_list);


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



        //Add Persons Button
        Button addPersonButton = (Button) v.findViewById(R.id.addPersonsButton);
        addPersonButton.setText(R.string.patient_add_persons_label);
        addPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.patient_add_persons_label,
                        Toast.LENGTH_SHORT).show();
                //switch to person screen
                // But the switching happens on the container Activity
                ((MainActivity) getActivity()).switchToPersonScreen();
            }
        });
    }

    private void wireListTitleWidgets(View v){
        View field_container;
        TextView label;

        MainActivity myActivity = (MainActivity)getActivity();

        //set up the labels for the medication list
        field_container = v.findViewById(R.id.personTitleRow);

        label = (TextView) (field_container.findViewById(R.id.personMainID));
        label.setText(R.string.person_id_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.personNickNameInput));
        label.setText(R.string.person_nick_name_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.personEmailAddrInput));
        label.setText(R.string.person_email_addr_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.personTextAddrInput));
        label.setText(R.string.person_text_addr_label);
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

        //4) Get the set of Person Instances from the Database

        MMPersonManager personManager = MMPersonManager.getInstance();
        Cursor cursor = personManager.getAllPersonsCursor();

        //5) Use the data to Create and set out person Adapter
        //     even though we're giving the Adapter the list,
        //     Adapter uses the PersonManager to maintain the list and
        //     the items in the list.
        MMPersonCursorAdapter adapter = new MMPersonCursorAdapter(getActivity(), cursor);
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

        MMPersonCursorAdapter adapter = getAdapter(getView());
        adapter.closeCursor();

        //switch to person screen
        // But the switching happens on the container Activity
        ((MainActivity) getActivity()).switchToPersonListReturn(mReturnFragmentTag, mPersonID);
    }

    private RecyclerView getRecyclerView(View v){
        return (RecyclerView) v.findViewById(R.id.personList);
    }

    private MMPersonCursorAdapter getAdapter(View v){
        return (MMPersonCursorAdapter) getRecyclerView(v).getAdapter();
    }

    /**********************************************************/
    //      Utility Functions used in handling events         //
    /**********************************************************/

    //called from onClick(), executed when a person is selected
    private void onSelect(int position){
        //todo need to update selection visually

        View v = getView();
        if (v == null)return;

        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.personList);
        MMPersonCursorAdapter adapter = (MMPersonCursorAdapter) recyclerView.getAdapter();

        MMPersonManager personManager = MMPersonManager.getInstance();
        MMPerson selectedPerson =
                personManager.getPersonFromCursor(adapter.getCursor(), position);

        Toast.makeText(getActivity(),
                selectedPerson.getNickname() + " is selected!",
                Toast.LENGTH_SHORT).show();

        //switch to the dose taken for the selected patient
        ((MainActivity) getActivity()).switchToPersonListReturn(mReturnFragmentTag,
                                                                selectedPerson.getPersonID());
    }
}