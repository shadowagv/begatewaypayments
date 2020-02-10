package com.begateway.mobilepayments.tasks;

import com.begateway.mobilepayments.TransactionType;
import com.begateway.mobilepayments.model.BaseResponse;
import com.begateway.mobilepayments.model.PaymentTokenResponse;
import com.begateway.mobilepayments.model.ResponseCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class RetrievePaymentTokenTask extends BaseRequestTask<PaymentTokenResponse> {

    private IRetrievePaymentTokenTask callback;

    public RetrievePaymentTokenTask setCallback(IRetrievePaymentTokenTask callback) {
        this.callback = callback;

        return this;
    }

    public RetrievePaymentTokenTask fillBodyRequest(boolean isTestMode, int attempts, String orderAmount, String orderCurrency, String orderDescription, String notificationUrl, String returnUrl, TransactionType transactionType) {

        JSONObject rootJson = new JSONObject();

        try {

            JSONObject checkoutJson = new JSONObject();
            JSONObject orderJson = new JSONObject();
            JSONObject orderAdditionalDataJson = new JSONObject();
            JSONObject settingsJson = new JSONObject();

            checkoutJson.put("test", isTestMode);
            checkoutJson.put("transaction_type", transactionType.toString().toLowerCase());
            checkoutJson.put("attempts", attempts);

            orderJson.put("amount", orderAmount);
            orderJson.put("currency", orderCurrency);
            orderJson.put("description", orderDescription);
            JSONArray contractParameters = new JSONArray();
            contractParameters.put("recurring");
            contractParameters.put("card_on_file");
            orderAdditionalDataJson.put("contract", contractParameters);
            orderJson.put("additional_data", orderAdditionalDataJson);

            settingsJson.put("auto_return", true);
            if (notificationUrl != null && notificationUrl.isEmpty() == false) {
                URL url= new URL(notificationUrl);
//                settingsJson.put("notification_url", url);

            }
            if (returnUrl != null && returnUrl.isEmpty() == false) {
                settingsJson.put("return_url", "YOUR_RETURN_URL");
            }

            checkoutJson.put("order", orderJson);
            checkoutJson.put("settings", settingsJson);

            rootJson.put("checkout", checkoutJson);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String jsonBody = rootJson.toString();
        jsonBody = jsonBody.replace("YOUR_RETURN_URL", returnUrl);

        setJsonBody(jsonBody);

        return this;
    }

    @Override
    public BaseResponse getResponseInstance() {
        return new PaymentTokenResponse();
    }

    @Override
    protected void onPostExecute(BaseResponse response) {
        super.onPostExecute(response);

        PaymentTokenResponse targetResponse = (PaymentTokenResponse)response;

        if (response.getStatus() == ResponseCode.SUCCESS) {
            try {

                JSONObject jsonData = response.getRawJson();
                targetResponse.Fill(jsonData);

            } catch (JSONException e) {
                response.setError(e.toString());
            }
        }

        if (callback != null)
        {
            callback.onCallback(targetResponse);
        }
    }
}