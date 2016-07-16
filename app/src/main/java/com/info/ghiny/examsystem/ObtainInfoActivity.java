package com.info.ghiny.examsystem;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.zxing.ResultPoint;
import com.info.ghiny.examsystem.adapter.ExamSubjectAdapter;
import com.info.ghiny.examsystem.database.ExamSubject;
import com.info.ghiny.examsystem.database.ExternalDbLoader;
import com.info.ghiny.examsystem.tools.ChiefLink;
import com.info.ghiny.examsystem.tools.ErrorManager;
import com.info.ghiny.examsystem.tools.IconManager;
import com.info.ghiny.examsystem.tools.JsonHelper;
import com.info.ghiny.examsystem.tools.ObtainInfoHelper;
import com.info.ghiny.examsystem.tools.OnSwipeListener;
import com.info.ghiny.examsystem.tools.ProcessException;
import com.info.ghiny.examsystem.tools.TCPClient;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

/**
 * Created by GhinY on 07/05/2016.
 */
public class ObtainInfoActivity extends AppCompatActivity {
    private static final String TAG = ObtainInfoActivity.class.getSimpleName();

    private ExamSubjectAdapter listAdapter;
    private ErrorManager errManager;
    private TCPClient obtainInfo;

    private DialogInterface.OnClickListener timesOutListener =
            new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    barcodeView.resume();
                    dialog.cancel();
                }
            };

    private CompoundBarcodeView barcodeView;
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                barcodeView.setStatusText(result.getText());
                requestPapers(result.getText());
                //get The info of the student here
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

        errManager  = new ErrorManager(this);
        listAdapter = new ExamSubjectAdapter();

        obtainInfo = new TCPClient(new TCPClient.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                try{
                    List<ExamSubject> subjects = JsonHelper.parsePaperList(message);
                    listAdapter.updatePapers(subjects);
                } catch (ProcessException err) {
                    errManager.displayError(err);
                    barcodeView.resume();
                }
            }
        });
        ExternalDbLoader.setTcpClient(obtainInfo);

        ListView paperList = (ListView)findViewById(R.id.paperInfoList);
        assert paperList != null;

        paperList.setAdapter(listAdapter);

        RelativeLayout thisLayout = (RelativeLayout) findViewById(R.id.obtainInfoLayout);
        assert thisLayout != null;
        thisLayout.setOnTouchListener(new OnSwipeListener(this){
            @Override
            public void onSwipeTop() {
                finish();
            }
        });
        paperList.setOnTouchListener(new OnSwipeListener(this){
            @Override
            public void onSwipeTop() {
                finish();
            }
        });


        barcodeView = (CompoundBarcodeView) findViewById(R.id.obtainScanner);
        barcodeView.decodeContinuous(callback);
        barcodeView.setStatusText("Scan candidate ID to get his/her exam details");
    }

    @Override
    protected void onResume() {
        super.onResume();
        ExternalDbLoader.setTcpClient(obtainInfo);
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event)
                || super.onKeyDown(keyCode, event);
    }

    //==============================================================================================
    private void requestPapers(String scanStr){
        try{
            barcodeView.pause();
            ObtainInfoHelper.reqCandidatePapers(scanStr);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!ChiefLink.isComplete()){
                        ProcessException err = new ProcessException(
                                "Server busy. Request times out. \n Please try again later.",
                                ProcessException.MESSAGE_DIALOG, IconManager.MESSAGE);
                        err.setListener(ProcessException.okayButton, timesOutListener);
                        barcodeView.pause();
                        errManager.displayError(err);
                    }
                }
            }, 10000);
        } catch (ProcessException err){
            errManager.displayError(err);
            barcodeView.resume();
        }

    }
}
