package com.ncku.netdb.medical.ViMyHB;


import android.os.Environment;

import java.util.ArrayList;

public class Config {

    public static final String defaultCodeRoot = "file:///android_asset/web/code/";
    public static final String defaultHtmlUrl =  defaultCodeRoot + "index.html";
    public static final String rootDir = Environment.getExternalStorageDirectory()
            + "/Android/data/VMyHB/";
    public static final String codeDir = rootDir + "code/";
    public static final String htmlUrl = rootDir + "code/index.html";

    // api
    public static final String Host = "http://52.68.170.188/";

    // data post
    public static final String dataSaveUrl = "users/data/save/compress_data/";
    public static final String checkVersionUrl = "version/check/";

    // User
    public static final String signUpUrl = "users/signup/";
    public static final String loginUrl = "users/signin/";
    public static final String signOutUrl = "users/signout/";

    // graph
    public static final String graphCalendarUrl = "graph/calendar/";
    public static final String graphPushUrl = "graph/broadcast/";
    public static final String graphTableImgCheckUrl = "graph/img_check/";
    public static final String graphMedicalUrl = "graph/medical_log/";
    public static final String graphSummaryUrl = "graph/summary/";

    // data path
    public static final String dataMedicalPath = rootDir + "data/fake.json";
    public static final String dataCalendarPath = rootDir + "data/calendar.json";
    public static final String dataTablePath = rootDir + "data/table.json";
    public static final String dataPushPath = rootDir + "data/push.json";
    public static final String dataInfoPath = rootDir + "data/info.json";

    public static final ArrayList<String> graphAPI = new ArrayList<String>() {{
        add(graphCalendarUrl);
        add(graphPushUrl);
        add(graphTableImgCheckUrl);
        add(graphMedicalUrl);
        add(graphSummaryUrl);
    }};

    public static final boolean DEBUG = true;

}
