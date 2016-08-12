package com.info.ghiny.examsystem.manager;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.info.ghiny.examsystem.model.CustomToast;
import com.info.ghiny.examsystem.model.ProcessException;

/**
 * Created by GhinY on 27/06/2016.
 */
public class ErrorManager {

    private Activity act;

    public ErrorManager(Activity act) {
        this.act = act;
    }

    public void displayError(ProcessException err){
        switch(err.getErrorType()){
            case ProcessException.UPDATE_PROMPT:
                showReassignDialog(err);
                break;
            case ProcessException.MESSAGE_DIALOG:
                showMessageDialog(err);
                break;
            case ProcessException.MESSAGE_TOAST:
                showToastMessage(err);
                break;
            case ProcessException.FATAL_MESSAGE:
                showFatalError(err);
                break;
            case ProcessException.YES_NO_MESSAGE:
                showYesNoDialog(err);
                break;
        }
    }

    public void showToastMessage(ProcessException err){
        CustomToast message = new CustomToast(act);
        message.showCustomMessage(err.getErrorMsg(), err.getErrorIcon());
    }

    //public void showToast(String msg){
    //    Toast.makeText(act, msg, Toast.LENGTH_LONG).show();
    //}

    public void showReassignDialog(final ProcessException err){
        AlertDialog.Builder dialog = new AlertDialog.Builder(act);
        dialog.setMessage(err.getMessage());
        dialog.setCancelable(true);
        dialog.setPositiveButton(ProcessException.updateButton,
                err.getListener(ProcessException.updateButton));
        dialog.setNegativeButton(ProcessException.cancelButton,
                err.getListener(ProcessException.cancelButton));
        AlertDialog alert = dialog.create();
        alert.show();
    }

    public void showMessageDialog(final ProcessException err){
        AlertDialog.Builder dialog = new AlertDialog.Builder(act);
        dialog.setMessage(err.getMessage());
        dialog.setCancelable(true);
        dialog.setNeutralButton(ProcessException.okayButton,
                err.getListener(ProcessException.okayButton));
        AlertDialog alert = dialog.create();
        alert.show();
    }

    public void showFatalError(ProcessException err){
        AlertDialog.Builder dialog = new AlertDialog.Builder(act);
        dialog.setMessage(err.getMessage());
        dialog.setCancelable(true);

        dialog.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = dialog.create();
        alert.show();
    }

    public void showYesNoDialog(ProcessException err){
        AlertDialog.Builder dialog = new AlertDialog.Builder(act);
        dialog.setMessage(err.getMessage());

        dialog.setCancelable(true);

        dialog.setPositiveButton(ProcessException.yesButton,
                err.getListener(ProcessException.yesButton));
        dialog.setNegativeButton(ProcessException.noButton,
                err.getListener(ProcessException.noButton));

        AlertDialog alert = dialog.create();
        alert.show();
    }
}