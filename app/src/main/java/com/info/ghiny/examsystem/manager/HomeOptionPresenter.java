package com.info.ghiny.examsystem.manager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;

import com.info.ghiny.examsystem.AttdReportActivity;
import com.info.ghiny.examsystem.CollectionActivity;
import com.info.ghiny.examsystem.DistributionActivity;
import com.info.ghiny.examsystem.InfoGrabActivity;
import com.info.ghiny.examsystem.PopUpLogin;
import com.info.ghiny.examsystem.SettingActivity;
import com.info.ghiny.examsystem.TakeAttdActivity;
import com.info.ghiny.examsystem.database.Candidate;
import com.info.ghiny.examsystem.database.ExternalDbLoader;
import com.info.ghiny.examsystem.interfacer.HomeOptionMVP;
import com.info.ghiny.examsystem.model.JavaHost;
import com.info.ghiny.examsystem.model.JsonHelper;
import com.info.ghiny.examsystem.model.ProcessException;
import com.info.ghiny.examsystem.model.TakeAttdModel;

import java.util.ArrayList;

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

public class HomeOptionPresenter implements HomeOptionMVP.MvpVPresenter, HomeOptionMVP.MvpMPresenter {

    private boolean secureFlag;
    private boolean navFlag;
    private HomeOptionMVP.MvpView taskView;
    private HomeOptionMVP.MvpModel taskModel;
    private Handler handler;

    public HomeOptionPresenter(HomeOptionMVP.MvpView taskView){
        this.taskView   = taskView;
        this.secureFlag = false;
        this.navFlag    = true;
    }

    void setNavFlag(boolean flag){
        navFlag = flag;
    }

    boolean isNavFlag() {
        return navFlag;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setTaskModel(HomeOptionMVP.MvpModel taskModel) {
        this.taskModel = taskModel;
    }

    @Override
    public void onResume(final ErrorManager errManager) {
        ExternalDbLoader.getJavaHost().setTaskView(taskView);
        ExternalDbLoader.getJavaHost().setMessageListener(new JavaHost.OnMessageReceived() {
            //here the messageReceived method is implemented
            @Override
            public void messageReceived(String message) {
                onChiefRespond(errManager, message);
            }
        });
        if(TakeAttdModel.getAttdList() == null){
            try{
                taskModel.initAttendance();
            } catch (ProcessException err) {
                taskView.displayError(err);
            }
        }
    }

    @Override
    public void onRestart() {
        if(!secureFlag && !navFlag){
            secureFlag = true;
            taskView.securityPrompt(false);
        }
        navFlag = false;
    }

    @Override
    public void onDestroy() {
        taskModel.saveAttendance();
        taskView.closeProgressWindow();
        handler.removeCallbacks(taskModel);
    }

    @Override
    public void onPasswordReceived(int requestCode, int resultCode, Intent data) {
        if(requestCode == PopUpLogin.PASSWORD_REQ_CODE && resultCode == Activity.RESULT_OK){
            secureFlag      = false;
            String password = data.getStringExtra("Password");
            try{
                taskModel.matchPassword(password);
            } catch(ProcessException err){
                taskView.displayError(err);
                secureFlag = true;
                taskView.securityPrompt(false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        taskView.displayError(taskModel.prepareLogout());
    }

    @Override
    public boolean onSetting() {
        navFlag = true;
        taskView.navigateActivity(SettingActivity.class);
        return true;
    }

    @Override
    public void onAttendance() {
        navFlag = true;
        taskView.navigateActivity(TakeAttdActivity.class);
    }

    @Override
    public void onCollection() {
        navFlag = true;
        taskView.navigateActivity(CollectionActivity.class);
    }

    @Override
    public void onDistribution() {
        navFlag = true;
        taskView.navigateActivity(DistributionActivity.class);
    }

    @Override
    public void onReport() {
        navFlag = true;
        taskView.navigateActivity(AttdReportActivity.class);
    }

    @Override
    public void onInfo() {
        navFlag = true;
        taskView.navigateActivity(InfoGrabActivity.class);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch(which){
            case DialogInterface.BUTTON_POSITIVE:
                taskView.finishActivity();
                break;
            default:
                dialog.cancel();
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        dialog.cancel();
    }


    @Override
    public void onChiefRespond(ErrorManager errManager, String messageRx) {
        try {
            String type = JsonHelper.parseType(messageRx);
            if(type.equals(JsonHelper.TYPE_VENUE_INFO)){
                taskView.closeProgressWindow();//Might Change
                taskModel.checkDownloadResult(messageRx);
            } else if(type.equals(JsonHelper.TYPE_ATTENDANCE_UP)){
                ArrayList<Candidate> candidates = JsonHelper.parseUpdateList(messageRx);
                TakeAttdModel.rxAttendanceUpdate(candidates);
                ExternalDbLoader.acknowledgeUpdateReceive();
            }
        } catch (ProcessException err) {
            ExternalDbLoader.getConnectionTask().publishError(errManager, err);
        }
    }

    @Override
    public void onTimesOut(ProcessException err) {
        if(taskView != null){
            taskView.closeProgressWindow();
            taskView.displayError(err);
        }
    }

    @Override
    public void notifyDatabaseFound() {
        ProcessException err = new ProcessException("Previous Attendance List was found " +
                "in database.\nDo you want to restore it?",
                ProcessException.YES_NO_MESSAGE, IconManager.MESSAGE);
        err.setListener(ProcessException.yesButton, restoreYes);
        err.setListener(ProcessException.noButton, restoreNo);

        taskView.displayError(err);
    }

    @Override
    public void notifyDownloadInfo() {
        taskView.openProgressWindow("Preparing Attendance List:", "Retrieving data...");
        handler.postDelayed(taskModel, 5000);
    }

    private DialogInterface.OnClickListener restoreYes  = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try{
                taskModel.restoreInfo();
            } catch (ProcessException err){
                taskView.displayError(err);
            }
        }
    };

    private DialogInterface.OnClickListener restoreNo   = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try{
                taskModel.downloadInfo();
            } catch (ProcessException err) {
                taskView.displayError(err);
            }
        }
    };
}
