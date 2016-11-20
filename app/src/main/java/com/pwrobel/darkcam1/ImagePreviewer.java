package com.pwrobel.darkcam1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.Gallery.LayoutParams;

import static android.support.v7.widget.TintTypedArray.obtainStyledAttributes;

/**
 * Created by pwrobel on 20.11.16.
 */

public class ImagePreviewer extends DialogFragment implements ViewSwitcher.ViewFactory {

    private Activity parentActivity;


    @Override
    public View makeView() {
        return null;
        /*
        ImageView imageView = new ImageView(this.parentActivity);
        imageView.setBackgroundColor(0xFF000000);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new
                ImageSwitcher.LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        return imageView;
        */
    }

    private ImageSwitcher imageSwitcher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.parentActivity = getActivity();
        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        View view = inflater.inflate(R.layout.image_preview, container);


        //imageSwitcher = (ImageSwitcher) this.parentActivity.findViewById(R.id.switcher1);

        /*
        if(this.imageSwitcher == null)
            return null;
        imageSwitcher.setFactory(this);
        imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this.parentActivity,
                android.R.anim.fade_in));
        imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this.parentActivity,
                android.R.anim.fade_out));

        Gallery gallery = (Gallery) this.parentActivity.findViewById(R.id.gallery1);
        gallery.setAdapter(new ImageAdapter(this.parentActivity));*/
        /*gallery.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView parent,
                                    View v, int position, long id)
            {
                imageSwitcher.setImageResource(imageIDs[position]);
            }
        });*/


        return view;
    }


    /*
    public class ImageAdapter extends BaseAdapter
    {
        private Context context;
        private int itemBackground;

        public ImageAdapter(Context c)
        {
            context = c;

            //---setting the style---
            //TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
            //itemBackground = a.getResourceId(
            //        R.styleable.Gallery1_android_galleryItemBackground, 0);
            //a.recycle();
        }

        //---returns the number of images---
        public int getCount()
        {
            return 3;//return imageIDs.length;
        }

        //---returns the ID of an item---
        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        //---returns an ImageView view---
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView imageView = new ImageView(context);
            //imageView.setImageResource(imageIDs[position]);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new Gallery.LayoutParams(150, 120));
            imageView.setBackgroundResource(itemBackground);
            return imageView;
        }
    }
    */

}
