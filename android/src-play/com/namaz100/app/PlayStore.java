package com.namaz100.app;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.Collections;
import java.util.List;

/** Google Play Billing for the one-time "challenge_100" product. */
public class PlayStore implements Store {
    static final String PRODUCT = "challenge_100";

    private final MainActivity act;
    private final BillingClient client;
    private ProductDetails details;
    private boolean owned, pending;
    private String price = "", error = "";

    public PlayStore(MainActivity activity) {
        act = activity;
        client = BillingClient.newBuilder(activity)
            .setListener((result, purchases) -> {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    handle(purchases);
                } else if (result.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    owned = true; report();
                } else if (result.getResponseCode() != BillingClient.BillingResponseCode.USER_CANCELED) {
                    error = "Purchase did not complete (" + result.getResponseCode() + "). Please try again.";
                    report();
                }
            })
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build();
    }

    private void connect(final Runnable then) {
        if (client.isReady()) { then.run(); return; }
        client.startConnection(new BillingClientStateListener() {
            @Override public void onBillingSetupFinished(BillingResult r) {
                if (r.getResponseCode() == BillingClient.BillingResponseCode.OK) then.run();
                else { error = "Google Play is not available right now."; report(); }
            }
            @Override public void onBillingServiceDisconnected() { }
        });
    }

    @Override public void refresh() {
        connect(() -> {
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                (r, list) -> { if (r.getResponseCode() == BillingClient.BillingResponseCode.OK) handle(list); else report(); });
            QueryProductDetailsParams q = QueryProductDetailsParams.newBuilder()
                .setProductList(Collections.singletonList(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT).setProductType(BillingClient.ProductType.INAPP).build()))
                .build();
            client.queryProductDetailsAsync(q, (r, result) -> {
                List<ProductDetails> l = result.getProductDetailsList();
                if (r.getResponseCode() == BillingClient.BillingResponseCode.OK && l != null && !l.isEmpty()) {
                    details = l.get(0);
                    ProductDetails.OneTimePurchaseOfferDetails o = details.getOneTimePurchaseOfferDetails();
                    if (o != null) price = o.getFormattedPrice();
                }
                report();
            });
        });
    }

    @Override public void buy() {
        error = "";
        connect(() -> {
            if (details == null) { error = "The challenge is not available for purchase yet. Please try again later."; report(); return; }
            BillingFlowParams p = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(
                    BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()))
                .build();
            act.runOnUiThread(() -> client.launchBillingFlow(act, p));
        });
    }

    private void handle(List<Purchase> purchases) {
        boolean found = false, wait = false;
        for (Purchase p : purchases) {
            if (!p.getProducts().contains(PRODUCT)) continue;
            if (p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                found = true;
                if (!p.isAcknowledged()) {
                    client.acknowledgePurchase(
                        AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.getPurchaseToken()).build(), r -> { });
                }
            } else if (p.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                wait = true;
            }
        }
        owned = found; pending = wait && !found;
        report();
    }

    private void report() {
        act.sendStoreState("{\"play\":true,\"owned\":" + owned + ",\"pending\":" + pending
            + ",\"ready\":" + (details != null) + ",\"price\":" + MainActivity.jsonStr(price)
            + ",\"error\":" + MainActivity.jsonStr(error) + "}");
    }
}
