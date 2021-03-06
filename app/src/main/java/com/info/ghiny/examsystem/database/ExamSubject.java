package com.info.ghiny.examsystem.database;

import com.info.ghiny.examsystem.model.ProcessException;
import com.info.ghiny.examsystem.manager.IconManager;

import java.util.Calendar;

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

public class ExamSubject {
    public static final String PAPER_DB_ID      = "_id";
    public static final String PAPER_CODE       = "PaperCode";
    public static final String PAPER_DESC       = "PaperDesc";
    public static final String PAPER_START_NO   = "PaperStartNo";
    public static final String PAPER_TOTAL_CDD  = "PaperTotalCdd";
    public static final String PAPER_SESSION    = "PaperSession";
    public static final String PAPER_VENUE      = "PaperVenue";
    public static final String PAPER_DATE       = "PaperDate";

    private String paperCode;
    private String paperDesc;
    private Calendar date;
    private Integer startTableNum;
    private Integer numOfCandidate;
    private Session paperSession;
    private String examVenue;

    public ExamSubject(){
        date            = Calendar.getInstance();
        paperSession    = Session.AM;
        examVenue       = null;
        startTableNum   = 0;
        numOfCandidate  = 0;
        paperCode       = null;
        paperDesc       = null;
    }

    public ExamSubject(String paperCode, String paperDesc, int startTableNum, Calendar date,
                       int numOfCandidate, String examVenue, Session paperSession){
        this.date           = date;
        this.paperSession   = paperSession;
        this.examVenue      = examVenue;
        this.startTableNum  = startTableNum;
        this.numOfCandidate = numOfCandidate;
        this.paperCode      = paperCode;
        this.paperDesc      = paperDesc;
    }

    public Integer getNumOfCandidate() {
        return numOfCandidate;
    }
    public Integer getStartTableNum() {
        return startTableNum;
    }

    public void setNumOfCandidate(Integer numOfCandidate) {
        this.numOfCandidate = numOfCandidate;
    }
    public void setStartTableNum(Integer startTableNum) {
        this.startTableNum = startTableNum;
    }

    public void setDate(Calendar date){
        this.date = date;
    }
    public Calendar getDate(){
        return date;
    }

    public void setPaperSession(Session session){
        paperSession = session;
    }

    public String getPaperSession(){
        return paperSession.toString();
    }

    public void setExamVenue(String venue){
        this.examVenue = venue;
    }

    public String getExamVenue() {
        return examVenue.toString();
    }

    public void setPaperCode(String paperCode){
        this.paperCode = paperCode;
    }

    public String getPaperCode(){
        return paperCode;
    }

    public void setPaperDesc(String paperDesc) {
        this.paperDesc = paperDesc;
    }

    public String getPaperDesc() {
        return paperDesc;
    }

    public static Calendar parseStringToDate(String date){
        Calendar calendar = null;
        String[] strArr = date.split("/");
        int year, month, day;

        try{
            if(strArr.length == 3){
                day     = Integer.parseInt(strArr[0]);
                month   = Integer.parseInt(strArr[1]) - 1;
                year    = Integer.parseInt(strArr[2]);
                calendar    = Calendar.getInstance();
                calendar.set(year, month, day);
            }
        } catch (Exception err) {
            calendar    = null;
        }

        return calendar;
    }

    public boolean isValidTable(Integer tableNumber) throws ProcessException {
        boolean valid   = false;
        int startNumber = startTableNum - 1;
        int endNumber   = startTableNum + numOfCandidate;

        if(tableNumber == null)
            throw new ProcessException("Input tableNumber is null", ProcessException.MESSAGE_DIALOG,
                    IconManager.WARNING);

        if(tableNumber < endNumber && tableNumber > startNumber)
            valid = true;

        return valid;
    }

    @Override
    public String toString() throws NullPointerException{
        String str;
        if(paperCode != null && paperDesc != null)
            str = paperCode + "  " + paperDesc;
        else if(paperCode == null)
            throw new NullPointerException("Paper Code was not filled yet");
        else
            throw new NullPointerException("Paper Description was not filled yet");

        return str;
    }
}
