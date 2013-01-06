package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import de.uni_potsdam.hpi.openmensa.api.Meal;

/**
 * 
 * @author dominik
 *
 */
public class MealAdapter extends BaseExpandableListAdapter {
	Context context;
    int layoutResourceId;   
    ArrayList<Meal> data = null;
    LayoutInflater inflater;
    
    public MealAdapter(Context context, int layoutResourceId, ArrayList<Meal> listItems) {
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = listItems;
        
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Object getChild(int groupPosition, int childPosition) {
        return data.get(groupPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
    	View row = convertView;
        MealHolder holder = null;
       
        if(row == null)
        {
            row = inflater.inflate(R.layout.details, parent, false);
           
            holder = new MealHolder();
            holder.priceStudents = (TextView)row.findViewById(R.id.txtPriceStudents);
            holder.priceEmployees = (TextView)row.findViewById(R.id.txtPriceEmployees);
            holder.pricePupils = (TextView)row.findViewById(R.id.txtPricePupils);
            holder.priceOthers = (TextView)row.findViewById(R.id.txtPriceOthers);
            holder.notes = (TextView)row.findViewById(R.id.txtNotes);
           
            row.setTag(holder);
        } else {
            holder = (MealHolder)row.getTag();
        }
       
        Meal meal = data.get(groupPosition);
        holder.priceStudents.setText(stringOrNone(meal.prices.students));
        holder.priceEmployees.setText(stringOrNone(meal.prices.employees));
        holder.pricePupils.setText(stringOrNone(meal.prices.pupils));
        holder.priceOthers.setText(stringOrNone(meal.prices.others));
        
        holder.notes.setText("");
        int i = meal.notes.length;
        for (String note : meal.notes) {
			holder.notes.append(note);
			if (--i > 0) {
				holder.notes.append(", ");
			}
		}
       
        return row;
    }
    
    private String stringOrNone(float price) {
    	if (price > 0)
    		return String.format("%s", price);
    	return context.getResources().getString(R.string.noprice);
    }

    public Object getGroup(int groupPosition) {
        return data.get(groupPosition);
    }

    public int getGroupCount() {
        return data.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
    	View row = convertView;
        MealHolder holder = null;
       
        if(row == null)
        {
            row = inflater.inflate(R.layout.list_item, parent, false);
           
            holder = new MealHolder();
            holder.category = (TextView)row.findViewById(R.id.txtCategory);
            holder.name = (TextView)row.findViewById(R.id.txtName);
           
            row.setTag(holder);
        } else {
            holder = (MealHolder)row.getTag();
        }
       
        Meal meal = data.get(groupPosition);
        holder.category.setText(meal.category);
        holder.name.setText(meal.name);
       
        return row;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }
    
    static class MealHolder
    {
    	TextView category;
        TextView name;
        TextView priceStudents;
        TextView priceEmployees;
        TextView pricePupils;
        TextView priceOthers;
        TextView notes;
    }

}
