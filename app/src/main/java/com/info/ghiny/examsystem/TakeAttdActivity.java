package com.info.ghiny.examsystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.info.ghiny.examsystem.database.LocalDbLoader;
import com.info.ghiny.examsystem.interfacer.TakeAttdMVP;
import com.info.ghiny.examsystem.manager.TakeAttdPresenter;
import com.info.ghiny.examsystem.manager.ConfigManager;
import com.info.ghiny.examsystem.manager.ErrorManager;
import com.info.ghiny.examsystem.view_holder.OnSwipeAnimator;
import com.info.ghiny.examsystem.model.TakeAttdModel;
import com.info.ghiny.examsystem.model.ProcessException;
import com.info.ghiny.examsystem.view_holder.OnSwipeListener;
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

public class TakeAttdActivity extends AppCompatActivity implements TakeAttdMVP.View{
    public static final String TAG = TakeAttdActivity.class.getSimpleName();

    //Required Tools
    private TakeAttdMVP.VPresenter taskPresenter;
    private ErrorManager errManager;

    private ProgressDialog progDialog;
    private TextView tableView;
    private TextView cddView;
    private TextView regNumView;
    private TextView paperView;
    private ImageView tagIndicator;

    private RelativeLayout help;
    private boolean helpDisplay;
    private RelativeLayout cddLayout;
    private RelativeLayout bottomLayout;

    private int mode;
    private ImageView crossHairView;
    private FloatingActionButton scanInitiater;
    private FloatingActionButton tagButton;
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
        setContentView(R.layout.activity_take_attd);

        initMVP();
        initView();
        //Barcode Viewer
        taskPresenter.loadSetting();
        barcodeView.decodeContinuous(callback);
        assert barcodeView != null;
    }

    private void initView(){
        barcodeView     = (BarcodeView) findViewById(R.id.assignScanner);
        errManager      = new ErrorManager(this);
        bottomLayout    = (RelativeLayout)findViewById(R.id.assignInfoBarcodeLayout);
        cddLayout       = (RelativeLayout)findViewById(R.id.tableInfoLayout);
        cddView         = (TextView)findViewById(R.id.canddAssignText);
        regNumView      = (TextView)findViewById(R.id.regNumAssignText);
        paperView       = (TextView)findViewById(R.id.paperAssignText);
        tableView       = (TextView)findViewById(R.id.tableNumberText);
        tagIndicator    = (ImageView)findViewById(R.id.assignImageTag);
        help            = (RelativeLayout) findViewById(R.id.takeAttdHelpContext);

        scanInitiater   = (FloatingActionButton) findViewById(R.id.takeAttdScanButton);
        tagButton       = (FloatingActionButton) findViewById(R.id.lateTagButton);
        crossHairView   = (ImageView) findViewById(R.id.takeAttdCrossHair);

        cddLayout.setOnTouchListener(new OnSwipeAnimator(this, cddLayout, this));
        bottomLayout.setOnTouchListener(new OnSwipeListener(this){
            @Override
            public void onSwipeLeft() {
                taskPresenter.onSwipeLeft();
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
        SharedPreferences preferences   = PreferenceManager.getDefaultSharedPreferences(this);
        LocalDbLoader dbLoader          = new LocalDbLoader(this);
        TakeAttdPresenter presenter     = new TakeAttdPresenter(this, preferences);
        TakeAttdModel model             = new TakeAttdModel(presenter);
        presenter.setTaskModel(model);
        presenter.setSynTimer(new Handler());

        taskPresenter   = presenter;
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
        super.onResume();
        taskPresenter.onResume(errManager);
    }

    @Override
    protected void onPause() {
        super.onPause();
        taskPresenter.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        taskPresenter.onRestart();
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
    public void onInitiateScan(View view) {
        barcodeView.resume();
    }

    @Override
    public void onTag(View view){
        taskPresenter.onTag(view);
    }

    //= MVP Interface Method =======================================================================


    @Override
    public void onSwiped() {
        taskPresenter.onSwiped(tagButton);
    }

    @Override
    public void setTableView(String tableNum) {
        tableView.setTypeface(Typeface.createFromAsset(getAssets(), ConfigManager.THICK_FONT));
        tableView.setText(tableNum);
    }

    @Override
    public void setCandidateView(String cddIndex, String cddRegNum, String cddPaper) {
        cddView.setTypeface(Typeface.createFromAsset(getAssets(), ConfigManager.BOLD_FONT));
        regNumView.setTypeface(Typeface.createFromAsset(getAssets(), ConfigManager.DEFAULT_FONT));
        paperView.setTypeface(Typeface.createFromAsset(getAssets(), ConfigManager.DEFAULT_FONT));

        cddView.setText(cddIndex);
        regNumView.setText(cddRegNum);
        paperView.setText(cddPaper);
    }

    @Override
    public void navigateActivity(Class<?> cls) {
        Intent nextAct  = new Intent(this, cls);
        startActivity(nextAct);
    }

    @Override
    public void displayError(ProcessException err) {
        errManager.displayError(err);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void runItSeparate(Runnable runner) {
        runOnUiThread(runner);
    }

    @Override
    public void securityPrompt(boolean cancellable) {
        Intent secure   = new Intent(this, PopUpLogin.class);
        secure.putExtra("Cancellable", cancellable);
        startActivityForResult(secure, PopUpLogin.PASSWORD_REQ_CODE);
    }

    @Override
    public void beep() {}

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
    public void pauseScanning() {
        barcodeView.pause();
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
    public void setAssignBackgroundColor(int color) {
        GradientDrawable bg = (GradientDrawable) cddLayout.getBackground();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bg.setColor(getResources().getColor(color, null));
        } else {
            bg.setColor(getResources().getColor(color));
        }
    }

    @Override
    public void setTagButton(boolean showAntiTag) {
        if (showAntiTag){
            tagIndicator.setVisibility(View.VISIBLE);
            tagButton.setImageResource(R.drawable.button_untag_icon);
        } else {
            tagIndicator.setVisibility(View.INVISIBLE);
            tagButton.setImageResource(R.drawable.button_tag_icon);
        }
    }

    @Override
    public void run() {
        barcodeView.resume();
    }
}