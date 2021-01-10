package com.jogtown.jogtown.utils.ui;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class ChartAxisFormatter extends ValueFormatter {

    String nameToAppend;
    Boolean shouldShowLabel;

    public ChartAxisFormatter(String appendableName, Boolean showLabel) {
        nameToAppend = appendableName;
        shouldShowLabel = showLabel;
    }

    @Override
    public String getFormattedValue(float value) {
        if (shouldShowLabel) {
            return Float.toString(value) + nameToAppend;
        }
        return "";

    }

}
