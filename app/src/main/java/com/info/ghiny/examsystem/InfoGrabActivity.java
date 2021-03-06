package com.info.ghiny.examsystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.zxing.ResultPoint;
import com.info.ghiny.examsystem.interfacer.InfoGrabMVP;
import com.info.ghiny.examsystem.manager.InfoGrabPresenter;
import com.info.ghiny.examsystem.manager.ErrorManager;
import com.info.ghiny.examsystem.model.InfoGrabModel;
import com.info.ghiny.examsystem.model.JsonHelper;
import com.info.ghiny.examsystem.view_holder.OnSwipeListener;
import com.info.ghiny.examsystem.model.ProcessException;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

import java.util.List;

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

public class InfoGrabActivity extends AppCompatActivity implements InfoGrabMVP.ViewFace {
    private static final String TAG = InfoGrabActivity.class.getSimpleName();

    private InfoGrabMVP.VPresenter taskPresenter;
    private ErrorManager errManager;

    private RelativeLayout help;
    private boolean helpDisplay;

    private int mode;
    private ImageView crossHairView;
    private FloatingActionButton scanInitiater;
    private ProgressDialog progDialog;
    private BarcodeView barcodeView;
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                taskPresenter.onScan(result.getText());
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    //==============================================================================================
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obtain_info);

        initView();
        initMVP();
        taskPresenter.loadSetting();
        barcodeView.decodeContinuous(callback);
    }

    private void initView(){
        barcodeView     = (BarcodeView) findViewById(R.id.obtainScanner);
        scanInitiater   = (FloatingActionButton) findViewById(R.id.grabInfoScanButton);
        crossHairView   = (ImageView) findViewById(R.id.grabInfoCrossHair);
        errManager      = new ErrorManager(this);
        help            = (RelativeLayout) findViewById(R.id.infoHelpContext);

        RelativeLayout thisLayout = (RelativeLayout) findViewById(R.id.obtainInfoLayout);
        assert thisLayout != null;
        thisLayout.setOnTouchListener(new OnSwipeListener(this){
            @Override
            public void onSwipeTop() {
                taskPresenter.onSwipeTop();
            }
        });

        helpDisplay = false;
        help.setVisibility(View.INVISIBLE);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDisplay = false;
                help.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initMVP(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        InfoGrabPresenter presenter = new InfoGrabPresenter(this, preferences);
        InfoGrabModel model     = new InfoGrabModel(presenter);
        presenter.setHandler(new Handler());
        presenter.setTaskModel(model);
        taskPresenter   = presenter;
    }

    @Override
    public void onBackPressed() {
        if(helpDisplay){
            helpDisplay = false;
            help.setVisibility(View.INVISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                helpDisplay = true;
                help.setVisibility(View.VISIBLE);
                return true;
            case R.id.action_setting:
                return taskPresenter.onSetting();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        taskPresenter.onResume(errManager);
        super.onResume();
    }

    @Override
    protected void onPause() {
        taskPresenter.onPause();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        taskPresenter.onRestart();
        super.onRestart();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event)
                || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        taskPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        taskPresenter.onPasswordReceived(requestCode, resultCode, data);
    }

    //==============================================================================================
    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void runItSeparate(Runnable runner) {
        runOnUiThread(runner);
    }

    @Override
    public void displayError(ProcessException err) {
        errManager.displayError(err);
    }

    @Override
    public void securityPrompt(boolean cancellable) {
        Intent secure   = new Intent(this, PopUpLogin.class);
        secure.putExtra("Cancellable", cancellable);
        startActivityForResult(secure, PopUpLogin.PASSWORD_REQ_CODE);
    }

    @Override
    public void navigateActivity(Class<?> cls) {
        Intent displayList = new Intent(this, cls);
        displayList.putExtra(JsonHelper.MINOR_KEY_CANDIDATES, taskPresenter.getStudentSubjects());
        startActivity(displayList);
    }

    @Override
    public void beep() {}

    @Override
    public void pauseScanning() {
        barcodeView.pause();
    }

    @Override
    public void resumeScanning() {
        if(helpDisplay){
            helpDisplay = false;
            help.setVisibility(View.INVISIBLE);
        }
        switch (mode){
            case 2:
                barcodeView.postDelayed(this, 1000);
                break;
            case 3:
                barcodeView.postDelayed(this, 2000);
                break;
            case 4:
                barcodeView.postDelayed(this, 3000);
                break;
        }
    }

    @Override
    public void openProgressWindow(String title, String message) {
        progDialog  = new ProgressDialog(this, R.style.ProgressDialogTheme);
        progDialog.setMessage(message);
        progDialog.setTitle(title);
        progDialog.show();
    }

    @Override
    public void closeProgressWindow() {
        if(progDialog != null)
            progDialog.dismiss();
    }

    @Override
    public void changeScannerSetting(boolean crossHair, boolean beep, boolean vibrate, int mode) {
        if(crossHair){
            this.crossHairView.setVisibility(View.VISIBLE);
        } else {
            this.crossHairView.setVisibility(View.INVISIBLE);
        }

        this.mode   = mode;
        if(mode == 1){
            scanInitiater.setVisibility(View.VISIBLE);
        } else {
            scanInitiater.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onInitiateScan(View view) {
        barcodeView.resume();
    }

    @Override
    public void run() {
        barcodeView.resume();
    }
}
