package com.info.ghiny.examsystem.manager;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.info.ghiny.examsystem.LinkChiefActivity;
import com.info.ghiny.examsystem.MainLoginActivity;
import com.info.ghiny.examsystem.database.ExternalDbLoader;
import com.info.ghiny.examsystem.interfacer.LinkChiefMVP;
import com.info.ghiny.examsystem.model.JavaHost;
import com.info.ghiny.examsystem.model.JsonHelper;
import com.info.ghiny.examsystem.model.ProcessException;

/**
 * Copyright (C) 2016 - 2017 Steven Foong Ghin Yew <stevenfgy@yahoo.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class LinkChiefPresenter implements LinkChiefMVP.PresenterFace, LinkChiefMVP.MPresenter {
    private LinkChiefMVP.ViewFace taskView;
    private LinkChiefMVP.ModelFace taskModel;
    private Handler handler;
    private boolean reconnect = false;
    private boolean requestComplete = false;

    private SharedPreferences preferences;
    private boolean crossHair;
    private boolean beep;
    private boolean vibrate;
    private int mode;
    private int waitTime;


    public LinkChiefPresenter(LinkChiefMVP.ViewFace taskView, SharedPreferences pref){
        this.taskView       = taskView;
        this.preferences    = pref;
    }

    @Override
    public boolean isRequestComplete() {
        return requestComplete;
    }

    @Override
    public void setRequestComplete(boolean requestComplete) {
        this.requestComplete = requestComplete;
    }

    public void setTaskModel(LinkChiefMVP.ModelFace taskModel) {
        this.taskModel = taskModel;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    boolean isReconnect() {
        return reconnect;
    }

    @Override
    public void onCreate(){
        reconnect = taskModel.tryConnectWithDatabase();
    }

    @Override
    public void loadSetting() {
        crossHair   = preferences.getBoolean("CrossHair", true);
        beep        = preferences.getBoolean("Beep", false);
        vibrate     = preferences.getBoolean("Vibrate", false);
        mode        = Integer.parseInt(preferences.getString("ScannerMode", "1"));
        waitTime    = Integer.parseInt(preferences.getString("PacketWaitTime", "5000"));

        taskView.changeScannerSetting(crossHair, beep, vibrate, mode);
    }

    @Override
    public void onResume(final ErrorManager errManager) {
        if(reconnect){
            while(ExternalDbLoader.getJavaHost() == null){}

            try{
                if(taskModel.reconnect()){
                    taskView.openProgressWindow("RECONNECTION", "Authenticating...");
                    handler.postDelayed(taskModel, waitTime);
                }
                reconnect = false;
            } catch (ProcessException err) {
                taskView.displayError(err);
            }

            ExternalDbLoader.getJavaHost().setTaskView(taskView);
            ExternalDbLoader.getJavaHost().setMessageListener(new JavaHost.OnMessageReceived() {
                //here the messageReceived method is implemented
                @Override
                public void messageReceived(String message) {
                    onChiefRespond(errManager, message);
                }
            });
        }
        this.onResume();
    }

    @Override
    public void onResume() {
        taskView.resumeScanning();
        loadSetting();
    }

    @Override
    public void onScan(String scanStr){
        try{
            taskView.pauseScanning();
            taskView.beep();
            taskModel.tryConnectWithQR(scanStr);
            taskView.openProgressWindow("Connecting", "Establishing connection to Host!");
            handler.postDelayed(establishCheck, waitTime);
        } catch (ProcessException err) {
            taskView.displayError(err);
            if(err.getErrorType() == ProcessException.MESSAGE_TOAST)
                taskView.resumeScanning();
        }
    }

    @Override
    public void onPause() {
        taskView.pauseScanning();
    }

    @Override
    public void onDestroy(){
        try {
            taskModel.closeConnection();
            taskView.closeProgressWindow();
            handler.removeCallbacks(taskModel);
        } catch (Exception e) {
            Log.d(LinkChiefActivity.TAG, e.getMessage());
        }
    }

    @Override
    public void onChiefRespond(ErrorManager errManager, String messageRx) {
        try {
            if(JsonHelper.parseType(messageRx).equals(JsonHelper.TYPE_RECONNECTION)){
                taskView.closeProgressWindow();
                setRequestComplete(true);
                taskModel.onChallengeMessageReceived(messageRx);
                taskView.navigateActivity(MainLoginActivity.class);
            }
        } catch (ProcessException err) {
            ExternalDbLoader.getConnectionTask().publishError(errManager, err);
        }
    }

    @Override
    public void onTimesOut(ProcessException err) {
        if(taskView != null){
            taskView.closeProgressWindow();
            taskView.pauseScanning();
            taskView.displayError(err);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        taskView.resumeScanning();
        dialog.cancel();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        taskView.resumeScanning();
        dialog.cancel();
    }

    private Runnable establishCheck = new Runnable() {
        @Override
        public void run() {
            if(ExternalDbLoader.getJavaHost().isConnected()){
                taskView.closeProgressWindow();
                taskView.navigateActivity(MainLoginActivity.class);
            } else {
                taskView.closeProgressWindow();
                taskView.displayError(new ProcessException("Connection failed!",
                        ProcessException.MESSAGE_DIALOG, IconManager.WARNING));
            }
        }
    };
}
