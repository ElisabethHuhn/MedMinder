package com.androidchicken.medminder;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 2/14/17, adpated from MMPersonAdapter
 *
 * Defines a fragment whose main purpose is to provide a list of Persons to the UI
 */

public class MainMMPersonListFragment extends Fragment {

        private static final String TAG = "LIST_PROJECTS_FRAGMENT";
        /**
         * Create variables for all the widgets
         *  although in the mockup, most will be statically defined in the xml
         */

        private Button          mAddPersonsButton;
        private ArrayList<MMPerson> mPersonList ;
        private RecyclerView    mRecyclerView;
        private MMPersonAdapter mAdapter;

        private MMPerson mSelectedPerson;
        private MMPerson mLastSelectedPerson;
        private int      mSelectedPosition;




        /**********************************************************/
        //          Fragment Lifecycle Functions                  //
        /**********************************************************/

        //Constructor
        public MainMMPersonListFragment() {
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


            //2) find and remember the RecyclerView
            mRecyclerView = (RecyclerView) v.findViewById(R.id.personList);

            //3) create and assign a layout manager to the recycler view
            //RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
            RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            //4) Get the list of Person Instances from the PersonManager

            //      get the singleton list container
            MMPersonManager personManager = MMPersonManager.getInstance();
            //      then go get our list of persons
            mPersonList = personManager.getPersonList();

            //5) Use the data to Create and set out person Adapter
            //     even though we're giving the Adapter the list,
            //     Adapter uses the PersonManager to maintain the list and
            //     the items in the list.
            mAdapter = new MMPersonAdapter(mPersonList);
            mRecyclerView.setAdapter(mAdapter);

            //6) create and set the itemAnimator
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());

            //7) create and add the item decorator
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                                                                   DividerItemDecoration.VERTICAL));
/*
            mRecyclerView.addItemDecoration(new DividerItemDecoration(
                                                                    getActivity(),
                                                                    LinearLayoutManager.VERTICAL));
*/

            //8) add event listeners to the recycler view
            mRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(getActivity(), mRecyclerView, new ClickListener() {

                    @Override
                    public void onClick(View view, int position) {
                        onSelect(position);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));

        }



        /**********************************************************/
        //      Utility Functions used in handling events         //
        /**********************************************************/

        //called from onClick(), executed when a person is selected
        private void onSelect(int position){
            //todo need to update selection visually
            mSelectedPosition = position;
            // TODO: 10/3/2016 Need to query list in Person Manager or Adapter, not locally 
            mSelectedPerson = mPersonList.get(position);

            Toast.makeText(getActivity(),
                    mSelectedPerson.getNickname() + " is selected!",
                    Toast.LENGTH_SHORT).show();

            //switch to the dose taken for the selected patient
            ((MainActivity) getActivity()).switchToHomeScreen(mSelectedPerson.getPersonID());
        }


        //Add some code to improve the recycler view
        //Here is the interface for event handlers for Click and LongClick
        public interface ClickListener {
            void onClick(View view, int position);

            void onLongClick(View view, int position);
        }

        public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

            private GestureDetector gestureDetector;
            private MainMMPersonListFragment.ClickListener clickListener;

            public RecyclerTouchListener(Context context,
                                         final RecyclerView recyclerView,
                                         final MainMMPersonListFragment.ClickListener clickListener) {

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
