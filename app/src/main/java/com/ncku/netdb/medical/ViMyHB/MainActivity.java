package com.ncku.netdb.medical.ViMyHB;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;


public class MainActivity extends ActionBarActivity {


    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private final String scanSuccess = "掃描成功";
    public static WebView webview;
    public User user;
    private UnzipUtility unZipper = new UnzipUtility();
    private static  SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = getSharedPreferences("main", Context.MODE_PRIVATE);
        if (sharedPref.getBoolean("init", true)){
            removeOldDir ();
        }
        initDirectory();

        user = new User(this);
        User.isLogin = getLoginState();

        if (!User.isLogin) {
            isNetwork();
            setContentView(R.layout.init_select);
            Button signUpButton;
            Button loginButton;

            loginButton = (Button) findViewById(R.id.loginButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    startFistLoginActivity();
                }
            });

            signUpButton = (Button) findViewById(R.id.signUpButton);
            signUpButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    startSignUpActivity();
                }
            });

        }
        else {

            checkUnzip();
            setContentView(R.layout.activity_main);
            webview = new WebView(this);
            setContentView(webview);
            WebSettings webSettings = webview.getSettings();
            webSettings.setJavaScriptEnabled(true);

            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);

            webSettings.setBuiltInZoomControls(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setDisplayZoomControls(true);
            webview.setHorizontalScrollBarEnabled(true);

            webview.addJavascriptInterface(new DataManager(Config.dataMedicalPath), "DataManager");

            // url handle
            WebViewClient mWebClient = new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                        String[] tmp = url.split("/");
                        String page = tmp[tmp.length - 1];
                        String path = getWebLinkPath(page);
                        String dataPath = getDataPath(page);
                        dataPath = Config.rootDir + dataPath;

                        // external link
                        if (url.contains("http")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                            return true;
                        }

                        // load file
                        if (isFileExist(Config.htmlUrl)) {
                            path = "file://" + Config.codeDir + path + "index.html";

                        } else {
                            path = Config.defaultCodeRoot + path + "index.html";
                        }

                        view.addJavascriptInterface(new DataManager(dataPath), "DataManager");
                        view.loadUrl(path);

                    return true;

                }
            };
            webview.setWebViewClient(mWebClient);

            if (isFileExist(Config.htmlUrl)) {
                webview.loadUrl("file://" + Config.htmlUrl);
            } else {
                if (!isMobileNetwork() && !isWifiNetwork()) {
                    Toast.makeText(getApplicationContext(), "ViMyHB有最新套件，請開啟網路下載....",
                            Toast.LENGTH_LONG).show();
                }

                webview.loadUrl(Config.defaultHtmlUrl);
            }


        }


    }

    @Override
    protected void onResume() {
        super.onResume();


        if (isWifiNetwork()) {
            // check new version, download file.
            if (getDownloadStatus() != DownloadManager.STATUS_RUNNING) {
                checkAppVersion();
            }
            syncData();
        }
        else if (isMobileNetwork()){
            syncData();
        }
        checkUnzip();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (DownloadTask.filter != null) {
            registerReceiver(DownloadTask.receiver, DownloadTask.filter);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_qr_reader) {
            startScanQRcode();
            return true;
        }

        if (id == R.id.action_signOut) {
            signOut();
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e){

            }
            finish();
            startActivity(getIntent());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private static AlertDialog showDialog(final Activity act, CharSequence title,
                                          CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {

        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                act.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
            }

        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

            return downloadDialog.show();
    }

    /* QR code reader callback */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

                String contents = data.getStringExtra("SCAN_RESULT");

                Toast.makeText(getApplicationContext(), scanSuccess, Toast.LENGTH_SHORT).show();

                MultipartEntity postParams = new MultipartEntity();
                try {
                    postParams.addPart("data", new StringBody(contents, Charset.forName("UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                User.isScanSuccess = false;
                isNetwork();
                new HttpAsyncTask().execute(Config.Host + Config.dataSaveUrl, "post", postParams);
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){

                }
                if (User.isScanSuccess) {
                    Toast.makeText(getApplicationContext(), "資料上傳成功", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "資料上傳失敗", Toast.LENGTH_SHORT).show();
                }
                startScanQRcode();
            }
            else {

            }
        }
        syncData();
    }

    private void initDirectory () {
        File rootDir = new File(Config.rootDir);
        File zipDir = new File(Config.rootDir, "zip");
        File codeDir = new File(Config.rootDir, "code");
        File dataDir = new File(Config.rootDir, "data");
        if (!rootDir.exists()) {
            rootDir.mkdir();
            codeDir.mkdir();
        }
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        if (!zipDir.exists()) {
            zipDir.mkdir();
        }
    }

    private void checkAppVersion () {
        User.releaseNum = getAppNum();
        String path = new Uri.Builder()
                .path(Config.checkVersionUrl)
                .appendQueryParameter("release_num", User.releaseNum)
                .build().toString();

        new HttpAsyncTask().execute(Config.Host + path, "get", null);
    }

    public void syncData() {
        Iterator<String> iterator = Config.graphAPI.iterator();
        while (iterator.hasNext()) {
            getData(iterator.next());
        }
    }

    public static void getData(String api) {
        String path = new Uri.Builder()
                .path(api)
                .build().toString();

        new HttpAsyncTask().execute(Config.Host + path, "get", null);
    }

   public static boolean isFileExist (String path) {
        File file = new File(path);
        return file.exists();
    }

    public boolean isWifiNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifiNetwork != null && wifiNetwork.isConnected();
    }

    public boolean isMobileNetwork () {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return wifiNetwork != null && wifiNetwork.isConnected();
    }

    private String getDataFromAssets(String filename) {
        StringBuilder buf = new StringBuilder();
        InputStream json;
        try {
            json = getAssets().open(filename);
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;

            while ((str = input.readLine()) != null) {
                buf.append(str);
            }

            input.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }


    private void startFistLoginActivity() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, FirstLoginActivity.class);
        startActivity(intent);
    }

    private void startSignUpActivity() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private String getUserInfo (String key) {
        String value = sharedPref.getString(key,"");
        return value;
    }

    private Boolean getLoginState() {;
        Boolean isLogin = sharedPref.getBoolean(getString(R.string.isLogin), false);
        Log.d("isLogin", isLogin + "");
        if (isLogin) {
            User.userPhoneNumber = getUserInfo(getString(R.string.saved_user_phone_number));
            User.userID = getUserInfo(getString(R.string.saved_user_ID));
            User.password = getUserInfo(getString(R.string.saved_user_password));
            User.postLogin();
        }

        return isLogin;
    }

    private String getDataFromSDCard(String filename) {
        StringBuilder buf = new StringBuilder();
        InputStream data;
        try {
            data = new FileInputStream(new File(filename));
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(data, "UTF-8"));
            String str;

            while ((str = input.readLine()) != null) {
                buf.append(str);
            }

            input.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }

    class DataManager {
        private String dataPath;

        public DataManager (String dataPath) {
            this.dataPath = dataPath;
        }
        @JavascriptInterface
        public String getData() {
            Log.d("data path:", this.dataPath);
            if (isFileExist(this.dataPath)) {
                Log.d("data from:", "user folder");
                return getDataFromSDCard(this.dataPath);
            }
            return null;
        }

        @JavascriptInterface
        public String getTemplate (String filename) {
            String path = Config.rootDir + "code/calendar/" + filename;
            if (isFileExist(path)) {
                Log.d("get template from:", path);
                return getDataFromSDCard(path);
            }
            return null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (webview == null || keyCode != KeyEvent.KEYCODE_BACK
                || webview.getUrl().contains("code/index.html")) {
            // exit app
            return super.onKeyDown(keyCode, event);
        }

        // go back to main page
        if (isFileExist(Config.htmlUrl)) {

            webview.loadUrl("file://" + Config.htmlUrl);
            return true;
        }
        else {
            webview.loadUrl(Config.defaultHtmlUrl);
            return true;
        }

    }

    private void startScanQRcode() {
        isNetwork();
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 1);
        } catch (ActivityNotFoundException e) {
            showDialog(MainActivity.this, "找不到QR Code掃描器",
                    "是否下載一個QR Code掃瞄器", "Yes", "No").show();

        }
    }

    private String getWebLinkPath(String page) {
        String path;
        if (!page.equals("code")) {
            path =  page + "/";
        }
        else {
            path = "";
        }

        return path;
    }


    private String getDataPath(String page) {
        return "data/" + page + ".json";
    }


    private String getAppNum () {
        SharedPreferences sharedPref = getSharedPreferences("main", Context.MODE_PRIVATE);
        String num = sharedPref.getString(getString(R.string.app_num), "0.0");
        return num;
    }

    private int getDownloadStatus() {
        DownloadManager.Query query = null;
        Cursor c = null;
        DownloadManager downloadManager = null;
        downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        query = new DownloadManager.Query();
        if(query!=null) {
            query.setFilterByStatus(DownloadManager.STATUS_FAILED|
                    DownloadManager.STATUS_PAUSED|DownloadManager.STATUS_SUCCESSFUL|
                    DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PENDING);
        } else {
            return -100;
        }
        c = downloadManager.query(query);
        if(c.moveToFirst()) {
           int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
           return status;
        }
        return -100;
    }

    /*
    *  check if zip file exist, then zip it.
    * */
    private void checkUnzip() {
        // app not downloading....
        if (getDownloadStatus() != DownloadManager.STATUS_RUNNING &&
                isFileExist(Config.rootDir + "/zip/code.zip")) {
            try {
                unZipper.unzip(Config.rootDir + "/zip/code.zip",
                        Config.rootDir + "/code");


                if (webview != null) {
                    webview.reload();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void signOut() {
        if (isNetwork()) {
            String path = new Uri.Builder()
                    .path(Config.signOutUrl)
                    .build().toString();
            new HttpAsyncTask().execute(Config.Host + path, "get", null);
            deleteRecursive(new File(Config.rootDir, "data"));
        }
    }

    private boolean isNetwork () {
        boolean hasNetwork = isMobileNetwork() || isWifiNetwork();
        if (!hasNetwork) {
            Toast.makeText(getApplicationContext(), "網路不穩定，請開啟網路重試...", Toast.LENGTH_LONG).show();
        }
        return hasNetwork;
    }

    private void removeOldDir () {
        File rootDir = new File(Config.rootDir);
        if (rootDir.exists()) {
            deleteRecursive(rootDir);
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("init", false);
        editor.commit();
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

}
