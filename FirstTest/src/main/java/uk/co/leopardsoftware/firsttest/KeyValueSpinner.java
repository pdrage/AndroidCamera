package uk.co.leopardsoftware.firsttest;

/**
 * Created by pdrage on 16/04/2014.
 * From source at http://androidbiginner.blogspot.co.uk/2013/08/custom-spinner-with-key-value.html
 */

import android.widget.SpinnerAdapter;
import java.util.ArrayList;
import android.content.Context;

import android.view.View;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.database.DataSetObserver;


public class KeyValueSpinner implements SpinnerAdapter{
    Context context;
    ArrayList alList;

    public KeyValueSpinner(Context context ,ArrayList alList){
        this.context =context;
        this.alList = alList;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return alList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return alList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        return 0;
    }
    //Note:-Create this two method getIDFromIndex and getIndexByID
    public int getIDFromIndex(int Index) {
        return    ((DocumentType)alList.get(Index)).getID();
    }

    public int getIndexByID(int ID) {
        for(int i=0;i< getCount(); i++){
            if(((DocumentType)alList.get(i)).getID() == ID){
            return i;
        }
    }
    return -1;
}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView textview = (TextView) inflater.inflate(android.R.layout.simple_spinner_item, null);
        textview.setText(((DocumentType)alList.get(position)).getName());

        return textview;
    }

    @Override
    public int getViewTypeCount() {
        return android.R.layout.simple_spinner_item;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // TODO Auto-generated method stub

    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView textview = (TextView) inflater.inflate(android.R.layout.simple_spinner_item, null);
        textview.setText(((DocumentType)alList.get(position)).getName());

        return textview;
    }
}