package com.info.ghiny.examsystem.manager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.info.ghiny.examsystem.PopUpLogin;
import com.info.ghiny.examsystem.R;
import com.info.ghiny.examsystem.database.CheckListLoader;
import com.info.ghiny.examsystem.database.ExternalDbLoader;
import com.info.ghiny.examsystem.interfacer.AttendanceListPresenter;
import com.info.ghiny.examsystem.interfacer.GeneralView;
import com.info.ghiny.examsystem.model.ChiefLink;
import com.info.ghiny.examsystem.model.FragmentHelper;
import com.info.ghiny.examsystem.model.IconManager;
import com.info.ghiny.examsystem.model.JsonHelper;
import com.info.ghiny.examsystem.model.LoginHelper;
import com.info.ghiny.examsystem.model.ProcessException;
import com.info.ghiny.examsystem.model.TCPClient;

import org.w3c.dom.Text;

/**
 * Created by GhinY on 08/08/2016.
 */
public class FragListManager implements AttendanceListPresenter {
    private Handler handler;
    private GeneralView generalView;
    private FragmentHelper fragmentModel;

    public FragListManager(GeneralView generalView){
        this.generalView    = generalView;
        this.fragmentModel  = new FragmentHelper();
        this.handler        = new Handler();
    }

    public void setFragmentModel(FragmentHelper fragmentModel) {
        this.fragmentModel = fragmentModel;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void signToUpload(){
        generalView.navigateActivity(PopUpLogin.class);
    }

    public void toggleUnassign(View view){
        float clear     = 1;
        float chalky    = 0.1f;

        ViewGroup parent = (ViewGroup) view.getParent();

        TextView table  = (TextView) parent.findViewById(R.id.assignedTableText);
        TextView cdd    = (TextView) parent.findViewById(R.id.assignedCddText);
        TextView prg    = (TextView) parent.findViewById(R.id.assignedPrgText);
        CheckBox bt     = (CheckBox) parent.findViewById(R.id.uncheckPresent);
        TextView status = (TextView) parent.findViewById(R.id.checkboxStatus);

        if(bt.isChecked()){
            table.setAlpha(clear);
            cdd.setAlpha(clear);
            prg.setAlpha(clear);
            status.setText(R.string.checked);
            //remove from list by MODEL
        } else {
            table.setAlpha(chalky);
            cdd.setAlpha(chalky);
            prg.setAlpha(chalky);
            status.setText(R.string.unchecked);
            //add to delete list by MODEL
        }
    }

    @Override
    public void onPasswordReceived(int requestCode, int resultCode, Intent data){
        if(requestCode == PopUpLogin.PASSWORD_REQ_CODE && resultCode == Activity.RESULT_OK){
            String password = data.getStringExtra("Password");
            try{
                if(!LoginHelper.getStaff().matchPassword(password))
                    throw new ProcessException("Submission denied. Incorrect Password",
                            ProcessException.MESSAGE_TOAST, IconManager.MESSAGE);

                fragmentModel.uploadAttdList();

                handler.postDelayed(timer, 5000);
            } catch(ProcessException err){
                generalView.displayError(err);
            }
        }
    }

    @Override
    public void onResume(final ErrorManager errorManager){
        ExternalDbLoader.getTcpClient().setMessageListener(new TCPClient.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                try{
                    ChiefLink.setCompleteFlag(true);
                    boolean uploaded = JsonHelper.parseBoolean(message);
                } catch (ProcessException err){
                    ExternalDbLoader.getChiefLink().publishError(errorManager, err);
                }
            }
        });
    }

    @Override
    public void onDestroy(){
        handler.removeCallbacks(timer);
    }

    private Runnable timer = new Runnable() {
        @Override
        public void run() {
            if(!ChiefLink.isComplete()){
                ProcessException err = new ProcessException(
                        "Server busy. Upload times out.\nPlease try again later.",
                        ProcessException.MESSAGE_DIALOG, IconManager.MESSAGE);
                err.setListener(ProcessException.okayButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                if(generalView != null){
                    generalView.displayError(err);
                }
            }
        }
    };
}
