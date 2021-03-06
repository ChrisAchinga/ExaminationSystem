package com.info.ghiny.examsystem.interfacer;

import android.content.DialogInterface;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.info.ghiny.examsystem.database.Candidate;
import com.info.ghiny.examsystem.database.Status;
import com.info.ghiny.examsystem.manager.ErrorManager;
import com.info.ghiny.examsystem.manager.SortManager;
import com.info.ghiny.examsystem.model.ProcessException;

import java.util.ArrayList;

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

public interface SubmissionMVP {
    interface MvpView extends NavigationView.OnNavigationItemSelectedListener,
            GeneralView, TaskConnView{
        void onUpload(View view);

        void displayReportWindow(String inCharge, String venue, String[] statusNo, String total);
    }

    interface MvpVPresenter extends DialogInterface.OnClickListener,
            TaskSecurePresenter, TaskConnPresenter {
        void onUpload();
        boolean onNavigationItemSelected(Toolbar toolbar, int itemId, ErrorManager errManager,
                                         FragmentManager manager, DrawerLayout drawer);
        boolean onSetting();
    }

    interface MvpMPresenter extends DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
        void onTimesOut(ProcessException err);
    }

    interface MvpModel extends Runnable, TaskSecureModel {
        void verifyChiefResponse(String messageRx) throws ProcessException;
        void uploadAttdList() throws ProcessException;
        ArrayList<Candidate> getCandidatesWith(Status status, SortManager.SortMethod sortMethod,
                                               boolean ascendingOrder) throws ProcessException ;
        void unassignCandidate(int lastPosition, Candidate candidate) throws ProcessException;
        void assignCandidate(Candidate candidate) throws ProcessException;
    }

}
