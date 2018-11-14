package com.stenden.esselbrugge.stendenrooster;

/**
 * Created by luuk on 21-9-17.
 */

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TabAdapter extends FragmentStatePagerAdapter {
    final int PAGE_COUNT = 5;
    private String tabTitles[] = new String[] { "MON", "TUE", "WED", "THU", "FRI" };
    private Context context;
    private List<Map<String, String>> data;

    public TabAdapter( FragmentManager fm, Context context, List<Map<String, String>> data) {
        super(fm);
        this.context = context;
        this.data = data;


    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {


                TabFragment fragmenttab1 = TabFragment.newInstance(position,context,data);


            return  fragmenttab1;

    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

}