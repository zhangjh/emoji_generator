package me.zhangjh.emoji.generator;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSONObject;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.common.collect.ImmutableList;

import org.apache.commons.collections4.CollectionUtils;

import java.util.function.Function;

public class GooglePayService {

    private final Activity activity;
    private static BillingClient billingClient;

    // 购买后的回调函数
    private final Function<Purchase, Object> handlePurchase;

    public GooglePayService(Activity activity, Function<Purchase, Object> function) {
        this.activity = activity;
        this.handlePurchase = function;
    }

    public void getClient(Context context) {
        if(billingClient == null) {
            PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
                // success
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && CollectionUtils.isNotEmpty(purchases)) {
                    for (Purchase purchase : purchases) {
                        // handlePurchase
                        handlePurchase.apply(purchase);
                    }
                } else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.

                } else {
                    // Handle any other error codes.

                }
            };

            billingClient = BillingClient.newBuilder(context)
                    .setListener(purchasesUpdatedListener)
                    .enablePendingPurchases()
                    .build();
        }
        connectPlay();
    }

    private void connectPlay() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                // 断了重连
                billingClient.startConnection(this);
            }
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                System.out.println(JSONObject.toJSONString(billingResult));
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    launch();
                }
            }
        });
    }

    private QueryProductDetailsParams queryItems() {
        return QueryProductDetailsParams.newBuilder()
                .setProductList(ImmutableList.of(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("download_emoji_pic")
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()
                ))
                .build();
    }

    public void launch() {
        billingClient.queryProductDetailsAsync(this.queryItems(),
            (billingResult, list) -> {
                // process returned productDetailsList
                for (ProductDetails details : list) {
                    ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamList =
                            ImmutableList.of(BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(details)
                                    .build()
                    );
                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamList)
                            .build();
                    BillingResult flowLaunchResult = billingClient.launchBillingFlow(activity, flowParams);
                    if(flowLaunchResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        // 发起购买流程成功
                        Log.d("google pay", "start launch flow success");
                    }
                }
            });
    }
}
