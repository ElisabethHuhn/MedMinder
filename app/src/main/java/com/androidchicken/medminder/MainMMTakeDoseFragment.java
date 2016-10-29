package com.androidchicken.medminder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

    private MMMedication mMed1;
    private MMMedication mMed2;
    private MMMedication mMed3;
    private MMMedication mMed4;
    private MMMedication mMed5;





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

        Bundle args = getArguments();

        if (args != null) {
            mPersonID = args.getInt(MMPerson.sPersonIDTag);
            MMPersonManager personManager = MMPersonManager.getInstance();

            //If personID can't be found in the list, mPatient will be null
            mPatient = personManager.getPerson(mPersonID);
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

        //determine if a person is yet associated with the fragment
        if (mPersonID != 0){
            //if there is a person corresponding to the patientID, put the name up on the screen
            if (mPatient != null) {
                mPatientNickName.setText(mPatient.getNickname().toString().trim());

            }
        }

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
                
            }
        });

        //Edit Medication Button
        mEditMedicationButton = (Button) v.findViewById(R.id.patientEditMedicationButton);
        mEditMedicationButton.setText(R.string.patient_edit_medication_label);
        //the order of images here is left, top, right, bottom
        //mEditMedicationButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_stakeout, 0, 0);
        mEditMedicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.patient_edit_medication_label,
                        Toast.LENGTH_SHORT).show();
                //switch to medication screen
                // But the switching happens on the container Activity
                ((MainActivity) getActivity()).switchToMedicationScreen();
            }
        });

        //Medication Buttons
        int last = mMedications.size();
        int position = 0;
        Button medButton;
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.medButtonLayout);
        while (position < last) {
            //Add the button to the layout
            medButton = new Button(getActivity());
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT );

            medButton.setLayoutParams(lp);
            medButton.setBackgroundColor(medButton.getContext().getResources().getColor(R.color.colorButton1Background));
            medButton.setText(getMedNickname(position+1));
            medButton.setTextColor(medButton.getContext().getResources().getColor(R.color.colorTextBlack));
            layout.addView(medButton);

            //save the pointer to the button
            mMedButtons.add(medButton);

        }

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

        //Medication 1 input for this dose
        //There is no label for this field
        mMed1Input = (EditText) v.findViewById(R.id.doseMed1Input);
        mMed1Input.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        //Medication 2 input for this dose
        //There is no label for this field
        mMed2Input = (EditText) v.findViewById(R.id.doseMed2Input);
        mMed2Input.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        //Medication 3 input for this dose
        //There is no label for this field
        mMed3Input = (EditText) v.findViewById(R.id.doseMed3Input);
        mMed3Input.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        //Medication 4 input for this dose
        //There is no label for this field
        mMed4Input = (EditText) v.findViewById(R.id.doseMed4Input);
        mMed4Input.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        //Medication 5 input for this dose
        //There is no label for this field
        mMed5Input = (EditText) v.findViewById(R.id.doseMed5Input);
        mMed5Input.setOnTouchListener(new View.OnTouchListener() {
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
}
