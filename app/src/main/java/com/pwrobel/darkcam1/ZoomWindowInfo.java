package com.pwrobel.darkcam1;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by pwrobel on 08.09.16.
 */
public class ZoomWindowInfo extends DialogFragment {

    CustomPagerAdapter tab_adapter1;
    ViewPager pager;

    public ZoomWindowInfo() {
        // Empty constructor required for DialogFragment
    }

    private Activity motherActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.motherActivity = getActivity();
        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        //getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View view = inflater.inflate(R.layout.fragment_layout2, container);
        TextView tilte = (TextView)view.findViewById(R.id.info_zoom_text_title);
        String modtitle = (this.getTextInCurrentLang("description_phenomenon1"));
        tilte.setText(modtitle);

        getDialog().setTitle(null);
        //getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        this.tab_adapter1 = new CustomPagerAdapter();
        this.pager = (ViewPager) view.findViewById(R.id.pager1);
        this.pager.setAdapter(this.tab_adapter1);
        this.pager.setCurrentItem(1);

        return view;
    }

    private Set<String> languages;
    private String language;
    public void setLanguagesInfo(Set<String> langs, String lang){
        this.languages = langs;
        this.language = lang;
    }

    private String getStringResourceByName(String aString) {
        if(this.motherActivity == null) {
            Log.i("darkcam", "error, mother activity null inside mass chooser dialog");
            return "";
        }
        String packageName = this.motherActivity.getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        return getString(resId);
    }

    private String getTextInCurrentLang(String text){
        return (this.getStringResourceByName(this.language + "_" + text));
    }

    public static String fixedLengthString(String string, int length) {
        return String.format("%1$"+length+ "s", string);
    }


}
