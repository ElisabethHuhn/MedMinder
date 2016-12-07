package com.androidchicken.medminder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainMMTakeDoseFragment extends Fragment {

    private static final String TAG = "MainMMTakeDoseFragment";

    /***********************************************/
    /*          UI Widgets                         */
    /***********************************************/

    //main area of screen fragment
    private Button mPatientProfileButton;
    private Button mAddPersonsButton;
    private Button mSelectPatientButton;
    private Button mSaveButton;
    private Button mEditMedicationButton;
    ArrayList<Button> mMedButtons = new ArrayList<>();
    ArrayList<EditText> mMedEdits = new ArrayList<>();


    private TextView mPatientNickName;

    private EditText mTimeInput;

    private EditText mMed1Input;
    private EditText mMed2Input;
    private EditText mMed3Input;
    private EditText mMed4Input;
    private EditText mMed5Input;



    /***********************************************/
    /*          Member Variables                   */
    /***********************************************/
    private MMPerson mPatient;
    private int      mPersonID;
    private ArrayList<MMMedication> mMedications;







    /***********************************************/
    /*          Static Methods                     */
    /***********************************************/


    //need to pass a person into the fragment
    public static MainMMTakeDoseFragment newInstance(int personID){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putInt         (MMPerson.sPersonIDTag,personID);

        MainMMTakeDoseFragment fragment = new MainMMTakeDoseFragment();

        fragment.setArguments(args);
        return fragment;
    }


    /***********************************************/
    /*          Constructor                        */
    /***********************************************/

    //
    public MainMMTakeDoseFragment() {
    }

    /***********************************************/
    /*          Lifecycle Methods                  */
    /***********************************************/

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
            mPersonID = args.getInt(MMPerson.sPersonIDTag);
            MMPersonManager personManager = MMPersonManager.getInstance();

            //If personID can't be found in the list, mPatient will be null
            mPatient = personManager.getPerson(mPersonID);
            if (mPatient != null) {
                mMedications = mPatient.getMedications();
            }

        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dose_taken, container, false);



        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_take_dose);

        initializeUI();



        return v;
    }

    /***********************************************/
    /*          Member Methods                     */
    /***********************************************/

    private void wireWidgets(View v){

        //Patient Profile Button
        mPatientProfileButton = (Button) v.findViewById(R.id.patientProfileButton);
        mPatientProfileButton.setText(R.string.patient_edit_profile_label);
        //the order of images here is left, top, right, bottom
        // mPatientProfileButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        mPatientProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.patient_edit_profile_label,
                        Toast.LENGTH_SHORT).show();
                //switch to person screen
                // But the switching happens on the container Activity
                if (mPersonID == 0) {
                    ((MainActivity) getActivity()).switchToPersonScreen();
                } else {
                    //pre-populate
                    ((MainActivity) getActivity()).switchToPersonScreen(mPersonID);
                }

            }
        });

        //Add Persons Button
        mAddPersonsButton = (Button) v.findViewById(R.id.addPersonsButton);
        mAddPersonsButton.setText(R.string.patient_add_persons_label);
        //the order of images here is left, top, right, bottom
        // mAddPersonsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
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

        //Show Persons Button
        mSelectPatientButton = (Button) v.findViewById(R.id.selectPatientButton);
        mSelectPatientButton.setText(R.string.show_persons_label);
        //the order of images here is left, top, right, bottom
        // mPatientProfileButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        mSelectPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.show_persons_label,
                        Toast.LENGTH_SHORT).show();
                //switch to person screen
                // But the switching happens on the container Activity
                ((MainActivity) getActivity()).switchToPersonListScreen();
            }
        });

        //save Button
        mSaveButton = (Button) v.findViewById(R.id.patientSaveButton);
        mSaveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //mSaveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.save_label,
                        Toast.LENGTH_SHORT).show();

                onSave();
            }
        });

        //Edit Medication Button
        mEditMedicationButton = (Button) v.findViewById(R.id.patientEditMedicationButton);
        mEditMedicationButton.setText(R.string.patient_add_medication_label);
        //the order of images here is left, top, right, bottom
        //mEditMedicationButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_stakeout, 0, 0);
        mEditMedicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.patient_add_medication_label,
                        Toast.LENGTH_SHORT).show();
                //switch to medication screen
                // But the switching happens on the container Activity
                ((MainActivity) getActivity()).switchToMedicationScreen(mPatient.getPersonID());
            }
        });

        //Medication Buttons
        if (mMedications != null) {

            int last       = mMedications.size();
            int position   = 0;

            //convert pixels to dp
            int sizeInDp   = 10; //padding between buttons
            float scale    = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (sizeInDp*scale + 0.5f);


            while (position < last) {
                addMedButtonToView(v, last, position, dpAsPixels);
                position++;
            }

            addbuttonListeners();

        }//if (sMedications != null)




        //Patient Nick Name
        mPatientNickName = (TextView) v.findViewById(R.id.patientNickNameLabel);
        //There are no events associated with this field

        //Time input for this dose
        //There is no label for this field
        mTimeInput = (EditText) v.findViewById(R.id.doseTimeInput);
        mTimeInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });



    }

    private String getMedNickname(int position){

        MMMedication medication;

        String nickName;
        position = position-1;

        if (mPatient != null){
            mMedications = mPatient.getMedications();
            if (mMedications != null) {
                if (position < mMedications.size()){
                    medication = mMedications.get(position);
                    if (medication != null){
                        nickName = medication.getMedicationNickname().toString().trim();
                        return nickName;
                    }
                }
            }
        }
        return "Med"+ String.valueOf(position+1);
    }

    private void addMedButtonToView(View v, int last, int position, int padding){
        Button medButton;
        EditText edtView;

        //
        //Add the button to the button layout
        //
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.medButtonLayout);

        medButton = new Button(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,//width
                ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 1f;
        lp.setMarginEnd(padding);

        medButton.setLayoutParams(lp);
        medButton.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorButton1Background));


        medButton.setPadding(0,0,padding,0);
        medButton.setText(getMedNickname(position + 1));
        medButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorTextBlack));
        layout.addView(medButton);

        //save the pointer to the button
        mMedButtons.add(medButton);


        //
        //add EditText to the dose layout
        //
        layout = (LinearLayout) v.findViewById(R.id.medInputLayout);
        lp = new LinearLayout.LayoutParams(
                0,//width
                ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 1f;
        //lp.gravity = Gravity.CENTER;
        lp.setMarginEnd(padding);

        edtView = new EditText(getActivity());
        edtView.setHint("0");
        edtView.setInputType(InputType.TYPE_CLASS_TEXT);
        edtView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edtView.setLayoutParams(lp);
        edtView.setPadding(0,0,padding,0);
        edtView.setGravity(Gravity.CENTER);
        edtView.setTextColor      (ContextCompat.getColor(getActivity(),R.color.colorTextBlack));
        edtView.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorInputBackground));

        //add listener
        edtView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        layout.addView(edtView);

    }

    private void addbuttonListeners(){
        Button medButton;

        if (mMedButtons.size() > 0) {
            medButton = mMedButtons.get(0);
            if (medButton != null) {
                //add the listeners to the button
                medButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),
                                R.string.patient_medication_button_1_label,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                medButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        Toast.makeText(getActivity(),
                                R.string.person_med_long_click,
                                Toast.LENGTH_SHORT).show();

                        return true;
                    }
                });
            }
        }

        if (mMedButtons.size() > 1) {
            medButton = mMedButtons.get(1);
            if (medButton != null) {
                //add the listeners to the button
                medButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),
                                R.string.patient_medication_button_2_label,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                medButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        Toast.makeText(getActivity(),
                                R.string.person_med_long_click,
                                Toast.LENGTH_SHORT).show();

                        return true;
                    }
                });
            }
        }

        if (mMedButtons.size() > 2) {
            medButton = mMedButtons.get(1);
            if (medButton != null) {
                //add the listeners to the button
                medButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),
                                R.string.patient_medication_button_2_label,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                medButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        Toast.makeText(getActivity(),
                                R.string.person_med_long_click,
                                Toast.LENGTH_SHORT).show();

                        return true;
                    }
                });
            }
        }

        if (mMedButtons.size() >= 3) {
            medButton = mMedButtons.get(2);
            if (medButton != null) {
                //add the listeners to the button
                medButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),
                                R.string.patient_medication_button_3_label,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                medButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        Toast.makeText(getActivity(),
                                R.string.person_med_long_click,
                                Toast.LENGTH_SHORT).show();

                        return true;
                    }
                });
            }
        }

        if (mMedButtons.size() >= 4) {
            medButton = mMedButtons.get(3);
            if (medButton != null) {
                //add the listeners to the button
                medButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),
                                R.string.patient_medication_button_4_label,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                medButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        Toast.makeText(getActivity(),
                                R.string.person_med_long_click,
                                Toast.LENGTH_SHORT).show();

                        return true;
                    }
                });
            }
        }

        if (mMedButtons.size() >= 5) {
            medButton = mMedButtons.get(4);
            if (medButton != null) {
                //add the listeners to the button
                medButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),
                                R.string.patient_medication_button_5_label,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                medButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        Toast.makeText(getActivity(),
                                R.string.person_med_long_click,
                                Toast.LENGTH_SHORT).show();

                        return true;
                    }
                });
            }
        }

    }

    private void initializeUI(){
        //determine if a person is yet associated with the fragment
        if (mPersonID != 0){
            //if there is a person corresponding to the patientID, put the name up on the screen
            if (mPatient != null) {
                mPatientNickName.setText(mPatient.getNickname().toString().trim());
            }
        }
    }

    private void onSave(){
        int temp;
    }
}
