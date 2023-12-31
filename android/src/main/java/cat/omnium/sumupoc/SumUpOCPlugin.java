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
import com.sumup.merchant.reader.api.SumUpLogin;
import com.sumup.merchant.reader.api.SumUpAPIHelper;
import com.sumup.merchant.reader.api.SumUpPayment;
import com.sumup.merchant.reader.api.SumUpState;
import com.sumup.merchant.reader.identitylib.ui.activities.LoginActivity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;


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
        Double total = call.getDouble("total");
        SumUpPayment.Currency currency = SumUpPayment.Currency.valueOf(call.getString("currency"));

        if (total == null) {
            return;
        }

        Intent intent = SumUpAPIHelper.getPaymentIntent(getActivity());
        intent.putExtra("total", new BigDecimal(total));
        intent.putExtra("currency", currency.getIsoCode());

        if (call.hasOption("title")) {
            String title = call.getString("title");
            intent.putExtra("title", title);
        }

        if (call.hasOption("receiptEmail")) {
            String receiptEmail = call.getString("receiptEmail");
            intent.putExtra("receipt-email", receiptEmail);
        }

        if (call.hasOption("receiptSMS")) {
            String receiptSMS = call.getString("receiptSMS");
            intent.putExtra("receipt-mobilephone", receiptSMS);
        }

        if (call.hasOption("foreignTransactionId")) {
            String foreignTransactionId = call.getString("foreignTransactionId");
            intent.putExtra("foreign-tx-id", foreignTransactionId);
        }

        if (call.hasOption("skipSuccessScreen")) {
            Boolean skipSuccessScreen = call.getBoolean("skipSuccessScreen");
            intent.putExtra("skip-screen-success", skipSuccessScreen);
        }

        if (call.hasOption("additionalInfo")) {
            JSObject jsAdditionalInfo = call.getObject("additionalInfo");
            HashMap<String, String> additionalInfo = new HashMap<>();
            for (Iterator<String> it = jsAdditionalInfo.keys(); it.hasNext(); ) {
                String key = it.next();
                additionalInfo.put(key, jsAdditionalInfo.getString(key));
            }

            intent.putExtra("addition-info", additionalInfo);
        }

        startActivityForResult(call, intent, "handleResponse");
    }

    @ActivityCallback
    public void handleResponse(PluginCall call, ActivityResult result) {
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
