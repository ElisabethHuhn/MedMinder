package com.androidchicken.medminder;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Elisabeth Huhn on 2/24/2017.
 *
 * Serves as a liaison between a list RecyclerView and the PersonManager
 * Adapted from the list adapter which uses an ArrayList of person objects
 * This adapter uses a Cursor of the person rows in the DB
 */

public class MMPersonCursorAdapter extends RecyclerView.Adapter<MMPersonCursorAdapter.MyViewHolder>{
    private Cursor mPersonCursor;

    //implement the ViewHolder as an inner class
    public class MyViewHolder extends RecyclerView.ViewHolder {
        //The views in each row to be displayed
        public TextView personID;
        public TextView personNickName, personEmailAddr, personTextAddr;


        public MyViewHolder(View v) {
            super(v);

            personID        = (TextView) v.findViewById(R.id.personMainID);
            personNickName  = (TextView) v.findViewById(R.id.personNickNameInput);
            personEmailAddr = (TextView) v.findViewById(R.id.personEmailAddrInput);
            personTextAddr  = (TextView) v.findViewById(R.id.personTextAddrInput);
        }

    } //end inner class MyViewHolder

    //Constructor for MMPersonAdapter
    public MMPersonCursorAdapter(Cursor personCursor){
        this.mPersonCursor = personCursor;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_person, parent,  false);
        return new MyViewHolder(itemView);

    }

    public void removeItem(int position) {
        if (mPersonCursor == null)return;

        MMPersonManager personManager = MMPersonManager.getInstance();

        //get the row indicated which is the person to be removed
        MMPerson person = personManager.getPersonFromCursor(mPersonCursor, position);
        if (person == null)return;

        //remove the person from the DB
        personManager.removePersonFromDB(person.getPersonID());
        // TODO: 2/24/2017 remove the proper concurrent dose from the DB

        //Create a new Cursor with the current contents of DB
        mPersonCursor = reinitializeCursor();
    }

    public Cursor reinitializeCursor(){
        MMPersonManager personManager = MMPersonManager.getInstance();
        //Create a new Cursor with the current contents of DB
        mPersonCursor = personManager.getAllPersonsCursor();

        //Tell the RecyclerView to update the User Display
        notifyDataSetChanged();;
        // notifyItemRangeChanged(position, getItemCount());

        return mPersonCursor;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){

        MMPersonManager personManager = MMPersonManager.getInstance();

        if (mPersonCursor == null){
            mPersonCursor = personManager.getAllPersonsCursor();
            //if there aren't any people, just return
            if (mPersonCursor == null) {
                holder.personID.       setText("0");
                holder.personNickName. setText("No persons defined");
                holder.personEmailAddr.setText("");
                holder.personTextAddr. setText("");}
            return;
        }

        //get the row indicated
        MMPerson person = personManager.getPersonFromCursor(mPersonCursor, position);

        holder.personID.       setText(String.valueOf(person.getPersonID()));
        holder.personNickName. setText(person.getNickname());
        holder.personEmailAddr.setText(person.getEmailAddress());
        holder.personTextAddr. setText(person.getTextAddress());
    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mPersonCursor != null) {
            returnValue = mPersonCursor.getCount();
        }
        return returnValue;
    }

    public Cursor getPersonCursor(){return mPersonCursor;}

}
