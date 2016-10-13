package com.info.ghiny.examsystem.manager;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.info.ghiny.examsystem.PopUpLogin;
import com.info.ghiny.examsystem.R;
import com.info.ghiny.examsystem.database.ExamSubject;
import com.info.ghiny.examsystem.interfacer.InfoDisplayMVP;
import com.info.ghiny.examsystem.model.IconManager;
import com.info.ghiny.examsystem.model.JsonHelper;
import com.info.ghiny.examsystem.model.LoginModel;
import com.info.ghiny.examsystem.model.ProcessException;

import java.util.List;

/**
 * Created by GhinY on 10/10/2016.
 */

public class InfoDisplayPresenter implements InfoDisplayMVP.Presenter {

    private InfoDisplayMVP.ViewFace taskView;
    private InfoDisplayMVP.Model taskModel;

    public InfoDisplayPresenter(InfoDisplayMVP.ViewFace taskView){
        this.taskView   = taskView;
    }

    public void setTaskModel(InfoDisplayMVP.Model taskModel) {
        this.taskModel = taskModel;
    }

    @Override
    public void onCreate(Intent intent){
        try{
            String message  = intent.getStringExtra(JsonHelper.LIST_LIST);
            taskModel.updateSubjects(message);
            taskView.notifyDataSetChanged();
        } catch (Exception err) {
            taskView.finishActivity();
        }
    }

    @Override
    public void onRestart() {
        taskView.securityPrompt(false);
    }

    @Override
    public void onPasswordReceived(int requestCode, int resultCode, Intent data) {
        if(requestCode == PopUpLogin.PASSWORD_REQ_CODE && resultCode == Activity.RESULT_OK){
            String password = data.getStringExtra("Password");
            try{
                if(!LoginModel.getStaff().matchPassword(password))
                    throw new ProcessException("Access denied. Incorrect Password",
                            ProcessException.MESSAGE_TOAST, IconManager.MESSAGE);
            } catch(ProcessException err){
                taskView.displayError(err);
                taskView.securityPrompt(false);
            }
        }
    }

    @Override
    public int getCount() {
        return taskModel.getNumberOfSubject();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.exam_subject, parent, false);
        }

        ExamSubject subject = taskModel.getSubjectAt(position);
        TextView examPaper  = (TextView)convertView.findViewById(R.id.paperCodeNameText);
        TextView examDay    = (TextView)convertView.findViewById(R.id.paperDayText);
        TextView examVenue  = (TextView)convertView.findViewById(R.id.paperVenueText);
        TextView examSes    = (TextView)convertView.findViewById(R.id.paperSessionText);

        Integer days = taskModel.getDaysLeft(subject.getDate());
        String dayLeft;

        if(days == -1)
            dayLeft = "ENDED";
        else if(days == 0)
            dayLeft = "TODAY";
        else if(days == 1)
            dayLeft = "TOMORROW";
        else
            dayLeft = days.toString() + " days left";

        examPaper.setText(subject.toString());
        examDay.setText(dayLeft);
        examVenue.setText(subject.getExamVenue());
        examSes.setText(subject.getPaperSession());

        return convertView;
    }

    @Override
    public ExamSubject getItem(int position) {
        return taskModel.getSubjectAt(position);
    }
}