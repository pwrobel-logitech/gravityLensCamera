package com.pwrobel.darkcam1;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Set;

/**
 * Created by pwrobel on 07.09.16.
 */
public class DialogMassChooser extends DialogFragment {

    private EditText mEditText;
    private SeekBar mSeek;
    TextView mass_value_text = null;

    private double default_phys_ratio;

    public void setDefault_phys_ratio(double ratio_phys){
        this.default_phys_ratio = ratio_phys;
    }

    private void setMassOnTextField(){
        if(mass_value_text != null) {
            double val = this.mScaleFactor;
            double ratio_earth1m = 0.017736;
            double ph_ratio = DialogMassChooser.this.default_phys_ratio;
            double mass = (val*ph_ratio) / ratio_earth1m;
            String str_mass = new DecimalFormat("##.##").format(mass*100);
            String suffix = this.getTextInCurrentLang("mass_unit_suffix");
            mass_value_text.setText(str_mass +"% "+ suffix);
        }
    }

    public DialogMassChooser() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.motherActivity = getActivity();
        Dialog dialog = getDialog();
        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View view = inflater.inflate(R.layout.fragment_layout1, container);
        TextView tilte = (TextView)view.findViewById(R.id.lbl_blackhole_mass_text);
        String modtitle = (this.getTextInCurrentLang("choose_mass"));
        tilte.setText(modtitle);
        mass_value_text = (TextView)view.findViewById(R.id.lbl_blackhole_mass);
        if(mass_value_text != null)
            DialogMassChooser.this.setMassOnTextField();
        //mEditText = (EditText) view.findViewById(R.id.txt_your_name);
        this.mSeek = (SeekBar)view.findViewById(R.id.seekBarMass);
        if(this.mSeek != null){
            this.mSeek.setMax(1000);
            int prg = this.getProgress();
            this.mSeek.setProgress(prg);
            this.mSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int prg = DialogMassChooser.this.getProgress();
                    seekBar.setProgress(prg);
                    double val = (((double) progress) / 1000.0)*(DialogMassChooser.this.mScaleFactor_max - DialogMassChooser.this.mScaleFactor_min ) + DialogMassChooser.this.mScaleFactor_min;
                    DialogMassChooser.this.setMassOnTextField();
                    if(!fromUser)
                        return;
                    MassSelectedListener act = (MassSelectedListener) DialogMassChooser.this.motherActivity;
                    DialogMassChooser.this.mScaleFactor = val;
                    if (act != null)
                        act.onMassSelected(val);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        //getDialog().setTitle("Hello");
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
        int progress = this.getProgress();
        //if(this.mSeek != null)
        //    this.mSeek.setProgress(progress);
    }

    private int getProgress(){
        return (int)(1000.0*((this.mScaleFactor - this.mScaleFactor_min)/(this.mScaleFactor_max-this.mScaleFactor_min)));
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

    //public static String fixedLengthString(String string, int length) {
    //    return String.format("%1$"+length+ "s", string);
    //}
}
