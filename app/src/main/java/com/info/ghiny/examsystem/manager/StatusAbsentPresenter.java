package com.info.ghiny.examsystem.manager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.info.ghiny.examsystem.database.CandidateDisplayHolder;
import com.info.ghiny.examsystem.database.Status;

import com.info.ghiny.examsystem.interfacer.StatusFragmentMVP;
import com.info.ghiny.examsystem.interfacer.SubmissionMVP;
import com.info.ghiny.examsystem.model.ProcessException;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by user09 on 11/25/2016.
 */

public class StatusAbsentPresenter implements StatusFragmentMVP.AbsentMvpVPresenter {

    private Paint p;
    private Bitmap retakeIcon;
    private Snackbar snackbar;
    private ArrayList<Candidate> absentList;

    private Candidate tempCandidate;
    private int tempPosition;
    private StatusFragmentMVP.AbsentMvpView taskView;
    private SubmissionMVP.MvpModel taskModel;

    public StatusAbsentPresenter(Bitmap retakeIcon, StatusFragmentMVP.AbsentMvpView taskView){
        this.p          = new Paint();
        this.retakeIcon = retakeIcon;
        this.taskView   = taskView;
    }

    public void setTaskModel(SubmissionMVP.MvpModel taskModel) {
        this.taskModel  = taskModel;
        this.absentList = taskModel.getCandidatesWith(Status.ABSENT);
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
        return new CandidateDisplayHolder(context, v);
    }

    @Override
    public void onBindViewHolder(CandidateDisplayHolder holder, int position) {
        Candidate cdd   = absentList.get(position);

        holder.setCddName(cdd.getExamIndex());
        holder.setCddRegNum(cdd.getRegNum());
        holder.setCddPaperCode(cdd.getPaperCode());
        holder.setCddProgramme(cdd.getProgramme());
        holder.setCddTable(cdd.getTableNumber());
    }

    @Override
    public int getItemCount() {
        return absentList.size();
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
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
                tempCandidate   = absentList.remove(position);
                taskView.removeCandidate(position, absentList.size());
                taskModel.assignCandidate(tempCandidate);
                String msg  = String.format(Locale.ENGLISH, "%s is now PRESENT",
                        tempCandidate.getRegNum());
                tempPosition    = position;

                snackbar     = Snackbar.make(refView, msg, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("UNDO", this);
                snackbar.show();
            }
        } catch (ProcessException err) {
            if(err.getErrorType() == ProcessException.MESSAGE_TOAST){
                absentList.add(position, tempCandidate);
                taskView.insertCandidate(position);
            }
            taskView.displayError(err);
        }
    }

    @Override
    public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

            View itemView = viewHolder.itemView;
            float height = (float) itemView.getBottom() - (float) itemView.getTop();
            float width = height / 3;

            if(dX < 0){
                p.setColor(Color.parseColor("#00b500"));
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
                if(retakeIcon != null){
                    canvas.drawBitmap(retakeIcon, null, icon_dest, p);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        try{
            taskModel.unassignCandidate(tempPosition, tempCandidate);
            absentList.add(tempPosition, tempCandidate);
            taskView.insertCandidate(tempPosition);
        } catch (ProcessException err) {
            taskView.displayError(err);
        }
    }
}