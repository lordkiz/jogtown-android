package com.jogtown.jogtown.utils.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.LruCache;

import androidx.annotation.NonNull;

public class MyTypefaceSpan extends MetricAffectingSpan {

    //Cache old
    private static LruCache<String, Typeface> typefaceLruCache = new LruCache<>(10);
    private Typeface myTypeface;

    public MyTypefaceSpan(Context context, String typefaceName) {
        myTypeface = typefaceLruCache.get(typefaceName);

        if (myTypeface == null) {
            myTypeface = Typeface.createFromAsset(
                    context.getApplicationContext().getAssets(),
                    typefaceName
                );

            //cache this
            typefaceLruCache.put(typefaceName, myTypeface);
        }
    }
    @Override
    public void updateMeasureState(@NonNull TextPaint textPaint) {
        textPaint.setTypeface(myTypeface);

        //important for proper rendering
        textPaint.setFlags(textPaint.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);

    }

    @Override
    public void updateDrawState(TextPaint tp) {

        tp.setTypeface(myTypeface);

        //important for proper rendering
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}
