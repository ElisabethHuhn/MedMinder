package com.androidchicken.medminder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainMMPersonFragment extends Fragment {

    //main area of screen fragment
    private Button   mSaveButton;

    private EditText mPersonNickNameInput;
    private EditText mPersonEmailAddrInput;
    private EditText mPersonTextAddrInput;
    private EditText mPersonOrderInput;
    private EditText mPersonDurationInput;


    private MMPerson mPerson;
    private int      mPersonID;

    /***********************************************/
    /*          Static Methods                     */
    /***********************************************/


    //need to pass a person into the fragment
    public static MainMMPersonFragment newInstance(int personID){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putInt         (MMPerson.sPersonIDTag,personID);

        MainMMPersonFragment fragment = new MainMMPersonFragment();

        fragment.setArguments(args);
        return fragment;
    }

    /***********************************************/
    /*          Constructor                        */
    /***********************************************/

    //
    public MainMMPersonFragment() {
    }

    /***********************************************/
    /*          Lifecycle Methods                  */
    /***********************************************/


    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the personID this screen refers to
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            mPersonID = args.getInt(MMPerson.sPersonIDTag);
            MMPersonManager personManager = MMPersonManager.getInstance();

            //If personID can't be found in the list, mPatient will be null
            mPerson = personManager.getPerson(mPersonID);

        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_person, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        //If we had any arguments passed, update the screen with them
        initializeUI();

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_person);

        return v;
    }

    private void wireWidgets(View v){

        //save Button
        mSaveButton = (Button) v.findViewById(R.id.personSaveButton);
        mSaveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //mSaveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.save_label,
                        Toast.LENGTH_SHORT).show();

                savePerson();

                //switch to home screen with the person as a patient
                int personID;
                if (mPerson != null) {
                    personID = mPerson.getPersonID();
                    if (personID != 0) {
                        // But the switching happens on the container Activity
                        ((MainActivity) getActivity()).switchToHomeScreen(personID);
                    }
                }

                //if here, person wasn't defined properly. Just keep trying
                
            }
        });




        mPersonNickNameInput = (EditText) v.findViewById(R.id.personNickNameInput);
        mPersonNickNameInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();

                return false;
            }
        });


        mPersonEmailAddrInput = (EditText) v.findViewById(R.id.personEmailAddrInput);
        mPersonEmailAddrInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        mPersonTextAddrInput = (EditText) v.findViewById(R.id.personTextAddrInput);
        mPersonTextAddrInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        mPersonOrderInput = (EditText) v.findViewById(R.id.personOrderInput);
        mPersonOrderInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        mPersonDurationInput = (EditText) v.findViewById(R.id.personDurationInput);
        mPersonDurationInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    private void initializeUI(){
        if (mPerson == null) {
            mPerson =  new MMPerson();
        }
        mPersonNickNameInput. setText(mPerson.getNickname().toString().trim());
        mPersonEmailAddrInput.setText(mPerson.getEmailAddress().toString().trim());
        mPersonTextAddrInput. setText(mPerson.getTextAddress().toString().trim());
        mPersonOrderInput.    setText(String.valueOf(mPerson.getMedOrder()).trim());
        mPersonDurationInput. setText(String.valueOf(mPerson.getDuration()).trim());


    }

    private void savePerson(){
        CharSequence nickname = mPersonNickNameInput.getText();
        if (nickname == null){
            Toast.makeText(getActivity(),
                    R.string.person_not_valid,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        MMPerson person = new MMPerson(nickname);

        //strings are set to "" in the constructor, so the empty case can be ignored
        String temp = mPersonEmailAddrInput.getText().toString().trim();
        if (!(temp.isEmpty())){
            person.setEmailAddress(temp);
        }

        temp = mPersonTextAddrInput.getText().toString().trim();
        if (!(temp.isEmpty())){
            person.setTextAddress(temp);
        }


        //int's are set to 0 in the constructor (and of course everywhere) so ignore
        boolean digitsOnly = TextUtils.isDigitsOnly(mPersonDurationInput.getText());
        int inputLength = mPersonDurationInput.getText().toString().trim().length();
        if ((digitsOnly) && (inputLength != 0)){
            person.setDuration(Integer.valueOf(mPersonDurationInput.getText().toString()));
        }

        digitsOnly = TextUtils.isDigitsOnly(mPersonOrderInput.getText());
        inputLength = mPersonOrderInput.getText().toString().trim().length();
        if ((digitsOnly)&& (inputLength != 0)){
            person.setMedOrder(Integer.valueOf(mPersonOrderInput.getText().toString()));
        }

        //done in constructor
        // person.setMedications(new ArrayList<MMMedication>());

        //so add the person to permanent storage
        MMPersonManager personManager = MMPersonManager.getInstance();
        personManager.add(person);
        mPerson = person;

    }
}
