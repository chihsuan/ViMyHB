package com.ncku.netdb.medical.ViMyHB;


import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class HttpAsyncTask extends AsyncTask<Object, Void, JSONObject> {

    private static List<Cookie> cookies;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected JSONObject doInBackground(Object... params) {

        String url = (String) params[0];
        String type = (String) params[1];

        if (type.equals("get")) {
            return makeGetRequest(url);

        }
        else if (type.equals("post")) {
            MultipartEntity postParams = (MultipartEntity) params[2];
            return  makePostRequest(url, postParams);
        }
        return null;
    }

    protected void onPostExecute(JSONObject result) {

        if (MainActivity.webview != null &&  MainActivity.isFileExist(Config.htmlUrl)) {
            MainActivity.webview.loadUrl("file://" + Config.htmlUrl);

        }

    }

    public DefaultHttpClient getHttpClient() {

        DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
        if (cookies != null) {
            int size = cookies.size();
            Log.i("cookies", cookies.toString());
            for (int i = 0; i < size; i++) {
                httpClient.getCookieStore().addCookie(cookies.get(i));

            }
        }
        return httpClient;
    }


    private JSONObject makeGetRequest(String url) {

        DefaultHttpClient httpClient = getHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response = null;
        Log.d("get:", url);
        try {
            response = httpClient.execute(request);
            cookies = httpClient.getCookieStore().getCookies();

            int status = response.getStatusLine().getStatusCode();
            Log.d("Response of GET request", response.getStatusLine().toString());
            if (status == 200) {
                HttpEntity entity = response.getEntity();
                String data = EntityUtils.toString(entity);
                //Log.d("Response of GET request", data);
                User.setParams(url, data);
                if (data.endsWith("}")) {
                    JSONObject json = new JSONObject(data);
                    return  json;
                }

            }


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (JSONException e) {

            e.printStackTrace();
        }
        return null;
    }

    public JSONObject makePostRequest(String url, MultipartEntity postParams) {

        DefaultHttpClient httpClient = getHttpClient();
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response = null;
        httpPost.setEntity(postParams);

        Log.d("post", url);


        try {
            response = httpClient.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            cookies = httpClient.getCookieStore().getCookies();

            Log.d("status", "code:" + status);
            //Log.d("Http Post Response:", response.toString());
            if (status == 200) {

                HttpEntity entity = response.getEntity();
                String data = EntityUtils.toString(entity);
                User.setParams(url, data);
                if (data.endsWith("}")) {
                    JSONObject json = new JSONObject(data);
                    return json;
                }
            }
            if (status == 400) {
                HttpEntity responseEntity = response.getEntity();
                String data = EntityUtils.toString(responseEntity);
                Log.d("400:", data);

            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {

            e.printStackTrace();
        }
        return null;
    }
}