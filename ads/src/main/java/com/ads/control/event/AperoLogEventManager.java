package com.ads.control.event;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.ads.control.util.AppUtil;
import com.ads.control.util.SharePreferenceUtils;
import com.applovin.mediation.MaxAd;
import com.google.android.gms.ads.AdValue;

/**
 * Created by lamlt on 12/09/2022.
 */
public class AperoLogEventManager {

    private static final String TAG = "AperoLogEventManager";

    public static void logPaidAdImpression(Context context, AdValue adValue, String adUnitId, String mediationAdapterClassName) {
        logEventWithAds(context, (float) adValue.getValueMicros(), adValue.getPrecisionType(), adUnitId, mediationAdapterClassName);
    }

    public static void logPaidAdImpression(Context context, MaxAd adValue) {
        logEventWithAds(context, (float) adValue.getRevenue(), 0, adValue.getAdUnitId(), adValue.getNetworkName());
    }

    private static void logEventWithAds(Context context, float revenue, int precision, String adUnitId, String network) {
        Log.d(TAG, String.format(
                "Paid event of value %.0f microcents in currency USD of precision %s%n occurred for ad unit %s from ad network %s.",
                revenue,
                precision,
                adUnitId,
                network));

        Bundle params = new Bundle(); // Log ad value in micros.
        params.putDouble("valuemicros", revenue);
        params.putString("currency", "USD");
        // These values below won’t be used in ROAS recipe.
        // But log for purposes of debugging and future reference.
        params.putInt("precision", precision);
        params.putString("adunitid", adUnitId);
        params.putString("network", network);

        // log revenue this ad
        logPaidAdImpressionValue(context, revenue / 1000000.0, precision, adUnitId, network);
        FirebaseAnalyticsUtil.logEventWithAds(context,params);
        FacebookEventUtils.logEventWithAds(context,params);
        // update current tota
        // l revenue ads
        SharePreferenceUtils.updateCurrentTotalRevenueAd(context, (float) revenue);
        logCurrentTotalRevenueAd(context, "event_current_total_revenue_ad");

        // update current total revenue ads for event paid_ad_impression_value_0.01
        AppUtil.currentTotalRevenue001Ad += revenue;
        SharePreferenceUtils.updateCurrentTotalRevenue001Ad(context, AppUtil.currentTotalRevenue001Ad);
        logTotalRevenue001Ad(context);

        logTotalRevenueAdIn3DaysIfNeed(context);
        logTotalRevenueAdIn7DaysIfNeed(context);
    }

    private static void logPaidAdImpressionValue(Context context, double value, int precision, String adunitid, String network) {
        Bundle params = new Bundle();
        params.putDouble("value", value);
        params.putString("currency", "USD");
        params.putInt("precision", precision);
        params.putString("adunitid", adunitid);
        params.putString("network", network);

        FirebaseAnalyticsUtil.logPaidAdImpressionValue(context, params);
        FacebookEventUtils.logPaidAdImpressionValue(context, params);
    }

    public static void logClickAdsEvent(Context context, String adUnitId) {
        Log.d(TAG, String.format(
                "User click ad for ad unit %s.",
                adUnitId));
        Bundle bundle = new Bundle();
        bundle.putString("ad_unit_id", adUnitId);

        FirebaseAnalyticsUtil.logClickAdsEvent(context, bundle);
        FacebookEventUtils.logClickAdsEvent(context, bundle);
    }

    public static void logCurrentTotalRevenueAd(Context context, String eventName) {
        float currentTotalRevenue = SharePreferenceUtils.getCurrentTotalRevenueAd(context);
        Bundle bundle = new Bundle();
        bundle.putFloat("value", currentTotalRevenue);

        FirebaseAnalyticsUtil.logCurrentTotalRevenueAd(context, eventName, bundle);
        FacebookEventUtils.logCurrentTotalRevenueAd(context, eventName, bundle);
    }


    public static void logTotalRevenue001Ad(Context context) {
        float revenue = AppUtil.currentTotalRevenue001Ad;
        if (revenue / 1000000 >= 0.01) {
            AppUtil.currentTotalRevenue001Ad = 0;
            SharePreferenceUtils.updateCurrentTotalRevenue001Ad(context, 0);
            Bundle bundle = new Bundle();
            bundle.putFloat("value", revenue / 1000000);
            FirebaseAnalyticsUtil.logTotalRevenue001Ad(context, bundle);
            FacebookEventUtils.logTotalRevenue001Ad(context, bundle);
        }
    }

    public static void logTotalRevenueAdIn3DaysIfNeed(Context context) {
        long installTime = SharePreferenceUtils.getInstallTime(context);
        if (!SharePreferenceUtils.isPushRevenue3Day(context)
                && (System.currentTimeMillis() - installTime >= 3L * 24 * 60 * 60 * 1000)) {
            Log.d(TAG, "logTotalRevenueAdAt3DaysIfNeed: ");
            logCurrentTotalRevenueAd(context, "event_total_revenue_ad_in_3_days");
            SharePreferenceUtils.setPushedRevenue3Day(context);
        }
    }

    public static void logTotalRevenueAdIn7DaysIfNeed(Context context) {
        long installTime = SharePreferenceUtils.getInstallTime(context);
        if (!SharePreferenceUtils.isPushRevenue7Day(context)
                && (System.currentTimeMillis() - installTime >= 7L * 24 * 60 * 60 * 1000)) {
            Log.d(TAG, "logTotalRevenueAdAt7DaysIfNeed: ");
            logCurrentTotalRevenueAd(context, "event_total_revenue_ad_in_7_days");
            SharePreferenceUtils.setPushedRevenue7Day(context);
        }
    }


    public static void setEventNamePurchaseAdjust(String eventNamePurchase) {
        AdjustApero.setEventNamePurchase(eventNamePurchase);
    }

    public static void trackAdRevenue(String id) {
        AdjustApero.trackAdRevenue(id);
    }

    public static void onTrackEvent(String eventName) {
        AdjustApero.onTrackEvent(eventName);
    }

    public static void onTrackEvent(String eventName, String id) {
        AdjustApero.onTrackEvent(eventName, id);
    }

    public static void onTrackRevenue(String eventName, float revenue, String currency) {
        AdjustApero.onTrackRevenue(eventName, revenue, currency);
    }

    public static void onTrackRevenuePurchase(float revenue, String currency) {
        AdjustApero.onTrackRevenuePurchase(revenue, currency);
    }

    public static void pushTrackEventAdmob(AdValue adValue) {
        AdjustApero.pushTrackEventAdmob(adValue);
    }

    public static void pushTrackEventApplovin(MaxAd ad, Context context) {
        AdjustApero.pushTrackEventApplovin(ad, context);
    }
}
