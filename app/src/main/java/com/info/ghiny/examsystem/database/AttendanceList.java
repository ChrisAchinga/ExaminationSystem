package com.info.ghiny.examsystem.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GhinY on 25/05/2016.
 */
public class AttendanceList {
    public enum Status {
        PRESENT,
        ABSENT,
        EXEMPTED,
        BARRED
    }
    private HashMap<Status, HashMap<String, HashMap<String, Candidate>>> attendanceList;

    public AttendanceList(){
        attendanceList = new HashMap<Status, HashMap<String, HashMap<String, Candidate>>>();

        HashMap<String, HashMap<String, Candidate>> present = new HashMap<String, HashMap<String, Candidate>>();
        HashMap<String, HashMap<String, Candidate>> absent  = new HashMap<String, HashMap<String, Candidate>>();
        HashMap<String, HashMap<String, Candidate>> barred  = new HashMap<String, HashMap<String, Candidate>>();
        HashMap<String, HashMap<String, Candidate>> exempt  = new HashMap<String, HashMap<String, Candidate>>();

        attendanceList.put(Status.PRESENT, present);
        attendanceList.put(Status.ABSENT, absent);
        attendanceList.put(Status.BARRED, barred);
        attendanceList.put(Status.EXEMPTED, exempt);
    }

    public AttendanceList(HashMap<String, HashMap<String, Candidate>> present,
                          HashMap<String, HashMap<String, Candidate>> absent,
                          HashMap<String, HashMap<String, Candidate>> barred,
                          HashMap<String, HashMap<String, Candidate>> exempt){
        attendanceList = new HashMap<Status, HashMap<String, HashMap<String, Candidate>>>();
        attendanceList.put(Status.PRESENT, present);
        attendanceList.put(Status.ABSENT, absent);
        attendanceList.put(Status.BARRED, barred);
        attendanceList.put(Status.EXEMPTED, exempt);
    }

    //Inherit Methods
    public HashMap<String, HashMap<String, Candidate>> getPaperList(Status status){
        assert status != null;
        return attendanceList.get(status);
    }

    public HashMap<String, Candidate> getCandidateList(Status status, String paperCode){
        assert status != null;
        assert paperCode != null;

        HashMap<String, Candidate> cddList = null;
        HashMap<String, HashMap<String, Candidate>> paperMap = getPaperList(status);
        if(!paperMap.isEmpty()){
            cddList = paperMap.get(paperCode);
        }

        return cddList;
    }


    //Available Methods
    public int getNumberOfStatus(){
        return attendanceList.size();
    }

    public int getNumberOfPaper(){
        return attendanceList.get(Status.PRESENT).size();
    }

    public int getNumberOfCandidates(){
        return getAllCandidateRegNumList().size();
    }

    public void addCandidate(Candidate cdd, String paperCode, Status status){
        assert cdd      != null : "Input Candidate argument cannot be null";
        assert paperCode!= null : "Input PaperCode argument cannot be null";
        assert status   != null : "Input Status argument cannot be null";

        HashMap<String, Candidate> cddList = getCandidateList(status, paperCode);
        if(cddList == null){
            cddList = new HashMap<String, Candidate>();
            cddList.put(cdd.getRegNum(), cdd);
            HashMap<String, HashMap<String, Candidate>> paperList = new HashMap<>();
            paperList.put(paperCode, cddList);

            attendanceList.put(status, paperList);
        }
        else{
            cdd.setStatus(status);
            cddList.put(cdd.getRegNum(), cdd);
        }

    }

    public void removeCandidate(String regNum){
        assert regNum != null;
        for(Map.Entry<Status, HashMap<String, HashMap<String, Candidate>>> s: attendanceList.entrySet()){
            fromPaperRemoveCandidate(regNum, s.getValue());
        }
    }

    public Candidate getCandidate(String regNum){
        assert regNum != null;
        Candidate candidate = null;
        for(Map.Entry<Status, HashMap<String, HashMap<String, Candidate>>> s: attendanceList.entrySet()){
            candidate = fromPaperFindCandidate(regNum, s.getValue());
            if(candidate != null)
                break;
        }
        return candidate;
    }

    protected List<String> getAllCandidateRegNumList(){
        List<String> regNumList = new ArrayList<>();

        for(Map.Entry<Status, HashMap<String, HashMap<String, Candidate>>> s:attendanceList.entrySet())
            fromPaperGetRegNum(regNumList, s.getValue());

        return regNumList;
    }

    //Private internal function used to traverse the level of Map
    private void fromPaperGetRegNum(List<String> regNumList,
                                    HashMap<String, HashMap<String, Candidate>> paperMap){
        for(Map.Entry<String, HashMap<String, Candidate>> s:paperMap.entrySet())
            fromCandidatesGetRegNum(regNumList, s.getValue());
    }

    private void fromCandidatesGetRegNum(List<String> regNumList,
                                         HashMap<String, Candidate> cddMap){
        for(Map.Entry<String, Candidate> s:cddMap.entrySet())
            regNumList.add(s.getKey());
    }

    private void fromPaperRemoveCandidate(String regNum, HashMap<String,
            HashMap<String, Candidate>> paperMap){
        assert regNum != null;
        assert paperMap != null;

        for(Map.Entry<String, HashMap<String, Candidate>> s:paperMap.entrySet()){
            fromCandidatesRemoveCandidate(regNum, s.getValue());
        }
    }

    private void fromCandidatesRemoveCandidate(String regNum,
                                               HashMap<String, Candidate> map){
        map.remove(regNum);
    }

    private Candidate fromPaperFindCandidate(String regNum, HashMap<String,
            HashMap<String, Candidate>> paperMap){
        assert regNum != null;
        assert paperMap != null;

        Candidate candidate = null;
        for(Map.Entry<String, HashMap<String, Candidate>> s:paperMap.entrySet()){
            candidate = fromCandidatesFindCandidate(regNum, s.getValue());
            if(candidate != null)
                break;
        }
        return candidate;
    }

    private Candidate fromCandidatesFindCandidate(String regNum,
                                                  HashMap<String, Candidate> map){
        assert regNum != null;
        assert map != null;

        return map.get(regNum);
    }
}
