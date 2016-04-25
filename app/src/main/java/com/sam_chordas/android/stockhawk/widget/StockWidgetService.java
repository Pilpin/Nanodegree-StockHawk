package com.sam_chordas.android.stockhawk.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.os.Binder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.StockDetailsActivity;

public class StockWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ViewFactory(getApplicationContext());
    }

    private class ViewFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context mContext;
        private Cursor cursor;

        public ViewFactory(Context c){
            mContext = c;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            if(cursor != null){
                cursor.close();
            }
            final long token = Binder.clearCallingIdentity();
            cursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                    QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP}, QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);
            Binder.restoreCallingIdentity(token);
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            if(cursor != null){
                return cursor.getCount();
            }
            return 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if(position == AdapterView.INVALID_POSITION || cursor == null || !cursor.moveToPosition(position) ){
                return null;
            }

            String symbol, bidPrice;

            symbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
            bidPrice = cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE));

            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
            rv.setTextViewText(R.id.stock_symbol, symbol);
            rv.setTextViewText(R.id.bid_price, bidPrice);

            if(cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP)) == 1) {
                rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }
            if (Utils.showPercent) {
                rv.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
            } else {
                rv.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE)));
            }

            final Intent fillInIntent = new Intent();
            fillInIntent.putExtra("symbol", cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));
            rv.setOnClickFillInIntent(R.id.stock_item, fillInIntent);

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if(cursor != null && cursor.moveToPosition(position)){
                return cursor.getInt(cursor.getColumnIndex(QuoteColumns._ID));
            }
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
