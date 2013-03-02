package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;
import java.util.Locale;

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
           
            holder = new MealHolder(row);
           
            row.setTag(holder);
        } else {
            holder = (MealHolder)row.getTag();
        }
       
        Meal meal = data.get(groupPosition);
        holder.setData(meal);
       
        return row;
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
           
            holder = new MealHolder(row);
           
            row.setTag(holder);
        } else {
            holder = (MealHolder)row.getTag();
        }
       
        Meal meal = data.get(groupPosition);
        
        holder.setData(meal);
       
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
    	public MealHolder() {}
    	
    	public MealHolder(View row) {
    		category = (TextView)row.findViewById(R.id.txtCategory);
            name = (TextView)row.findViewById(R.id.txtName);
    		priceStudents = (TextView)row.findViewById(R.id.txtPriceStudents);
            priceEmployees = (TextView)row.findViewById(R.id.txtPriceEmployees);
            pricePupils = (TextView)row.findViewById(R.id.txtPricePupils);
            priceOthers = (TextView)row.findViewById(R.id.txtPriceOthers);
            notes = (TextView)row.findViewById(R.id.txtNotes);
		}
    	
		public void setData(Meal meal) {
			if (category != null) {
				category.setText(meal.category);
		        name.setText(meal.name);
				int i = meal.notes.length;
				notes.setText("");
				for (String note : meal.notes) {
					notes.append(note);
					if (--i > 0) {
						notes.append(", ");
					}
				}
			}
			
			if (priceStudents != null) {
				priceStudents.setText(stringOrNone(meal.prices.students));
				priceEmployees.setText(stringOrNone(meal.prices.employees));
				pricePupils.setText(stringOrNone(meal.prices.pupils));
				priceOthers.setText(stringOrNone(meal.prices.others));
			}
		}
		
		private String stringOrNone(float price) {
	    	if (price > 0)
	    		return String.format(Locale.getDefault(), "%.2f", price);
	    	return MainActivity.getAppContext().getResources().getString(R.string.noprice);
	    }
		
		TextView category;
        TextView name;
        TextView priceStudents;
        TextView priceEmployees;
        TextView pricePupils;
        TextView priceOthers;
        TextView notes;
    }

}
