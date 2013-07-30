package de.uni_potsdam.hpi.openmensa;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;

import de.uni_potsdam.hpi.openmensa.helpers.RefreshableFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the primary sections of the app.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
	
	static final int NUM_ITEMS = 5;

	public SectionsPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	private Fragment[] fragments = new Fragment[getCount()];
	
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		
		for (Fragment fragment : fragments) {
			if (fragment != null) {
				((RefreshableFragment) fragment).refresh();
			}
		}
	}

	/**
	 * Creates/ returns an Item
	 */	
	@Override
	public Fragment getItem(int position) {
		Log.d(MainActivity.TAG, String.format("New Fragment requested %d", position));
		if (fragments[position] == null) {
			if (position == 0) {
				CanteenFragment fragment = new CanteenFragment();
				fragments[0] = fragment;
			} else {
				DayFragment fragment = new DayFragment();
				fragments[position] = fragment;
			}
			
		}

		return fragments[position];
	}

	@Override
	public int getCount() {
		return NUM_ITEMS;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Context context = MainActivity.getAppContext();
		switch (position) {
			case 0:
				return context.getString(R.string.section_canteen).toUpperCase();
			case 1:
				return context.getString(R.string.section_yesterday).toUpperCase();
			case 2:
				return context.getString(R.string.section_today).toUpperCase();
			case 3:
				return context.getString(R.string.section_tomorrow).toUpperCase();
			case 4:
				return context.getString(R.string.section_da_tomorrow).toUpperCase();
		}
		return null;
	}

	public void setToFetching(boolean on, boolean animated) {
		ArrayList<Integer> daySections = getDaySections();
		int i = 0;
		for (Fragment fragment : fragments) {
			if (!daySections.contains(i++))
				continue;
			if (fragment != null) {
				((DayFragment) fragment).setToFetching(on, animated);
			}
		}
	}

	public ArrayList<Integer> getDaySections() {
		ArrayList<Integer> sections = new ArrayList<Integer>();
		for (int i = 1; i < getCount(); i++) {
			sections.add(i);
		}
		return sections;
	}
}