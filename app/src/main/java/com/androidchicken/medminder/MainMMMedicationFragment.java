package com.androidchicken.medminder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class MainMMMedicationFragment extends Fragment {

    //main area of screen fragment

    private Button   mMedicationSaveButton;

    private EditText mMedicationBrandNameInput;
    private EditText mMedicationGenericNameInput;
    private EditText mMedicationNickNameInput;
    private EditText mMedicationForInput;
    private EditText mMedicationOrderInput;
    private EditText mMedicationDoseAmountInput;
    private EditText mMedicationDoseUnitsInput;
    private EditText mMedicationDoseDueWhenInput;
    private EditText mMedicationDoseNumInput;

    //Medication being input
    private MMMedication mMedication = (MMMedication) new MMMedication();


    public MainMMMedicationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_medication, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_medication);



        return v;
    }

    private void wireWidgets(View v){


        mMedicationSaveButton = (Button) v.findViewById(R.id.medicationSaveButton);
        mMedicationSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.save_label,
                        Toast.LENGTH_SHORT).show();
                //Save the Medication to the Medication Manager

                //switch to home screen
                // But the switching happens on the container Activity
                ((MainActivity) getActivity()).switchToHomeScreen();
            }

        });




        mMedicationBrandNameInput = (EditText) v.findViewById(R.id.medicationBrandNameInput);
        mMedicationBrandNameInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();

                mMedication.setBrandName(mMedicationBrandNameInput.getText().toString());

                return false;
            }
        });


        mMedicationGenericNameInput = (EditText) v.findViewById(R.id.medicationGenericNameInput);
        mMedicationGenericNameInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();

                mMedication.setGenericName(mMedicationGenericNameInput.getText().toString());

                return false;
            }
        });


        mMedicationNickNameInput = (EditText) v.findViewById(R.id.medicationNickNameInput);
        mMedicationNickNameInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();

                mMedication.setMedicationNickname(mMedicationNickNameInput.getText().toString());

                return false;
            }
        });

        mMedicationForInput = (EditText) v.findViewById(R.id.medicationForInput);
        mMedicationForInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();

                mMedication.setForPersonID(Integer.valueOf(mMedicationForInput.getText().toString()));

                return false;
            }
        });


        mMedicationOrderInput = (EditText) v.findViewById(R.id.medicationOrderInput);
        mMedicationOrderInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();

                mMedication.setOrder(Integer.valueOf (mMedicationOrderInput.getText().toString()));

                return false;
            }
        });


        mMedicationDoseAmountInput = (EditText) v.findViewById(R.id.medicationDoseAmountInput);
        mMedicationDoseAmountInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();

                mMedication.setDoseAmount(Integer.valueOf(mMedicationDoseAmountInput.getText().toString()));

                return false;
            }
        });


        mMedicationDoseUnitsInput = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
        mMedicationDoseUnitsInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();

                mMedication.setDoseUnits(mMedicationDoseUnitsInput.getText().toString());

                return false;
            }
        });

        mMedicationDoseDueWhenInput = (EditText) v.findViewById(R.id.medicationDoseDueWhenInput);
        mMedicationDoseDueWhenInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();

                mMedication.setWhenDue(mMedicationDoseDueWhenInput.getText().toString());

                return false;
            }
        });

        mMedicationDoseNumInput = (EditText) v.findViewById(R.id.medicationDoseNumInput);
        mMedicationDoseNumInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();

                mMedication.setNum(Integer.valueOf(mMedicationDoseNumInput.getText().toString()));

                return false;
            }
        });



    }
}
