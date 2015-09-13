package com.ncku.netdb.medical.ViMyHB;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;

public class DownloadReceiver extends BroadcastReceiver {
    private com.ncku.netdb.medical.ViMyHB.UnzipUtility unZipper = new com.ncku.netdb.medical.ViMyHB.UnzipUtility();
    public DownloadReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // an Intent broadcast.
        try {
            unZipper.unzip(Config.rootDir + "/zip/code.zip", Config.rootDir + "/code");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (MainActivity.webview != null) {
            MainActivity.webview.loadUrl("file://" + Config.htmlUrl);
        }

    }
}
