package com.ncku.netdb.medical.ViMyHB;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class User {

    public static Context context;
    public static String releaseNum = "0.0";
    public static String releaseUrl = "";
    public static String userPhoneNumber = "";
    public static String userID = "";
    public static String password = "";
    public static boolean isLogin = false;
    public static boolean isScanSuccess = false;
    private static  SharedPreferences sharedPref;
    private static SharedPreferences.Editor editor;

    public User (Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences("main", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public static void setParams (String url, String data) {
        if (url.equals(Config.Host + Config.dataSaveUrl)) {
            isScanSuccess = true;
        }
        else if (url.contains(Config.checkVersionUrl)) {

            try {
                JSONObject obj = new JSONObject(data);
                String releaseNumTmp = obj.getString("release_num");
                releaseNum  = sharedPref.getString(context.getString(R.string.app_num), releaseNum);
                if (releaseNumTmp != null && !releaseNum.equals(releaseNumTmp)) {
                    releaseNum = releaseNumTmp;
                    updateAppNum(releaseNumTmp);
                    releaseUrl = obj.getString("release_url");

                    DownloadTask downloadTask = new DownloadTask(context);
                    downloadTask.downloadFileFromURL(User.releaseUrl, "ViMyHB下載最新套件",
                            "Please wait a few seconds...", "code.zip");
                }

            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else if (url.contains(Config.signUpUrl)) {
            postLogin();

        }
        else if (url.contains(Config.loginUrl)) {
            isLogin = true;
            editor.putBoolean(context.getString(R.string.isLogin), true);
            editor.commit();
            Log.d("Login", "succeeded");
        }
        else if (url.contains(Config.signOutUrl)) {
            isLogin = false;
            editor.putBoolean(context.getString(R.string.isLogin), false);
            editor.commit();
        }
        else if (url.contains(Config.graphMedicalUrl)) {
            updateData(data, Config.dataMedicalPath);
        }
        else if (url.contains(Config.graphCalendarUrl)) {
            updateData(data, Config.dataCalendarPath);
        }
        else if (url.contains(Config.graphTableImgCheckUrl)) {
            updateData(data, Config.dataTablePath);
        }
        else if (url.contains(Config.graphPushUrl)) {
            updateData(data, Config.dataPushPath);
        }
        else if (url.contains(Config.graphSummaryUrl)) {
            updateData(data, Config.dataInfoPath);
        }


    }

    public static void updateAppNum(String num) {
        editor.putString(context.getString(R.string.app_num), num);
        editor.commit();
    }

    private static void updateData(String data, String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                f.createNewFile();
            }

            FileOutputStream stream = new FileOutputStream(f);
            try {
                stream.write(data.getBytes());
                stream.close();
                if (Config.DEBUG) {
                    Log.d("update data", data);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {

        }
    }

    public static void postLogin () {

        MultipartEntity postParams = new MultipartEntity();
        try {
            postParams.addPart("user_phone", new StringBody(userPhoneNumber));
            postParams.addPart("social_front_4_id", new StringBody(userID));
            postParams.addPart("password", new StringBody(password));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        new HttpAsyncTask().execute(Config.Host + Config.loginUrl, "post", postParams);

    }

}
