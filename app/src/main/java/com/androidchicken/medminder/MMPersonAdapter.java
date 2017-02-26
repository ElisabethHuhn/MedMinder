package com.androidchicken.medminder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Elisabeth Huhn on 10/19/2016.
 *
 * Serves as a liaison between a list RecyclerView and the PersonManager
 */

public class MMPersonAdapter extends RecyclerView.Adapter<MMPersonAdapter.MyViewHolder>{
    //This list is copied from the PersonManager.
    // Only the PersonManager may
    //      alter the contents of the list, or
    //      alter an element of the list
    private List<MMPerson> mPersonList;

    //implement the ViewHolder as an inner class
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView personID;
        public TextView personNickName, personEmailAddr, personTextAddr;


        public MyViewHolder(View v) {
            super(v);

            personID        = (TextView) v.findViewById(R.id.personID);
            personNickName  = (TextView) v.findViewById(R.id.personNickNameInput);
            personEmailAddr = (TextView) v.findViewById(R.id.personEmailAddrInput);
            personTextAddr  = (TextView) v.findViewById(R.id.personTextAddrInput);
        }

    } //end inner class MyViewHolder

    //Constructor for MMPersonAdapter
    public MMPersonAdapter(List<MMPerson> personList){
        this.mPersonList = personList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_person, parent,  false);
        return new MyViewHolder(itemView);

    }

    public void removeItem(int position) {
        //This list is used locally as well as in the person container,
        //The list is "owned" by the PersonManager,
        // so call it to remove the item from the list
        MMPersonManager personManager = MMPersonManager.getInstance();
        personManager.removePerson(position);


        //this is for the particular item removed
        notifyItemRemoved(position);

        //this line is for all the items above position in the list
        //this line below gives you the animation and also updates the
        //list items after the deleted item
        notifyItemRangeChanged(position, getItemCount());


    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        if (mPersonList != null ) {
            //get the person indicated
            MMPerson person = mPersonList.get(position);

            holder.personID.       setText(String.valueOf(person.getPersonID()));
            holder.personNickName. setText(person.getNickname());
            holder.personEmailAddr.setText(person.getEmailAddress());
            holder.personTextAddr. setText(person.getTextAddress());

        } else {
            holder.personID.       setText("0");
            holder.personNickName. setText(R.string.no_persons_defined);
            holder.personEmailAddr.setText("");
            holder.personTextAddr. setText("");

        }

    }

    @Override
    public int getItemCount(){

        int returnValue = 0;

        if (mPersonList != null) {
            returnValue = mPersonList.size();
        }
        return returnValue;
    }

}
