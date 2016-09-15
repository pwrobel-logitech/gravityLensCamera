package com.pwrobel.darkcam1;
//lens image - credit for : ANDREW HAMILTON
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.util.Linkify;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pwrobel on 08.09.16.
 */
public class ZoomWindowInfo extends DialogFragment {

        /**
         * @param textView
         *            textView who's text you want to change
         * @param linkThis
         *            a regex of what text to turn into a link
         * @param toThis
         *            the url you want to send them to
         */
        private static void internaladdLinks(TextView textView, String linkThis, String toThis) {
            Pattern pattern = Pattern.compile(linkThis);
            String scheme = toThis;
            android.text.util.Linkify.addLinks(textView, pattern, scheme, new android.text.util.Linkify.MatchFilter() {
                @Override
                public boolean acceptMatch(CharSequence s, int start, int end) {
                    return true;
                }
            }, new android.text.util.Linkify.TransformFilter() {

                @Override
                public String transformUrl(Matcher match, String url) {
                    return "";
                }
            });
        }



    CustomPagerAdapter tab_adapter1;
    ViewPager pager;

    public ZoomWindowInfo() {
        // Empty constructor required for DialogFragment
    }

    private Activity motherActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        //dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

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

        TextView tilte2 = (TextView)view.findViewById(R.id.info_zoom_text_title2);
        String modtitle2 = (this.getTextInCurrentLang("description_phenomenon2"));
        tilte2.setText(modtitle2);

        TextView tdonation = (TextView)view.findViewById(R.id.text_info_donations);
        String modon = (this.getTextInCurrentLang("about_app_description1"));
        tdonation.setText(modon);

        TextView tdonation2 = (TextView)view.findViewById(R.id.donation_link1);
        String modon2 = (this.getTextInCurrentLang("linkify_pp"));
        tdonation2.setText(modon2);
        //Pattern p1 = Pattern.compile(modon2);
        String scheme = this.getTextInCurrentLang("donation_link1");
        ZoomWindowInfo.internaladdLinks(tdonation2, modon2, scheme);

        TextView tdonation3 = (TextView)view.findViewById(R.id.page_ovh_info);
        String modon3 = (this.getTextInCurrentLang("ovh_page1"));
        tdonation3.setText(modon3);

        TextView tdonation4 = (TextView)view.findViewById(R.id.donation_link1_info1);
        String modon4 = (this.getTextInCurrentLang("note2"));
        tdonation4.setText(modon4);

        TextView tdonation5 = (TextView)view.findViewById(R.id.page_ovh_info_note1);
        String modon5 = (this.getTextInCurrentLang("note1"));
        tdonation5.setText(modon5);

        //getDialog().setTitle(null);
        //getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        //setStyle(STYLE_NO_TITLE, 0);

        this.tab_adapter1 = new CustomPagerAdapter(ZoomWindowInfo.this);
        this.pager = (ViewPager) view.findViewById(R.id.pager1);
        pager.invalidate();
        this.pager.setAdapter(this.tab_adapter1);

        int num_tab = which_tab_num;
        if (num_tab >= this.tab_adapter1.getCount())
            num_tab = 0;
        this.pager.invalidate();
        this.tab_adapter1.notifyDataSetChanged();
        this.pager.setCurrentItem(num_tab);

        view.invalidate();

        return view;
    }

    private Set<String> languages;
    private String language;
    public void setLanguagesInfo(Set<String> langs, String lang){
        this.languages = langs;
        this.language = lang;
    }

    public String query_string_in_current_lang(String tag){
        return this.getTextInCurrentLang(tag);
    }

    private int which_tab_num;
    public void setWhichTabSelectedByDefault(int which){
        this.which_tab_num = which;
    }

    public void update_pager(){
        if(this.pager != null)
            this.pager.invalidate();
        if(this.tab_adapter1 != null)
            this.tab_adapter1.notifyDataSetChanged();
        if(this.pager != null)
            this.pager.setCurrentItem(this.which_tab_num);
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
