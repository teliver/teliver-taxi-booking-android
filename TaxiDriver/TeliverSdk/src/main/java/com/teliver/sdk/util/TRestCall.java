
package com.teliver.sdk.util;

import android.content.Context;
import android.os.AsyncTask;

import com.teliver.sdk.models.TConstants;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


final class TRestCall extends AsyncTask<String, Integer, String> {

    private ResponseListener listener;

    private RequestBody requestBody;

    private HTTP_TYPE httpType;

    private TPreference preference;

    enum HTTP_TYPE {
        TYPE_POST, TYPE_PATCH
    }

    interface ResponseListener {
        void onResponse(String result);
    }

    TRestCall(Context context) {
        httpType = HTTP_TYPE.TYPE_POST;
        preference = new TPreference(context);
    }

    void setHttpType(HTTP_TYPE httpType) {
        this.httpType = httpType;
    }


    void setCallBackListener(ResponseListener finishedListener) {
        this.listener = finishedListener;
    }


    void requestApi(String append, RequestBody requestBody) {
        this.requestBody = requestBody;
        this.execute("https://api.teliver.xyz/api/v1/sdk/" + append);
    }

    void requestApi(String url, String append, RequestBody requestBody) {
        this.requestBody = requestBody;
        this.execute(url + append);
    }


    @Override
    protected String doInBackground(String... params) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder request = new Request.Builder();
            request.url(params[0]);
            request.addHeader("Content-Type", "application/x-www-form-urlencoded");
            request.addHeader("app-key", preference.getString(TConstants.API_KEY));
            request.addHeader("Authorization", "Bearer " + preference.getString(TConstants.AUTH_TOKEN));
            if (httpType == HTTP_TYPE.TYPE_POST)
                request.post(requestBody);
            else request.patch(requestBody);
            Response response = client.newCall(request.build()).execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (this.listener != null)
            listener.onResponse(TUtils.clearNull(result));
    }
}