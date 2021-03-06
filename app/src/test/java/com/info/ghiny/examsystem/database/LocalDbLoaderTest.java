package com.info.ghiny.examsystem.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;

import static org.junit.Assert.*;

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

@RunWith(RobolectricTestRunner.class)
@Config(manifest= Config.NONE)
public class LocalDbLoaderTest {

    private static final String FILLED_DB_PATH  = "/database/FragListDb.db";
    private static final String EMPTY_DB_PATH   = "/database/EmptyListDb.db";
    private static final String EMPTY_USER_PATH     = "/database/EmptyUserDb.db";
    private static final String FILLED_USER_PATH    = "/database/FilledUserDb.db";

    private String dbPath;
    private LocalDbLoader dbLoader;

    private String SAVE_ATTENDANCE = "INSERT INTO AttdTable " +
            "(ExamIndex, RegNum, TableNo, Status, PaperCode, Programme, Late, AttdCollector) VALUES (";

    @Before
    public void setUp() throws Exception {
        Context context = Mockito.mock(Context.class);
        dbLoader    = new LocalDbLoader(context);
    }

    //= emptyAttdInDB() & emptyPapersInDB() ========================================================

    /**
     * emptyAttdInDB() & emptyPapersInDB()
     *
     * 1. First test verify the two method to detect an empty database
     * 2. Second test verify the two method to detect non-empty database
     *
     * @throws Exception
     */
    @Test
    public void testEmptyAttdInDBAndEmptyPapersInDB_withEmptyDB() throws Exception {
        String path = getClass().getResource(EMPTY_DB_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        assertTrue(dbLoader.emptyAttdInDB());
        assertTrue(dbLoader.emptyPapersInDB());
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        db.close();
    }

    @Test
    public void testEmptyAttdInDBAndEmptyPapersInDB_withFilledDB() throws Exception {
        String path = getClass().getResource(FILLED_DB_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        assertFalse(dbLoader.emptyAttdInDB());
        assertFalse(dbLoader.emptyPapersInDB());
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        db.close();
    }

    //= QueryAttendanceList() ======================================================================
    /**
     * queryAttendanceList()
     *
     * 1. When the database is empty, return an empty AttendanceList object
     * 2. When the database is filled, return AttendanceList object with value in .db file
     *
     * @throws Exception
     */
    @Test
    public void testQueryAttendanceList_WithEmptyDatabase() throws Exception {
        String path = getClass().getResource(EMPTY_DB_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);

        AttendanceList attdList = dbLoader.queryAttendanceList();

        assertEquals(0, attdList.getNumberOfCandidates(Status.ABSENT));
        assertEquals(0, attdList.getNumberOfCandidates(Status.PRESENT));
        assertEquals(0, attdList.getNumberOfCandidates(Status.BARRED));
        assertEquals(0, attdList.getNumberOfCandidates(Status.EXEMPTED));
        assertEquals(0, attdList.getNumberOfCandidates(Status.QUARANTINED));

        db.close();
    }

    @Test
    public void testQueryAttendanceList_withFilledDatabase() throws Exception {
        String path = getClass().getResource(FILLED_DB_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        AttendanceList attdList = dbLoader.queryAttendanceList();

        //= ABSENT ====
        assertEquals(1, attdList.getNumberOfCandidates(Status.ABSENT));
        assertEquals(Status.ABSENT, attdList.getCandidate("15WAU00001").getStatus());
        assertFalse(attdList.getCandidate("15WAU00001").isLate());
        assertNull(attdList.getCandidate("15WAU00001").getCollector());

        //= PRESENT ====
        assertEquals(2, attdList.getNumberOfCandidates(Status.PRESENT));
        assertEquals(Status.PRESENT, attdList.getCandidate("15WAU00002").getStatus());
        assertEquals("12345", attdList.getCandidate("15WAU00002").getCollector());
        assertTrue(attdList.getCandidate("15WAU00002").isLate());
        assertEquals(Status.PRESENT, attdList.getCandidate("15WAU00003").getStatus());
        assertFalse(attdList.getCandidate("15WAU00003").isLate());
        assertEquals("12345", attdList.getCandidate("15WAU00003").getCollector());

        //= BARRED ====
        assertEquals(1, attdList.getNumberOfCandidates(Status.BARRED));
        assertEquals(Status.BARRED, attdList.getCandidate("15WAU00004").getStatus());
        assertFalse(attdList.getCandidate("15WAU00004").isLate());

        //= EXEMPTED ====
        assertEquals(1, attdList.getNumberOfCandidates(Status.EXEMPTED));
        assertEquals(Status.EXEMPTED, attdList.getCandidate("15WAU00005").getStatus());
        assertFalse(attdList.getCandidate("15WAU00005").isLate());
        assertNull(attdList.getCandidate("15WAU00005").getCollector());

        //= QUARANTINED ====
        assertEquals(0, attdList.getNumberOfCandidates(Status.QUARANTINED));
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        db.close();
    }

    //= QueryPapers() ==============================================================================

    /**
     * queryPapers()
     *
     * 1. When the database is empty, return an empty HashMap<> of ExamSubject
     * 2. When the database is filled, return HashMap<> of ExamSubject with value in .db file
     *
     * @throws Exception
     */
    @Test
    public void testQueryPapers_withEmptyDatabase() throws Exception {
        String path = getClass().getResource(EMPTY_DB_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        HashMap<String, ExamSubject> paperMap   = dbLoader.queryPapers();

        assertEquals(0, paperMap.size());
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        db.close();
    }

    @Test
    public void testQueryPapers_withFilledDatabase() throws Exception {
        String path = getClass().getResource(FILLED_DB_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        HashMap<String, ExamSubject> paperMap   = dbLoader.queryPapers();

        assertEquals(2, paperMap.size());
        assertEquals("SUBJECT 1", paperMap.get("BAME 0001").getPaperDesc());
        assertEquals(Integer.valueOf(32), paperMap.get("BAME 0001").getStartTableNum());
        assertEquals(Integer.valueOf(10), paperMap.get("BAME 0001").getNumOfCandidate());

        assertEquals("SUBJECT 2", paperMap.get("BAME 0002").getPaperDesc());
        assertEquals(Integer.valueOf(20), paperMap.get("BAME 0002").getStartTableNum());
        assertEquals(Integer.valueOf(10), paperMap.get("BAME 0002").getNumOfCandidate());
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        db.close();
    }

    //= ClearDatabase() ============================================================================

    /**
     * clearDatabase()
     *
     * remove all the data in the database
     *
     * @throws Exception
     */
    @Test
    public void testClearDatabase() throws Exception {
        String path = getClass().getResource(EMPTY_DB_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        assertTrue(dbLoader.emptyAttdInDB());

        db.execSQL(SAVE_ATTENDANCE + "'Steven', '15WAU11111', 16, 'PRESENT', 'BAME 0001', 'RMB3', 0, '24680')");
        assertFalse(dbLoader.emptyAttdInDB());

        dbLoader.clearDatabase();
        assertTrue(dbLoader.emptyAttdInDB());

        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        db.close();
    }


    //= SaveAttendanceList() =======================================================================

    /**
     * saveAttendanceList()
     *
     * take an argument of AttendanceList and save it into database
     *
     * @throws Exception
     */
    @Test
    public void testSaveAttendanceList() throws Exception {
        String path = getClass().getResource(EMPTY_DB_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        assertEquals(0, dbLoader.queryAttendanceList().getTotalNumberOfCandidates());

        Candidate cdd1 = new Candidate(1, "RMB3", "FGY", "15WAU00001", "BAME 0001",
                Status.ABSENT);
        Candidate cdd2 = new Candidate(1, "RMB3", "NYN", "15WAU00002", "BAME 0001",
                Status.ABSENT);
        Candidate cdd3 = new Candidate(12, "RMB3", "Name 1", "15WAU00003", "BAME 0001",
                Status.PRESENT);
        cdd3.setCollector("24680");

        AttendanceList attdList = new AttendanceList();
        attdList.addCandidate(cdd1);
        attdList.addCandidate(cdd2);
        attdList.addCandidate(cdd3);

        dbLoader.saveAttendanceList(attdList);
        assertEquals(3, dbLoader.queryAttendanceList().getTotalNumberOfCandidates());

        AttendanceList testList = dbLoader.queryAttendanceList();


        assertEquals("24680", testList.getCandidate("15WAU00003").getCollector());
        assertNull(testList.getCandidate("15WAU00001").getCollector());
        assertNull(testList.getCandidate("15WAU00002").getCollector());
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

        dbLoader.clearDatabase();
        db.close();
    }

    //= SavePaperList() =======================================================================

    /**
     * savePaperList()
     *
     * take an argument of HashMap<String, ExamSubject></> and save it into database
     *
     * @throws Exception
     */
    @Test
    public void testSavePaperList() throws Exception {
        String path = getClass().getResource(EMPTY_DB_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        assertEquals(0, dbLoader.queryPapers().size());
        ExamSubject subject1 = new ExamSubject("BAME 0001", "SUBJECT 1", 25,
                Calendar.getInstance(), 10, "H2", Session.AM);
        ExamSubject subject2 = new ExamSubject("BAME 0002", "SUBJECT 2", 55,
                Calendar.getInstance(), 10, "H2", Session.AM);

        HashMap<String, ExamSubject> subjects = new HashMap<>();
        subjects.put(subject1.getPaperCode(), subject1);
        subjects.put(subject2.getPaperCode(), subject2);

        dbLoader.savePaperList(subjects);

        assertEquals(2, dbLoader.queryPapers().size());

        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        dbLoader.clearDatabase();
        db.close();
    }

    //= EXTENSION: USER DATABASE TEST ==+++++=======================================================
    //= emptyUserInDB() and emptyConnectorInDB =====================================================

    @Test
    public void testEmptyUserInDB_withEmptyConnectorDatabase() throws Exception{
        String path = getClass().getResource(EMPTY_USER_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);

        assertTrue(dbLoader.emptyConnectorInDB());
        assertTrue(dbLoader.emptyUserInDB());

        db.close();
    }

    @Test
    public void testEmptyUserInDB_withFilledUserDatabase() throws Exception{
        String path = getClass().getResource(FILLED_USER_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);

        assertFalse(dbLoader.emptyConnectorInDB());
        assertFalse(dbLoader.emptyUserInDB());

        db.close();
    }

    //= ClearUserDatabase() ========================================================================
    @Test
    public void testClearUserDatabase1_EntryOfConnector() throws Exception {
        String path = getClass().getResource(EMPTY_USER_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        assertTrue(dbLoader.emptyConnectorInDB());
        db.execSQL("INSERT INTO ConnectionTable (IP, Port, RegDate, Session) " +
                "VALUES ('192.168.0.90', 6666, '14:09:2016', 'AM')");
        assertFalse(dbLoader.emptyConnectorInDB());

        dbLoader.clearUserDatabase();

        assertTrue(dbLoader.emptyConnectorInDB());
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        db.close();
    }

    @Test
    public void testClearUserDatabase2_EntryOfStaff() throws Exception {
        String path = getClass().getResource(EMPTY_USER_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        assertTrue(dbLoader.emptyUserInDB());
        db.execSQL("INSERT INTO StaffTable (IdNo, HashPass, Name, Venue, Role) " +
                "VALUES ('158888', 'aXlespfeaasSD', 'Staff1', 'M1', 'In-Charge')");
        assertFalse(dbLoader.emptyUserInDB());

        dbLoader.clearUserDatabase();

        assertTrue(dbLoader.emptyUserInDB());
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        db.close();
    }

    //= QueryConnector() ===========================================================================
    @Test
    public void testQueryConnector_withEmptyDb() throws Exception{
        String path = getClass().getResource(EMPTY_USER_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);

        Connector connector = dbLoader.queryConnector();
        assertNull(connector);

        db.close();
    }

    @Test
    public void testQueryConnector_withFilledDb() throws Exception{
        String path = getClass().getResource(FILLED_USER_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);

        Connector connector = dbLoader.queryConnector();

        assertNotNull(connector);
        assertEquals("192.168.0.112", connector.getIpAddress());
        assertEquals(Integer.valueOf(56789), connector.getPortNumber());
        assertEquals(8, connector.getDate().get(Calendar.DAY_OF_MONTH));
        assertEquals(8, connector.getDate().get(Calendar.MONTH));   //Note: 8=September as 0=January
        assertEquals(2016, connector.getDate().get(Calendar.YEAR));
        assertEquals(Session.AM, connector.getSession());

        db.close();
    }

    //= SaveConnector() ============================================================================
    @Test
    public void testSaveConnector() throws Exception {
        String path = getClass().getResource(EMPTY_USER_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);

        assertTrue(dbLoader.emptyConnectorInDB());
        Connector connector = new Connector("127.0.0.1", 6666, null);
        dbLoader.saveConnector(connector);
        assertFalse(dbLoader.emptyConnectorInDB());

        assertNotNull(dbLoader.queryConnector());
        assertEquals("127.0.0.1", dbLoader.queryConnector().getIpAddress());
        assertEquals(Integer.valueOf(6666), dbLoader.queryConnector().getPortNumber());

        dbLoader.clearUserDatabase();
        db.close();
    }

    //= QueryUser() ================================================================================
    @Test
    public void testQueryUser_withEmptyDb() throws Exception{
        String path = getClass().getResource(EMPTY_USER_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);

        StaffIdentity id = dbLoader.queryUser();
        assertNull(id);

        db.close();
    }

    @Test
    public void testQueryUser_withFilledDb() throws Exception{
        String path = getClass().getResource(FILLED_USER_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);

        StaffIdentity id = dbLoader.queryUser();

        assertNotNull(id);
        assertEquals("158888", id.getIdNo());
        assertEquals("aXsjPaeQ9=O9s!lks+", id.getHashPass());
        assertEquals("Staff1", id.getName());
        assertEquals("M1", id.getExamVenue());
        assertEquals(Role.IN_CHARGE, id.getRole());

        db.close();
    }

    //= SaveUser() =================================================================================
    @Test
    public void testSaveUser() throws Exception {
        String path = getClass().getResource(EMPTY_USER_PATH).toURI().getPath();
        File dbFile = new File(path);
        assertTrue(dbFile.exists());
        dbPath = dbFile.getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        LocalDbLoader.setDatabase(db);

        assertTrue(dbLoader.emptyUserInDB());
        StaffIdentity id    = new StaffIdentity("156666", true, "Staff2", "M2");
        id.setHashPass("feiKs9/wfSnn0dE2");
        id.setRole(Role.IN_CHARGE);

        dbLoader.saveUser(id);

        assertFalse(dbLoader.emptyUserInDB());
        assertNotNull(dbLoader.queryUser());
        assertEquals("156666", dbLoader.queryUser().getIdNo());
        assertEquals("feiKs9/wfSnn0dE2", dbLoader.queryUser().getHashPass());

        dbLoader.clearUserDatabase();
        db.close();
    }

}