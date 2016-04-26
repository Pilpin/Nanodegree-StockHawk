package com.sam_chordas.android.stockhawk.rest;

import android.os.AsyncTask;

import com.sam_chordas.android.stockhawk.data.ParcelableLineSet;
import com.sam_chordas.android.stockhawk.ui.StockDetailsActivity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class HistoryAsyncTask extends AsyncTask<Void, Void, ParcelableLineSet> {
    private static String LOG_TAG = HistoryAsyncTask.class.toString();
    private StockDetailsActivity stockDetailsActivity;
    private String symbol;
    private int max = Integer.MIN_VALUE;
    private int min = Integer.MAX_VALUE;

    public HistoryAsyncTask(StockDetailsActivity activity, String stockSymbol){
        stockDetailsActivity = activity;
        symbol = stockSymbol;
    }

    @Override
    protected ParcelableLineSet doInBackground(Void... params) {
        try {
            Calendar from = Calendar.getInstance();
            Calendar to = Calendar.getInstance();
            from.add(Calendar.WEEK_OF_MONTH, -2);

            Stock stock = YahooFinance.get(symbol, from, to, Interval.DAILY);

            ParcelableLineSet stockHistory = new ParcelableLineSet();
            Iterator<HistoricalQuote> iterator = stock.getHistory().listIterator();
            HistoricalQuote tmp;
            while(iterator.hasNext()){
                tmp = iterator.next();
                String date = tmp.getDate().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " " + tmp.getDate().get(Calendar.DATE);
                stockHistory.addPoint(date, tmp.getAdjClose().floatValue());
                int value = tmp.getAdjClose().intValue();
                max = value > max ? value : max;
                min = value < min ? value : min;
            }
            return stockHistory;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(ParcelableLineSet stockHistory) {
        super.onPostExecute(stockHistory);
        if(stockHistory != null){
            stockDetailsActivity.onTaskCompleted(stockHistory, max, min);
        }
    }
}
