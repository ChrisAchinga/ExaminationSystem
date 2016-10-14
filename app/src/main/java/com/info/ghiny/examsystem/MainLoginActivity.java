package com.info.ghiny.examsystem;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.info.ghiny.examsystem.database.CheckListLoader;
import com.info.ghiny.examsystem.interfacer.LoginMVP;
import com.info.ghiny.examsystem.manager.LoginPresenter;
import com.info.ghiny.examsystem.manager.ConfigManager;
import com.info.ghiny.examsystem.manager.ErrorManager;
import com.info.ghiny.examsystem.model.LoginModel;
import com.info.ghiny.examsystem.model.ProcessException;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

import java.util.List;

public class MainLoginActivity extends AppCompatActivity implements LoginMVP.View {
    private static final String TAG = MainLoginActivity.class.getSimpleName();

    //private LoginPresenter loginManager;
    private LoginMVP.VPresenter taskPresenter;
    private CheckListLoader dbLoader;
    private ErrorManager errorManager;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);

        initMVP();

        TextView idView = (TextView)findViewById(R.id.identityText);    assert idView  != null;
        idView.setTypeface(Typeface.createFromAsset(this.getAssets(), ConfigManager.DEFAULT_FONT));

        barcodeView.decodeContinuous(callback);
    }

    private void initMVP(){
        barcodeView     = (BarcodeView) findViewById(R.id.loginScanner);
        errorManager    = new ErrorManager(this);
        dbLoader        = new CheckListLoader(this);

        LoginPresenter presenter  = new LoginPresenter(this);
        LoginModel model       = new LoginModel(presenter, dbLoader);
        presenter.setHandler(new Handler());
        presenter.setTaskModel(model);
        taskPresenter           = presenter;
    }

    @Override
    protected void onResume() {
        taskPresenter.onResume(errorManager);
        super.onResume();
    }

    @Override
    protected void onPause() {
        taskPresenter.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        taskPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event)
                || super.onKeyDown(keyCode, event);
    }

    //==============================================================================================
    public void onActivityResult(int reqCode, int resCode, Intent data){
        taskPresenter.onPasswordReceived(reqCode, resCode, data);
    }

    //Interface of View ============================================================================
    @Override
    public void displayError(ProcessException err) {
        errorManager.displayError(err);
    }

    @Override
    public void navigateActivity(Class<?> cls) {
        Intent nextAct  = new Intent(this, cls);
        startActivity(nextAct);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void securityPrompt(boolean cancellable) {
        Intent pwPrompt = new Intent(this, PopUpLogin.class);
        pwPrompt.putExtra("Cancellable", cancellable);
        startActivityForResult(pwPrompt, PopUpLogin.PASSWORD_REQ_CODE);
    }

    @Override
    public void beep() {}

    @Override
    public void pauseScanning() {
        barcodeView.pause();
    }

    @Override
    public void resumeScanning() {
        barcodeView.resume();
    }

    @Override
    public void openProgressWindow(String title, String message) {
        progDialog  = ProgressDialog.show(this, title, message);
    }

    @Override
    public void closeProgressWindow() {
        if(progDialog != null)
            progDialog.dismiss();
    }
}