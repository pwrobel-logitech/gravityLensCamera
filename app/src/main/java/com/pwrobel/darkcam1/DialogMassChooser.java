package com.pwrobel.darkcam1;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by pwrobel on 07.09.16.
 */
public class DialogMassChooser extends DialogFragment {

    private EditText mEditText;

    public DialogMassChooser() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_layout1, container);
        mEditText = (EditText) view.findViewById(R.id.txt_your_name);
        getDialog().setTitle("Hello");
        this.motherActivity = getActivity();
        TextView mdesc = (TextView) view.findViewById(R.id.lbl_blackhole_mass_text);
        mdesc.setText(this.getTextInCurrentLang("choose_mass"));
        return view;
    }

    private Activity motherActivity;
    private Set<String> languages;
    private String language;
    public void setLanguagesInfo(Set<String> langs, String lang){
        this.languages = langs;
        this.language = lang;
    }

    private double mScaleFactor_min = 0.5f;
    private double mScaleFactor_max = 2.0f;
    private double mScaleFactor;
    public void setFactorScaleRange(double min, double max){
        this.mScaleFactor_min = min;
        this.mScaleFactor_max = max;
    }

    public void setFactorScale(double factor){
        this.mScaleFactor = factor;
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
}
