package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.ParcelableLineSet;
import com.sam_chordas.android.stockhawk.rest.HistoryAsyncTask;

public class StockDetailsActivity extends AppCompatActivity {
    private static String LOG_TAG = StockDetailsActivity.class.toString();
    private static final String MAX_VALUE = "max_value";
    private static final String MIN_VALUE = "min_value";
    private static final String STOCK_HISTORY = "stock_history";

    LineChartView chartView;
    ParcelableLineSet mStockHistory;
    int chartMinValue;
    int chartMaxValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        chartView = (LineChartView) findViewById(R.id.linechart);
        String symbol = "";

        if(getIntent() != null){
            if (getIntent().hasExtra(getString(R.string.intent_extra_symbol))) {
                symbol = getIntent().getStringExtra(getString(R.string.intent_extra_symbol));
                if (getIntent().hasExtra(getString(R.string.intent_extra_bidPrice))) {
                    String bidPrice = getIntent().getStringExtra(getString(R.string.intent_extra_bidPrice));
                    getSupportActionBar().setTitle(symbol + " - " + bidPrice);
                }
            }
        }

        if(savedInstanceState == null && !symbol.equals("")){
            HistoryAsyncTask task = new HistoryAsyncTask(this, symbol);
            task.execute();
        }else if(savedInstanceState != null){
            mStockHistory = savedInstanceState.getParcelable(STOCK_HISTORY);
            chartMaxValue = savedInstanceState.getInt(MAX_VALUE);
            chartMinValue = savedInstanceState.getInt(MIN_VALUE);
            displayChart();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(MAX_VALUE, chartMaxValue);
        outState.putInt(MIN_VALUE, chartMinValue);
        outState.putParcelable(STOCK_HISTORY, mStockHistory);
        super.onSaveInstanceState(outState);
    }

    public void displayChart(){
        if(chartView.isShown()){
            chartView.dismiss();
        }
        chartView.addData(mStockHistory);
        chartView.setAxisBorderValues(chartMinValue - 3, chartMaxValue + 3);
        Animation anim = new Animation();
        chartView.show(anim);
    }

    public void onTaskCompleted(ParcelableLineSet stockHistory, int maxValue, int minValue){
        chartMinValue = minValue;
        chartMaxValue = maxValue;

        mStockHistory = stockHistory;
        mStockHistory.setColor(getResources().getColor(android.R.color.white));
        mStockHistory.setDotsColor(getResources().getColor(R.color.material_green_700));
        mStockHistory.setSmooth(true);

        displayChart();
    }
}
