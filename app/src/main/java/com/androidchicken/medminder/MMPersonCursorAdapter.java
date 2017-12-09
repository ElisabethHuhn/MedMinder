package com.androidchicken.medminder;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
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

class MMPersonCursorAdapter extends RecyclerView.Adapter<MMPersonCursorAdapter.MyViewHolder>{
    private Cursor  mPersonCursor;
    private Context mContext;

    //implement the ViewHolder as an inner class
    class MyViewHolder extends RecyclerView.ViewHolder {
        //The views in each row to be displayed
        TextView personNickName;


        MyViewHolder(View v) {
            super(v);

            personNickName  = v.findViewById(R.id.personNickNameInput);

        }

    } //end inner class MyViewHolder

    //Constructor for MMPersonAdapter
    MMPersonCursorAdapter(Context context, Cursor personCursor){
        this.mContext      = context;
        this.mPersonCursor = personCursor;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_row_person, parent,  false);
        return new MyViewHolder(itemView);

    }

    Cursor reinitializeCursor(){
        closeCursor();

        MMPersonManager personManager = MMPersonManager.getInstance();
        //Create a new Cursor with the current contents of DB
        mPersonCursor = personManager.getAllPersonsCursor();

        //Tell the RecyclerView to update the User Display
        notifyDataSetChanged();
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
                holder.personNickName. setText(mContext.getString(R.string.no_persons_defined));


                setBackColor(holder, R.color.colorGray);
                return;
            }


        }

        //get the row indicated
        MMPerson person = personManager.getPersonFromCursor(mPersonCursor, position);

        holder.personNickName. setText(person.getNickname());


        if (person.isCurrentlyExists()){
            setBackColor(holder, R.color.colorWhite);
        } else {
            setBackColor(holder, R.color.colorScreenDeletedBackground);
        }
    }


    private void setBackColor(MMPersonCursorAdapter.MyViewHolder holder, int newColor){
        holder.personNickName.   setBackgroundColor(ContextCompat.getColor(mContext, newColor));

    }


    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mPersonCursor != null) {
            returnValue = mPersonCursor.getCount();
        }
        return returnValue;
    }

    Cursor getCursor(){return mPersonCursor;}

    void closeCursor(){
        if (mPersonCursor != null)mPersonCursor.close();
    }

}
