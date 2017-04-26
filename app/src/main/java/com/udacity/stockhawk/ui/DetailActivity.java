package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int STOCK_LOADER = 0;

    private Cursor mCursor;

    private String stockSymbol;

    @BindView(R.id.chart)
    LineChart lineChart;

    private Uri stockUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        stockUri = getIntent().getData();

        stockSymbol = Contract.Quote.getStockFromUri(stockUri);

        getSupportActionBar().setTitle(stockSymbol);

        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                stockUri,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() != 0) {
            mCursor = data;

            mCursor.moveToFirst();

            String stockHistory = mCursor.getString(Contract.Quote.POSITION_HISTORY);

            String[] stockClosingPrices = stockHistory.split("\n");

            Collections.reverse(Arrays.asList(stockClosingPrices));

            List<Entry> entries = new ArrayList<>();

            final List<String> dateLabels = new ArrayList<>();

            int previousQuartersMonth = 0;
            float quarterSinceDataStart = 0;

            for (String daysData : stockClosingPrices) {
                String[] daysDataParts = daysData.split(", ");

                long datesTimestamp = Long.parseLong(daysDataParts[0]);
                int quoteMonth = getMonth(datesTimestamp);
                String daysStockPrice = daysDataParts[1];

                // Show the oldest date and every 3 months subsequently
                if (previousQuartersMonth == 0 || previousQuartersMonth == quoteMonth) {
                    entries.add(new Entry(quarterSinceDataStart, Float.parseFloat(daysStockPrice)));

                    dateLabels.add(getMonthYear(datesTimestamp));

                    quarterSinceDataStart++;

                    // Work out the month number for 3 months in the future
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(datesTimestamp);

                    cal.add(Calendar.MONTH, 3);

                    previousQuartersMonth = getMonth(cal.getTimeInMillis());
                }
            }

            IAxisValueFormatter xAxisformatter = new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return dateLabels.get((int) value);
                }
            };

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(xAxisformatter);
            xAxis.setTextColor(Color.WHITE);

            final DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

            IAxisValueFormatter yAxisFormatter = new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return dollarFormat.format(value);
                }
            };

            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            leftAxis.setValueFormatter(yAxisFormatter);
            leftAxis.setTextColor(Color.WHITE);

            YAxis rightAxis = lineChart.getAxisRight();
            rightAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            rightAxis.setValueFormatter(yAxisFormatter);
            rightAxis.setTextColor(Color.WHITE);

            LineDataSet dataSet = new LineDataSet(entries, getString(R.string.chart_label, stockSymbol));
            dataSet.setValueTextSize(10f);

            LineData lineData = new LineData(dataSet);
            lineData.setValueTextColor(Color.WHITE);
            lineData.setValueFormatter(new ChartValueFormatter());

            lineChart.setData(lineData);

            // Remove the description label
            lineChart.setDescription(null);

            lineChart.setContentDescription(getString(R.string.chart_label, stockSymbol));

            Legend legend = lineChart.getLegend();
            legend.setTextColor(Color.WHITE);

            lineChart.invalidate();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;

        lineChart.notifyDataSetChanged();

        lineChart.invalidate();
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);

        return DateFormat.format("dd-MM-yyyy", cal).toString();
    }

    private int getMonth(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);

        return Integer.parseInt(new SimpleDateFormat("M", Locale.ENGLISH).format(cal.getTime()));
    }

    private String getMonthYear(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);

        return DateFormat.format("MM/yy", cal).toString();
    }
}
