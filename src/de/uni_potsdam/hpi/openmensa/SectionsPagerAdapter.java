package de.uni_potsdam.hpi.openmensa;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the primary sections of the app.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
	
	static final int NUM_ITEMS = 4;

	public SectionsPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	private DaySectionFragment[] fragments = new DaySectionFragment[getCount()];
	
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		
		// TODO: use adapter properly
		for (DaySectionFragment fragment : fragments) {
			if (fragment != null) {
				fragment.update();
			}
		}
	}

	/**
	 * Creates/ returns an Item
	 */	
	@Override
	public DaySectionFragment getItem(int position) {
		if (fragments[position] == null) {
			DaySectionFragment fragment = new DaySectionFragment();
			fragments[position] = fragment;
		}

		return fragments[position];
	}

	@Override
	public int getCount() {
		return NUM_ITEMS;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Context context = MainActivity.context;
		switch (position) {
			case 0:
				return context.getString(R.string.title_section0).toUpperCase();
			case 1:
				return context.getString(R.string.title_section1).toUpperCase();
			case 2:
				return context.getString(R.string.title_section2).toUpperCase();
			case 3:
				return context.getString(R.string.title_section3).toUpperCase();
		}
		return null;
	}

	public void setToFetching(boolean on, boolean animated) {
		for (DaySectionFragment fragment : fragments) {
			if (fragment != null) {
				fragment.setToFetching(on, animated);
			}
		}
	}
}