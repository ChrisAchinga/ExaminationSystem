package com.info.ghiny.examsystem.fragments;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.info.ghiny.examsystem.R;
import com.info.ghiny.examsystem.view_holder.CandidateDisplayHolder;
import com.info.ghiny.examsystem.interfacer.StatusFragmentMVP;
import com.info.ghiny.examsystem.interfacer.SubmissionMVP;
import com.info.ghiny.examsystem.manager.ErrorManager;
import com.info.ghiny.examsystem.manager.StatusAbsentPresenter;
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

public class FragmentAbsent extends RootFragment implements StatusFragmentMVP.AbsentMvpView {
    private SubmissionMVP.MvpModel taskModel;

    private StatusFragmentMVP.AbsentMvpVPresenter taskPresenter;

    private RecyclerView recyclerView;
    private View uploadButton;
    private AbsentListAdapter adapter;
    private ErrorManager errorManager;
    private Bitmap retakeIcon;

    public FragmentAbsent(){}

    @Override
    public void setTaskModel(SubmissionMVP.MvpModel taskModel) {
        this.taskModel = taskModel;
    }

    @Override
    public void setErrorManager(ErrorManager errorManager) {
        this.errorManager   = errorManager;
    }

    @Override
    public void refresh() {
        taskPresenter.onRefresh();
        adapter.notifyDataSetChanged();
    }

    //==============================================================================================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retakeIcon  = BitmapFactory.decodeResource(getResources(), R.drawable.other_entry_icon);
        initMVP();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater         inflater,
                             @Nullable ViewGroup    container,
                             @Nullable Bundle       savedInstanceState) {
        View view       =  inflater.inflate(R.layout.fragment_status_absent, null);
        recyclerView    = (RecyclerView) view.findViewById(R.id.recyclerAbsentList);
        uploadButton    = getActivity().findViewById(R.id.uploadButton);
        adapter         = new AbsentListAdapter();
        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return taskPresenter.onMove(recyclerView, viewHolder, target);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                taskPresenter.onSwiped(uploadButton, viewHolder, direction);
            }

            @Override
            public void onChildDraw(Canvas canvas, RecyclerView rcView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isActive) {
                taskPresenter.onChildDraw(canvas, rcView, viewHolder, dX, dY, actionState, isActive);
                super.onChildDraw(canvas, rcView, viewHolder, dX, dY, actionState, isActive);
            }
        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return view;
    }

    private void initMVP(){
        SharedPreferences preferences   = PreferenceManager.getDefaultSharedPreferences(getContext());
        StatusAbsentPresenter presenter = new StatusAbsentPresenter(retakeIcon, preferences, this);
        presenter.setTaskModel(taskModel);
        this.taskPresenter              = presenter;
    }

    @Override
    public void onResume() {
        taskPresenter.onResume();
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onPause() {
        taskPresenter.onPause();
        super.onPause();
    }

    //= Adapter Class ==============================================================================

    public class AbsentListAdapter extends RecyclerView.Adapter<CandidateDisplayHolder> {

        @Override
        public CandidateDisplayHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return taskPresenter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(CandidateDisplayHolder holder, int position) {
            taskPresenter.onBindViewHolder(holder, position);
        }


        @Override
        public int getItemCount() {
            return taskPresenter.getItemCount();
        }

    }

    //= MVP MvpView Interface Implementation ==========================================================

    @Override
    public void insertCandidate(int position) {
        adapter.notifyItemInserted(position);
    }

    @Override
    public void removeCandidate(int position, int newSize) {
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, newSize);
    }

    @Override
    public void displayError(ProcessException err){
        errorManager.displayError(err);
    }
}
