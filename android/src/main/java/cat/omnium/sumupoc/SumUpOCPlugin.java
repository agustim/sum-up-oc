package cat.omnium.sumupoc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.sumup.merchant.reader.api.SumUpAPI;
import com.sumup.merchant.reader.api.SumUpPayment;
import com.sumup.merchant.reader.api.SumUpState;
import com.sumup.merchant.reader.identitylib.ui.activities.LoginActivity;
import com.sumup.merchant.reader.models.TransactionInfo;
import com.sumup.merchant.reader.ui.activities.CardReaderPageActivity;
import com.sumup.merchant.reader.ui.activities.CardReaderPaymentAPIDrivenPageActivity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;


@CapacitorPlugin(name = "SumUpOC")
public class SumUpOCPlugin extends Plugin {
    
    private static final String TAG = "SumUpOCPlugin";
    private static final int REQUEST_CODE_LOGIN = 1;
    private static final int REQUEST_CODE_PAYMENT = 2;
    private static final int REQUEST_CODE_CARD_READER_PAGE = 4;
    
    @Override
    public void load() {
        Log.d(TAG, "load Plugin");
        SumUpState.init(getContext());
    }

    @PluginMethod
    public void login(PluginCall call) {

        Log.d(TAG, "login");
        String affiliateKey = call.getString("affiliateKey");

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.putExtra("isAffiliate", true);
        intent.putExtra("affiliate-key", affiliateKey);

        Log.d(TAG, "affiliateKey: " + affiliateKey);
        Log.d(TAG, "intent: " + intent.toString());
    
        if (call.hasOption("accessToken")) {
            Log.d(TAG, "hasOption accessToken");
            String accessToken = call.getString("accessToken");
            Log.d(TAG, "accessToken: " + accessToken);
            intent.putExtra("access-token", accessToken);
        }

        startActivityForResult(call, intent, "handleResponse");
    }

    @PluginMethod
    public void logout(PluginCall call) {
        Log.d(TAG, "logout");

        JSObject ret = new JSObject();
        if (SumUpAPI.isLoggedIn()) {
            SumUpAPI.logout();
            ret.put("code", 0);
            ret.put("message", "Logged out");
        } else {
            ret.put("code", 1);
            ret.put("message", "Not logged in");
        } 
        call.resolve(ret);
    }

    @PluginMethod
    public void checkout(PluginCall call) {

        JSObject data = call.getData();

        if (data == null) {
            call.reject("options cannot be empty");
        }

        // We reject if its not, but this does not get recognized
        assert data != null;

        if (!data.has("total")) {
            call.reject("missing total");
        }

        if (!data.has("currency")) {
            call.reject("missing currency");
        }
        SumUpPayment.Currency currency = SumUpPayment.Currency.valueOf(data.getString("currency"));

        Intent CheckoutIntent = new Intent(this.getActivity(), CardReaderPaymentAPIDrivenPageActivity.class);
        CheckoutIntent.putExtra(SumUpAPI.Param.TOTAL, new BigDecimal(data.getString("total")));
        CheckoutIntent.putExtra(SumUpAPI.Param.CURRENCY, currency.getIsoCode());

        if (data.has("title")) {
            CheckoutIntent.putExtra(SumUpAPI.Param.TITLE, data.getString("title"));
        }

        if (data.has("receiptEmail")) {
            CheckoutIntent.putExtra(SumUpAPI.Param.RECEIPT_EMAIL, data.getString("receiptEmail"));
        }

        if (data.has("receiptSMS")) {
            CheckoutIntent.putExtra(SumUpAPI.Param.RECEIPT_PHONE, data.getString("receiptSMS"));
        }

        if (data.has("foreignTransactionId")) {
            CheckoutIntent.putExtra(SumUpAPI.Param.FOREIGN_TRANSACTION_ID, data.getString("foreignTransactionId"));
        } else {
            CheckoutIntent.putExtra(SumUpAPI.Param.FOREIGN_TRANSACTION_ID, UUID.randomUUID().toString());
        }

        if (data.has("skipSuccessScreen")) {
            CheckoutIntent.putExtra(SumUpAPI.Param.SKIP_SUCCESS_SCREEN, true);
        }

        if (data.has("skipFailedScreen")) {
            CheckoutIntent.putExtra(SumUpAPI.Param.SKIP_FAILED_SCREEN, true);
        }

        JSObject info = data.getJSObject("additionalInfo");
        if (info != null) {
            HashMap<String, String> infoObject = new HashMap<>();
            for (Iterator<String> it = info.keys(); it.hasNext(); ) {
                String key = it.next();
                infoObject.put(key, info.getString(key));
            }

            CheckoutIntent.putExtra(SumUpAPI.Param.ADDITIONAL_INFO, infoObject);
        }

        startActivityForResult(call, CheckoutIntent, "handleResponse");
    }

    @ActivityCallback
    public void handleResponse(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }

        Intent data = result.getData();
        if (data == null) {
            return;
        }

        Bundle extrasBundle = data.getExtras();
        if (extrasBundle == null) {
            return;
        }

        int resultCode = extrasBundle.getInt(SumUpAPI.Response.RESULT_CODE);
        String resultMessage = extrasBundle.getString(SumUpAPI.Response.MESSAGE);
        Log.d(TAG, "handleResponse: " + resultCode + " " + resultMessage);
        if (resultCode > 0) {
            JSObject ret = new JSObject();
            ret.put("code", resultCode);
            ret.put("message", resultMessage);
            call.resolve(ret);
        } else {
            call.reject(resultMessage, String.format("%d", resultCode));
        }
    }

}
