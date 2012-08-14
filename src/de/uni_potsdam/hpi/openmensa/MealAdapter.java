package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import de.uni_potsdam.hpi.openmensa.api.Meal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * 
 * @author dominik
 *
 */
public class MealAdapter extends ArrayAdapter<Meal>{
	Context context;
    int layoutResourceId;   
    ArrayList<Meal> data = null;
    LayoutInflater inflater;
   
    public MealAdapter(Context context, int layoutResourceId, ArrayList<Meal> listItems) {
        super(context, layoutResourceId, listItems);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = listItems;
        
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MealHolder holder = null;
       
        if(row == null)
        {
            row = inflater.inflate(R.layout.list_item, parent, false);
           
            holder = new MealHolder();
            holder.title = (TextView)row.findViewById(R.id.txtTitle);
            holder.description = (TextView)row.findViewById(R.id.txtDescription);
           
            row.setTag(holder);
        }
        else
        {
            holder = (MealHolder)row.getTag();
        }
       
        Meal meal = data.get(position);
        holder.title.setText(meal.name);
        holder.description.setText(meal.description);
       
        return row;
    }
   
    static class MealHolder
    {
    	TextView title;
        TextView description;
    }
}
