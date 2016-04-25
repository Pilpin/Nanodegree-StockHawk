package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.HistoryAsyncTask;

public class StockDetailsActivity extends AppCompatActivity {
    private static String LOG_TAG = StockDetailsActivity.class.toString();
    LineChartView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        chartView = (LineChartView) findViewById(R.id.linechart);
        String symbol = "";
        String bidPrice = "";
        if(getIntent() != null) {
            if (getIntent().hasExtra(getString(R.string.intent_extra_symbol))) {
                symbol = getIntent().getStringExtra(getString(R.string.intent_extra_symbol));
                HistoryAsyncTask task = new HistoryAsyncTask(this, symbol);
                task.execute();
            }
            if (getIntent().hasExtra(getString(R.string.intent_extra_bidPrice))) {
                bidPrice = getIntent().getStringExtra(getString(R.string.intent_extra_bidPrice));
            }
        }

        if(!symbol.equals("") && !bidPrice.equals("")){
            getSupportActionBar().setTitle(symbol + " - " + bidPrice);
        }
    }

    public void onTaskCompleted(LineSet stockHistory, int maxValue, int minValue){
        stockHistory.setColor(getResources().getColor(android.R.color.white));
        chartView.addData(stockHistory);
        chartView.setAxisBorderValues(minValue - 3, maxValue + 3);
        chartView.show();
    }
}
