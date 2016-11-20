package com.androidchicken.medminder;

/**
 * Created by Elisabeth Huhn on 11/4/2016.
 *
 * Defines an exception to use if the DB is found to be Corrupt
 */

public class Prism4DCorruptDBException extends Exception {
    //Parameterless Constructor
    public Prism4DCorruptDBException() {}

    //Constructor with a message
    public Prism4DCorruptDBException(String message){
        super(message);
    }
}
