package com.pwrobel.darkcam1;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;

/**
 * Created by pwrobel on 13.09.16.
 */
class CustomPagerAdapter extends PagerAdapter {

    CustomPagerAdapter(){
        super();
    }

    public Object instantiateItem(View collection, int position) {

        int resId = 0;
        switch (position) {
            case 0:
                resId = R.id.page_one;
                break;
            case 1:
                resId = R.id.page_two;
                break;
        }
        if(collection != null)
            return collection.findViewById(resId);
        else
            return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return "Titleee";
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}