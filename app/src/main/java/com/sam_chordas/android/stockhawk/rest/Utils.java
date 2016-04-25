package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON, Context c){
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray resultsArray;
        ArrayList<String> badSymbols = new ArrayList<String>();
        try{
            jsonObject = new JSONObject(JSON);
            if (jsonObject.length() != 0){
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1){
                    jsonObject = jsonObject.getJSONObject("results").getJSONObject("quote");
                    if(!jsonObject.getString("Bid").equals("null")){
                        batchOperations.add(buildBatchOperation(jsonObject));
                    }else{
                        badSymbols.add(jsonObject.getString("symbol").toUpperCase());
                    }
                } else{
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0){
                        for (int i = 0; i < resultsArray.length(); i++){
                            jsonObject = resultsArray.getJSONObject(i);
                            if(!jsonObject.getString("Bid").equals("null")){
                                batchOperations.add(buildBatchOperation(jsonObject));
                            }else{
                                badSymbols.add(jsonObject.getString("symbol"));
                            }
                        }
                    }
                }
            }
        } catch (JSONException e){
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        ListIterator<String> iterator = badSymbols.listIterator();
        StringBuilder stringBuilder = new StringBuilder();
        while(iterator.hasNext()){
            if(iterator.previousIndex() != -1){
                stringBuilder.append(", ");
            }
            stringBuilder.append(iterator.next());
        }
        if(stringBuilder.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(c.getString(R.string.preferences_key_badSymbols), stringBuilder.toString());
            editor.apply();
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice){
        bidPrice = String.format(Locale.getDefault(), "%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange){
        if(change.equals("null")){
            change = "0.00%";
        }
        String weight = change.substring(0,1);
        String ampersand = "";
        if (isPercentChange){
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format(Locale.getDefault(), "%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol").toUpperCase());
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return builder.build();
    }

    public static boolean isConnected(Context c){
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
