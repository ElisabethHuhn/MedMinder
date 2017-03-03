package com.androidchicken.medminder;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

    private static final String TAG = "LIST_PROJECTS_FRAGMENT";
    /**
     * Create variables for all the widgets
     *
     */

    private Button          mAddPersonsButton;


    /**********************************************************/
    //          Fragment Lifecycle Functions                  //
    /**********************************************************/

    //Constructor
    public MMPersonListFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }

    //This is where parameters are unbundled
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
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


        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_person_list);


        //9) return the view
        return v;
    }


    private void wireWidgets(View v){

        //Add Persons Button
        mAddPersonsButton = (Button) v.findViewById(R.id.addPersonsButton);
        mAddPersonsButton.setText(R.string.patient_add_persons_label);
        mAddPersonsButton.setOnClickListener(new View.OnClickListener() {
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
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.personList);

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
        MMPersonCursorAdapter adapter = new MMPersonCursorAdapter(cursor);
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
            new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {

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



    /**********************************************************/
    //      Utility Functions used in handling events         //
    /**********************************************************/

    //called from onClick(), executed when a person is selected
    private void onSelect(int position){
        //todo need to update selection visually

        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.personList);
        MMPersonCursorAdapter adapter = (MMPersonCursorAdapter) recyclerView.getAdapter();

        MMPersonManager personManager = MMPersonManager.getInstance();
        MMPerson selectedPerson =
                personManager.getPersonFromCursor(adapter.getPersonCursor(), position);

        Toast.makeText(getActivity(),
                selectedPerson.getNickname() + " is selected!",
                Toast.LENGTH_SHORT).show();

        //switch to the dose taken for the selected patient
        ((MainActivity) getActivity()).switchToHomeScreen(selectedPerson.getPersonID());
    }


    //Add some code to improve the recycler view
    //Here is the interface for event handlers for Click and LongClick
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

            private GestureDetector gestureDetector;
            private MMPersonListFragment.ClickListener clickListener;

            public RecyclerTouchListener(Context context,
                                         final RecyclerView recyclerView,
                                         final MMPersonListFragment.ClickListener clickListener) {

                this.clickListener = clickListener;
                gestureDetector = new GestureDetector(context,
                                                      new GestureDetector.SimpleOnGestureListener() {

                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                return true;
                            }

                            @Override
                            public void onLongPress(MotionEvent e) {
                                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                                if (child != null && clickListener != null) {
                                    clickListener.onLongClick(child, recyclerView.getChildLayoutPosition(child));
                                }
                            }
                        });
            }

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                    clickListener.onClick(child, rv.getChildLayoutPosition(child));
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        }

}
