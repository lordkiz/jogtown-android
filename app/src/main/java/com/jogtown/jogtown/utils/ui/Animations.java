package com.jogtown.jogtown.utils.ui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.jogtown.jogtown.R;

public class Animations {


    public static Animation clockwise(View view){
        return AnimationUtils.loadAnimation(view.getContext(),
                R.anim.clockwise);
    }

    public static Animation zoom(View view){
        return AnimationUtils.loadAnimation(view.getContext(),
                R.anim.zoom);
    }

    public static Animation fadeIn(View view){
        return AnimationUtils.loadAnimation(view.getContext(),
                        R.anim.fadein);
    }

    public static Animation fadeOut(View view){
        return AnimationUtils.loadAnimation(view.getContext(),
                R.anim.fadeout);
    }

    public static Animation blink(View view){
        return AnimationUtils.loadAnimation(view.getContext(),
                        R.anim.blink);
    }

    public static Animation move(View view){
        return AnimationUtils.loadAnimation(view.getContext(), R.anim.move);
    }

    public static Animation slide(View view){
        return AnimationUtils.loadAnimation(view.getContext(), R.anim.slide);
    }
}
