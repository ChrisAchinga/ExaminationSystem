package com.info.ghiny.examsystem.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.info.ghiny.examsystem.R;
import com.info.ghiny.examsystem.database.Candidate;
import com.info.ghiny.examsystem.view_holder.CandidateDisplayHolder;
import com.info.ghiny.examsystem.database.Status;
import com.info.ghiny.examsystem.interfacer.StatusFragmentMVP;
import com.info.ghiny.examsystem.interfacer.SubmissionMVP;
import com.info.ghiny.examsystem.model.ProcessException;

import java.util.ArrayList;
import java.util.Locale;

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

public class StatusPresentPresenter implements StatusFragmentMVP.PresentMvpPresenter {

    private Paint p;
    private Bitmap undoAssignIcon;
    private Snackbar snackbar;
    private ArrayList<Candidate> presentList;
    private SharedPreferences preferences;

    private boolean prefGroup;
    private boolean prefAscend;
    private int prefSort;

    private Candidate tempCandidate;
    private int tempPosition;
    private StatusFragmentMVP.PresentMvpView taskView;
    private SubmissionMVP.MvpModel taskModel;

    public StatusPresentPresenter(Bitmap undoAssignIcon, SharedPreferences preferences,
                                  StatusFragmentMVP.PresentMvpView taskView){
        this.p              = new Paint();
        this.taskView       = taskView;
        this.undoAssignIcon = undoAssignIcon;
        this.preferences    = preferences;
    }

    public void setTaskModel(SubmissionMVP.MvpModel taskModel) {
        this.taskModel      = taskModel;
    }

    private void initPresentList() throws ProcessException{
        if(prefGroup){
            switch (prefSort){
                case 1:
                    this.presentList = taskModel.getCandidatesWith(Status.PRESENT,
                            SortManager.SortMethod.GROUP_PAPER_GROUP_PROGRAM_SORT_ID, prefAscend);
                    break;
                case 2:
                    this.presentList = taskModel.getCandidatesWith(Status.PRESENT,
                            SortManager.SortMethod.GROUP_PAPER_GROUP_PROGRAM_SORT_NAME, prefAscend);
                    break;
                default:
                    this.presentList = taskModel.getCandidatesWith(Status.PRESENT,
                            SortManager.SortMethod.GROUP_PAPER_GROUP_PROGRAM_SORT_TABLE, prefAscend);
            }
        } else {
            switch (prefSort){
                case 1:
                    this.presentList = taskModel.getCandidatesWith(Status.PRESENT,
                            SortManager.SortMethod.GROUP_PAPER_SORT_ID, prefAscend);
                    break;
                case 2:
                    this.presentList = taskModel.getCandidatesWith(Status.PRESENT,
                            SortManager.SortMethod.GROUP_PAPER_SORT_NAME, prefAscend);
                    break;
                default:
                    this.presentList = taskModel.getCandidatesWith(Status.PRESENT,
                            SortManager.SortMethod.GROUP_PAPER_GROUP_PROGRAM_SORT_TABLE, prefAscend);
            }
        }
    }

    @Override
    public void onResume(){
        prefGroup   = preferences.getBoolean("ProgrammeGrouping", true);
        prefAscend  = preferences.getBoolean("AscendingSort", true);
        prefSort    = Integer.parseInt(preferences.getString("CandidatesSorting", "3"));
        try {
            initPresentList();
        } catch (ProcessException err) {
            taskView.displayError(err);
        }
    }

    @Override
    public void onPause() {
        if(snackbar != null)
            snackbar.dismiss();
    }

    @Override
    public CandidateDisplayHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.attendance_body, parent, false);
        v.setBackgroundResource(R.drawable.custom_attendance);
        return new CandidateDisplayHolder(context, v, this);
    }

    @Override
    public void onBindViewHolder(CandidateDisplayHolder holder, int position) {
        Candidate cdd   = presentList.get(position);

        holder.setCddName(cdd.getExamIndex());
        holder.setCddRegNum(cdd.getRegNum());
        holder.setCddPaperCode(cdd.getPaperCode());
        holder.setCddProgramme(cdd.getProgramme());
        holder.setCddTable(cdd.getTableNumber());
        if(cdd.isLate()){
            holder.setCddLateTag(true);
        } else {
            holder.setCddLateTag(false);
        }
    }

    @Override
    public int getItemCount() {
        return presentList.size();
    }

    @Override
    public void onLongPressed(int position, View view, boolean toggleToTrue) {
        presentList.get(position).setLate(toggleToTrue);
    }

    @Override
    public boolean onMove(RecyclerView            recyclerView,
                          RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(View refView, RecyclerView.ViewHolder viewHolder, int direction) {
        if(snackbar != null){
            snackbar.dismiss();
        }
        int position = viewHolder.getAdapterPosition();
        try{
            if (direction == ItemTouchHelper.LEFT){
                tempCandidate   = presentList.remove(position);
                taskView.removeCandidate(position, presentList.size());
                taskModel.unassignCandidate(position, tempCandidate);
                tempPosition    = position;
                String msg  = String.format(Locale.ENGLISH, "%s is now ABSENT",
                        tempCandidate.getRegNum());

                snackbar = Snackbar.make(refView, msg, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("UNDO", this);
                snackbar.show();
            }
        } catch (ProcessException err) {
            taskView.displayError(err);
        }
    }

    @Override
    public void onChildDraw(Canvas                  canvas,
                            RecyclerView            recyclerView,
                            RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState,
                            boolean                 isCurrentlyActive) {
        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

            View itemView = viewHolder.itemView;
            float height = (float) itemView.getBottom() - (float) itemView.getTop();
            float width = height / 3;

            if(dX < 0){
                p.setColor(Color.parseColor("#D32F2F"));
                RectF background = new RectF(
                        (float) itemView.getRight() + dX,
                        (float) itemView.getTop(),
                        (float) itemView.getRight(),
                        (float) itemView.getBottom());
                RectF icon_dest = new RectF(
                        (float) itemView.getRight()  - 2*width,
                        (float) itemView.getTop()    + width,
                        (float) itemView.getRight()  - width,
                        (float) itemView.getBottom() - width);
                canvas.drawRect(background,p);
                canvas.drawBitmap(undoAssignIcon, null, icon_dest, p);
            }
        }
    }

    @Override
    public void onClick(View v) {
        try{
            taskModel.assignCandidate(tempCandidate);
            presentList.add(tempPosition, tempCandidate);
            taskView.insertCandidate(tempPosition);
        } catch (ProcessException err) {
            taskView.displayError(err);
        }
    }

    public void onRefresh(){
        try{
            presentList.clear();
            initPresentList();
        } catch (ProcessException err) {
            taskView.displayError(err);
        }
    }

    void setTempCandidate(Candidate tempCandidate) {
        this.tempCandidate = tempCandidate;
    }
    void setTempPosition(int tempPosition) {
        this.tempPosition = tempPosition;
    }
    void setPresentList(ArrayList<Candidate> presentList) {
        this.presentList = presentList;
    }
    ArrayList<Candidate> getPresentList() {
        return presentList;
    }
}
