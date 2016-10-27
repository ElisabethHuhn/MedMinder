package com.androidchicken.medminder;

/**
 * Created by elisabethhuhn on 10/12/2016.
 *
 * This class contains utilities used by other classes in the package
 */

public class MMUtilities {
    /*************************************/
    /*    Static (class) Constants       */
    /*************************************/


    /*************************************/
    /*    Static (class) Variables       */
    /*************************************/


    /*************************************/
    /*    Member (instance) Variables    */
    /*************************************/



    /*************************************/
    /*         Static Methods            */
    /*************************************/
    //generate a guarenteed unique ID
    public static int getUniqueID(){
        return (int) (System.currentTimeMillis() & 0xfffffff);
    }

    /*************************************/
    /*         CONSTRUCTOR               */
    /*************************************/



    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/


    /*************************************/
    /*          Member Methods           */
    /*************************************/


}
