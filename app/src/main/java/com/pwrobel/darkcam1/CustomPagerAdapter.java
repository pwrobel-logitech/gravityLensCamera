package com.pwrobel.darkcam1;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by pwrobel on 13.09.16.
 */
class CustomPagerAdapter extends PagerAdapter {

    ZoomWindowInfo parent_info_dialog;

    CustomPagerAdapter(ZoomWindowInfo info){
        super();
        this.parent_info_dialog = info;
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
            case 2:
                resId = R.id.page_three;
                break;
        }
        if(collection != null)
            return collection.findViewById(resId);
        else
            return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        container.removeView((View)object);
    }

    @Override
    public CharSequence getPageTitle(int position){
        if(this.parent_info_dialog == null)
            return Integer.toString(position);
        String query = "title_tab"+position+"_phenomenon";
        String obtained = this.parent_info_dialog.query_string_in_current_lang(query);
        return obtained;
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