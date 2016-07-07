package com.info.ghiny.examsystem.tools;

import com.info.ghiny.examsystem.database.ExamDatabaseLoader;
import com.info.ghiny.examsystem.database.ExamSubject;
import com.info.ghiny.examsystem.database.StaffIdentity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by GhinY on 01/07/2016.
 */
public class ObtainInfoHelperTest {
    ExamDatabaseLoader exDBLoader;
    List<ExamSubject> papers;
    ExamSubject subject1;
    ExamSubject subject2;
    ExamSubject subject3;
    ExamSubject subject4;

    @Before
    public void setUp() throws Exception {
        subject1 = new ExamSubject("BAME 0001", "SUBJECT 1", 25, Calendar.getInstance(),
                10, ExamSubject.ExamVenue.H2, ExamSubject.Session.AM);
        subject2 = new ExamSubject("BAME 0002", "SUBJECT 2", 55, Calendar.getInstance(),
                10, ExamSubject.ExamVenue.H2, ExamSubject.Session.AM);
        subject3 = new ExamSubject("BAME 0003", "SUBJECT 3", 10, Calendar.getInstance(),
                10, ExamSubject.ExamVenue.H2, ExamSubject.Session.AM);
        subject4 = new ExamSubject("BAME 0004", "SUBJECT 4", 70, Calendar.getInstance(),
                10, ExamSubject.ExamVenue.H2, ExamSubject.Session.AM);

        exDBLoader = Mockito.mock(ExamDatabaseLoader.class);
        ObtainInfoHelper.setExamDBLoader(exDBLoader);
    }
    //= GetCandidatePapers =========================================================================
    /**
     *  getCandidatePapers()
     *
     *  When the candidate was not found
     *  MESSAGE_TOAST will be thrown
     */
    @Test
    public void testGetCandidatePapers_Throw_Error_if_candidate_not_found() throws Exception {
        try{
            when(exDBLoader.getIdentity(anyString())).thenReturn(null);
            papers = ObtainInfoHelper.getCandidatePapers("951108106303");
            fail("Expected MESSAGE_TOAST but nothing was thrown");
        } catch (ProcessException err){
            assertEquals("Not an StaffIdentity", err.getErrorMsg());
            assertEquals(ProcessException.MESSAGE_TOAST, err.getErrorType());
        }
    }

    /**
     *  getCandidatePapers()
     *
     *  When the candidate does not have any paper
     *  should return empty List<> but not null
     */
    @Test
    public void testGetCandidatePapers_CandidateWithoutPapers() throws Exception {
        try{
            StaffIdentity id = new StaffIdentity("15WAU00001", "0123", false, "Seong");
            when(exDBLoader.getIdentity(anyString())).thenReturn(id);
            when(exDBLoader.getPapersExamine(id.getRegNum())).thenReturn(new ArrayList<String>());

            papers = ObtainInfoHelper.getCandidatePapers("900000102222");

            assertNotNull(papers);
            assertEquals(0, papers.size());
        } catch (ProcessException err){
            fail("No Exception expected but " +  err.getErrorMsg() + " was thrown");
        }
    }

    /**
     *  getCandidatePapers()
     *
     *  When the candidate have 3 paper codes
     *  should return a List<ExamSubject>
     */
    @Test
    public void testGetCandidatePapers_CandidateWithPapers() throws Exception {
        try{
            StaffIdentity id = new StaffIdentity("15WAU00001", "0123", false, "Seong");

            List<String> paperCodeList = new ArrayList<>();
            paperCodeList.add(subject1.getPaperCode());
            paperCodeList.add(subject2.getPaperCode());
            paperCodeList.add(subject3.getPaperCode());

            when(exDBLoader.getIdentity(anyString())).thenReturn(id);
            when(exDBLoader.getPapersExamine(id.getRegNum())).thenReturn(paperCodeList);
            when(exDBLoader.getPaperInfo(anyString())).thenReturn(subject1)
                    .thenReturn(subject2).thenReturn(subject3);

            papers = ObtainInfoHelper.getCandidatePapers("900000102222");

            assertNotNull(papers);
            assertEquals(3, papers.size());
            assertEquals(subject1, papers.get(0));
            assertEquals(subject2, papers.get(1));
            assertEquals(subject3, papers.get(2));
        } catch (ProcessException err){
            fail("No Exception expected but " +  err.getErrorMsg() + " was thrown");
        }
    }

    //= GetDaysLeft ================================================================================
    /**
     * getDaysLeft()
     *
     * return -1 when the Date of the paper to be examine is already past
     *
     * 1st of July --> today
     */
    @Test
    public void testGetDaysLeft_PastExam() throws Exception {
        Calendar paperDate = Calendar.getInstance();
        paperDate.set(2016, 6, 1);

        Integer dayLeft = ObtainInfoHelper.getDaysLeft(paperDate);

        assertEquals(-1, dayLeft.intValue());
    }

    /**
     * getDaysLeft()
     *
     * return -1 when the Date of the paper to be examine is already past
     * even if the DAY_OF_MONTH is larger for the paperDate
     *
     * Note: 5 = June, not May
     * As it start from 0, January is 0
     */
    @Test
    public void testGetDaysLeft_PastExam_With_Different_in_MONTH() throws Exception {
        Calendar paperDate = Calendar.getInstance();
        paperDate.set(2016, 5, 29);

        Integer dayLeft = ObtainInfoHelper.getDaysLeft(paperDate);

        assertEquals(-1, dayLeft.intValue());
    }

    /**
     * getDaysLeft()
     *
     * return 0 when the Date of the paper to be examine is the same day
     */
    @Test
    public void testGetDaysLeft_PresentExam() throws Exception {
        Calendar paperDate = Calendar.getInstance();

        Integer dayLeft = ObtainInfoHelper.getDaysLeft(paperDate);

        assertEquals(0, dayLeft.intValue());
    }

    /**
     * getDaysLeft()
     *
     * return number of day left when the Date of the paper to be examine is not yet
     */
    @Test
    public void testGetDaysLeft_FutureExam() throws Exception {
        Calendar paperDate = Calendar.getInstance();
        paperDate.add(Calendar.DAY_OF_MONTH, 4);

        Integer dayLeft = ObtainInfoHelper.getDaysLeft(paperDate);

        assertEquals(4, dayLeft.intValue());
    }

    /**
     * getDaysLeft()
     *
     * return number of day left when the Date of the paper to be examine was having year different
     * Although not realistic but it is a feature of the method to be correct
     */
    @Test
    public void testGetDaysLeft_FutureExamWithYearOfDifferent() throws Exception {
        Calendar paperDate = Calendar.getInstance();
        paperDate.add(Calendar.DAY_OF_MONTH, 800);

        Integer dayLeft = ObtainInfoHelper.getDaysLeft(paperDate);

        assertEquals(799, dayLeft.intValue());
    }
}