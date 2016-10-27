package com.androidchicken.medminder;

import java.util.ArrayList;

/**
 * Created by elisabethhuhn on 10/17/2016.
 *
 * The class in charge of maintaining the set of instances of Medication
 *  both in-memory and in the DB
 *
 */

public class MMMedicationManager {
    /************************************/
    /********* Static Constants  ********/
    /************************************/

    public static final int MEDICATION_NOT_FOUND = -1;


    /************************************/
    /********* Static Variables  ********/
    /************************************/
    private static MMMedicationManager ourInstance ;

    /**************************************/
    /********* Member Variables   *********/
    /**************************************/
    //The medication lists exist on the Persons, rather than on a list here
    //private ArrayList<MMMedication> mMedicationList;

    /************************************/
    /********* Static Methods   *********/
    /************************************/
    public static MMMedicationManager getInstance() {
        if (ourInstance == null){
            ourInstance = new MMMedicationManager();
        }
        return ourInstance;
    }


    /************************************/
    /********* Constructors     *********/
    /************************************/
    private MMMedicationManager() {

        //The medication list already exists on the Person instance
        //mMedicationList = new ArrayList<>();

        //This is where we would read the list from the database
        // TODO: 10/17/2016   get the list of projects from the DB

        //but for now, just make up some data
        // TODO: 10/17/2016 create dummy person data if none exists

    }

    /*******************************************/
    /********* Public Member Methods   *********/
    /*******************************************/

    //This routine not only adds to the in memory list, but also to the DB
    //returns FALSE if for any reason the medication can not be added
    public boolean add(MMMedication newMedication){
        //Find the person the medication is for
        int personID = newMedication.getForPersonID();

        //Now find that person using the PersonManager
        MMPersonManager   personManager =  MMPersonManager.getInstance();
        MMPerson person = personManager.getPerson(personID);

        //If person not found, return false.
        //  Can not add a medication to a person who does not exist
        if (person == null) return false;

        //determine if the medication already is associated with this person
        ArrayList<MMMedication> medicationList = person.getMedications();

        //if not, create one
        if (medicationList == null){
            medicationList = new ArrayList<>();
            person.setMedications(medicationList);
        }

        //determine whether the medication already exists in the list
        int atPosition = findMedicationPosition(medicationList, newMedication.getMedicationID());
        if (atPosition == MEDICATION_NOT_FOUND){//The medication does not already exist. Add it
            medicationList.add(newMedication);
            // TODO: 10/18/2016 Add the medication to the DB
        } else { //The medication does exist, Update it
            MMMedication listMedication = medicationList.get(atPosition);

            //update the list instance with the attributes from the new medication being added
            listMedication.setBrandName         (newMedication.getBrandName());
            listMedication.setGenericName       (newMedication.getGenericName());
            listMedication.setMedicationNickname(newMedication.getMedicationNickname());
            listMedication.setOrder             (Integer.valueOf(newMedication.getOrder()));
            listMedication.setDoseAmount        (Integer.valueOf(newMedication.getDoseAmount()));
            listMedication.setDoseUnits         (newMedication.getDoseUnits());
            listMedication.setWhenDue           (newMedication.getWhenDue());
            listMedication.setNum               (Integer.valueOf(newMedication.getNum()));

            // TODO: 10/18/2016 Update the medication in the DB
        }

        return true;
    }

    //This routine not only replaces in the in memory list, but also in the DB
    public void update(MMMedication medication){
        //The update functionality already exists in add
        //    as a Medication can only appear once
        add(medication);
    }//end public add()

    //Because the list is on one person instance, we must also have the person ID
    //of the instance being manipulated
    //This routine not only removes from the in-memory list, but also from the DB
    public boolean removeMedication(int personID, int position) {
        //Now find that person using the PersonManager
        MMPersonManager   personManager =  MMPersonManager.getInstance();
        MMPerson person = personManager.getPerson(personID);

        //If person not found, return false.
        //  Can not add a medication to a person who does not exist
        if (person == null) return false;

        //determine if the medication already is associated with this person
        ArrayList<MMMedication> medicationList = person.getMedications();

        //if not, create one
        if (medicationList == null){
            medicationList = new ArrayList<>();
            person.setMedications(medicationList);
        }

        if (position > medicationList.size()) {
            //Can't remove a position that the list isn't long enough for
            return false;
        }

        medicationList.remove(position);
        return true;
    }//end public remove position




    /********************************************/
    /********* Private Member Methods   *********/
    /********************************************/

    //Find the position of the medication instance
    //     that matches the argument medicationID
    //     within the argument list medicationList
    //returns constant = MEDICATION_NOT_FOUND if the medication is not in the list
    //NOTE it is a RunTimeException to call this routine if the list is null or empty
    private int findMedicationPosition(ArrayList<MMMedication> medicationList, int medicationID){
        MMMedication medication;
        int position        = 0;
        int last            = medicationList.size();

        //Determine whether an instance of the medication is already in the list
        //NOTE that if list is empty, while doesn't loop even once
        while (position < last){
            medication = medicationList.get(position);

            if (medication.getMedicationID() == medicationID){
                //Found the medication in the list at this position
                return position;
            }
            position++;
        }
        return MEDICATION_NOT_FOUND;
    }


}
