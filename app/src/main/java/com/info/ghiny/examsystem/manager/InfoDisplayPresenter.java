package com.info.ghiny.examsystem.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.info.ghiny.examsystem.PopUpLogin;
import com.info.ghiny.examsystem.R;
import com.info.ghiny.examsystem.database.ExamSubject;
import com.info.ghiny.examsystem.database.ExternalDbLoader;
import com.info.ghiny.examsystem.interfacer.InfoDisplayMVP;
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

public class InfoDisplayPresenter implements InfoDisplayMVP.Presenter {

    private InfoDisplayMVP.ViewFace taskView;
    private InfoDisplayMVP.Model taskModel;
    private boolean secureFlag;
    private ConfigManager configManager;

    public InfoDisplayPresenter(InfoDisplayMVP.ViewFace taskView, ConfigManager configManager){
        this.taskView       = taskView;
        this.secureFlag     = false;
        this.configManager  = configManager;
    }

    public void setTaskModel(InfoDisplayMVP.Model taskModel) {
        this.taskModel = taskModel;
    }

    @Override
    public void onCreate(Intent intent){
        try{
            String message  = intent.getStringExtra(JsonHelper.MINOR_KEY_CANDIDATES);
            taskModel.updateSubjects(message);
            taskView.notifyDataSetChanged();
            ExternalDbLoader.getJavaHost().setTaskView(taskView);
        } catch (ProcessException err) {
            taskView.displayError(err);
        } catch (Exception err){
            taskView.displayError(new ProcessException(err.getMessage(),
                    ProcessException.FATAL_MESSAGE, IconManager.WARNING));
        }
    }

    @Override
    public void onRestart() {
        if(!secureFlag){
            secureFlag = true;
            taskView.securityPrompt(false);
        }
    }

    @Override
    public void onPasswordReceived(int requestCode, int resultCode, Intent data) {
        if(requestCode == PopUpLogin.PASSWORD_REQ_CODE && resultCode == Activity.RESULT_OK){
            secureFlag  = false;
            String password = data.getStringExtra("Password");
            try{
                taskModel.matchPassword(password);
            } catch(ProcessException err){
                taskView.displayError(err);
                secureFlag  = true;
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
            dayLeft = days.toString() + " days later";

        examPaper.setTypeface(configManager.getTypeface(ConfigManager.DEFAULT_FONT));
        examDay.setTypeface(configManager.getTypeface(ConfigManager.DEFAULT_FONT));
        examVenue.setTypeface(configManager.getTypeface(ConfigManager.BOLD_FONT));
        examSes.setTypeface(configManager.getTypeface(ConfigManager.DEFAULT_FONT));

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
