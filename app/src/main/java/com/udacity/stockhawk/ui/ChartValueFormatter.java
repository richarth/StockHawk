package com.udacity.stockhawk.ui;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by richardthompson on 22/04/2017.
 */

class ChartValueFormatter implements IValueFormatter {
    private DecimalFormat mFormat;

    public ChartValueFormatter() {
        mFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return mFormat.format(value);
    }
}
