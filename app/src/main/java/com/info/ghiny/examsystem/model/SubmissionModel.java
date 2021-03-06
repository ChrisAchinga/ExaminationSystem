package com.info.ghiny.examsystem.model;

import com.info.ghiny.examsystem.database.AttendanceList;
import com.info.ghiny.examsystem.database.Candidate;
import com.info.ghiny.examsystem.database.ExternalDbLoader;
import com.info.ghiny.examsystem.database.StaffIdentity;
import com.info.ghiny.examsystem.database.Status;
import com.info.ghiny.examsystem.interfacer.LoginMVP;
import com.info.ghiny.examsystem.interfacer.SubmissionMVP;
import com.info.ghiny.examsystem.manager.IconManager;
import com.info.ghiny.examsystem.manager.SortManager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

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

public class SubmissionModel implements SubmissionMVP.MvpModel {
    private HashMap<String, Integer> unassignedMap;
    private SubmissionMVP.MvpMPresenter taskPresenter;
    private boolean sent;
    private AttendanceList attendanceList;
    private SortManager sortManager;
    private List<String> regNumList;
    private StaffIdentity user;

    void setSent(boolean sent) {
        this.sent = sent;
    }
    void setUnassignedMap(HashMap<String, Integer> unassignedMap) {
        this.unassignedMap = unassignedMap;
    }
    boolean isSent() {
        return sent;
    }
    HashMap<String, Integer> getUnassignedMap() {
        return unassignedMap;
    }

    public SubmissionModel(SubmissionMVP.MvpMPresenter taskPresenter){
        this.taskPresenter  = taskPresenter;
        this.sent           = false;
        this.unassignedMap  = new HashMap<>();
        this.attendanceList = TakeAttdModel.getAttdList();
        this.sortManager    = new SortManager();
        this.user           = LoginModel.getStaff();
    }

    @Override
    public void uploadAttdList() throws ProcessException {
        this.sent   = false;
        ExternalDbLoader.updateAttendanceList(attendanceList);
    }

    @Override
    public void verifyChiefResponse(String messageRx) throws ProcessException {
        sent    = true;
        boolean uploaded = JsonHelper.parseBoolean(messageRx);
        if(uploaded){
            throw new ProcessException("Submission successful",
                    ProcessException.MESSAGE_DIALOG, IconManager.ASSIGNED);
        } else {
            throw new ProcessException("Submission denied by Chief",
                    ProcessException.MESSAGE_DIALOG, IconManager.WARNING);
        }
    }

    @Override
    public void matchPassword(String password) throws ProcessException {
        if(!user.matchPassword(password))
            throw new ProcessException("Access denied. Incorrect Password",
                    ProcessException.MESSAGE_TOAST, IconManager.MESSAGE);
    }

    @Override
    public ArrayList<Candidate> getCandidatesWith(Status                    status,
                                                  SortManager.SortMethod    sortMethod,
                                                  boolean                   ascendingOrder)
                                                    throws ProcessException {
        if(attendanceList == null){
            throw new ProcessException("Attendance List is not initialize yet",
                    ProcessException.FATAL_MESSAGE, IconManager.WARNING);
        } else if(regNumList == null){
            regNumList     = attendanceList.getAllCandidateRegNumList();
        }
        TreeSet<Candidate> treeSet  = new TreeSet<>(sortManager.getComparator(sortMethod));
        for(int i = 0; i < regNumList.size(); i++) {
            Candidate tempCdd = attendanceList.getCandidate(regNumList.get(i));
            if(tempCdd.getStatus() == status){
                treeSet.add(tempCdd);
            }
        }

        if(!ascendingOrder)
            return new ArrayList<>(treeSet.descendingSet());
        return new ArrayList<>(treeSet);
    }

    @Override
    public void unassignCandidate(int lastPosition, Candidate candidate) throws ProcessException {
        if(candidate == null || candidate.getStatus() != Status.PRESENT){
            throw new ProcessException("Candidate Info Corrupted",
                    ProcessException.FATAL_MESSAGE, IconManager.WARNING);
        }
        unassignedMap.put(candidate.getRegNum(), candidate.getTableNumber());
        attendanceList.removeCandidate(candidate.getRegNum());
        candidate.setStatus(Status.ABSENT);
        candidate.setTableNumber(0);
        candidate.setCollector(null);
        attendanceList.addCandidate(candidate);
        TakeAttdModel.updateAbsentForUpdatingList(candidate);
    }

    @Override
    public void assignCandidate(Candidate candidate) throws ProcessException {
        if(candidate == null || candidate.getStatus() != Status.ABSENT){
            throw new ProcessException("Candidate Info Corrupted",
                    ProcessException.FATAL_MESSAGE, IconManager.WARNING);
        }

        Integer table   = unassignedMap.remove(candidate.getRegNum());
        if(table == null){
            throw new ProcessException("Candidate is never assign before",
                    ProcessException.MESSAGE_TOAST, IconManager.WARNING);
        }

        attendanceList.removeCandidate(candidate.getRegNum());
        candidate.setCollector(user.getIdNo());
        candidate.setTableNumber(table);
        candidate.setStatus(Status.PRESENT);
        attendanceList.addCandidate(candidate);
        TakeAttdModel.updatePresentForUpdatingList(candidate);
    }

    @Override
    public void run() {
        try{
            if(!sent){
                ProcessException err = new ProcessException(
                        "Server busy. Upload times out.\nPlease try again later.",
                        ProcessException.MESSAGE_DIALOG, IconManager.MESSAGE);
                err.setListener(ProcessException.okayButton, taskPresenter);
                err.setBackPressListener(taskPresenter);
                throw err;
            }
        } catch (ProcessException err) {
            taskPresenter.onTimesOut(err);
        }
    }
}
