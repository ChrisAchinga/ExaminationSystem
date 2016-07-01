package com.info.ghiny.examsystem.database;

import android.support.test.runner.AndroidJUnit4;
import static android.support.test.InstrumentationRegistry.getTargetContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by GhinY on 27/06/2016.
 */
@RunWith(AndroidJUnit4.class)
public class JdbcDatabaseTest {
    JdbcDatabase db;
    String SAVE_ATTENDANCE = "INSERT INTO AttdTable " +
                                "(Name, RegNum, TableNo, Status, Code, Programme) VALUES (";

    @Before
    public void setUp() throws Exception {
        //getTargetContext().deleteDatabase(JdbcDatabase.ADDRESS);
        getTargetContext().deleteDatabase(JdbcDatabase.DB_NAME);
        db = new JdbcDatabase(JdbcDatabase.DRIVER, JdbcDatabase.ADDRESS);
    }

    //= EstaConnection() ===========================================================================
    /**
     *  estaConnection()
     *  establish a connection with the database
     *
     *  when there is no .db found, estaConnection create a database file
     *  in the url while constructing the JdbcDatabase object.
     *
     *  This test check if estaConnection() return a connection to a Read and Write DB
     */
    @Test
    public void testEstaConnection() throws Exception {
        try{
            Connection con = db.estaConnection();
            assertFalse(con.isReadOnly());
            assertFalse(con.isClosed());
            con.close();
            assertTrue(con.isClosed());
        } catch (Exception err){
            fail("No exception expected but " + err.getMessage() + " was thrown.");
        }
    }


    //= CreateTableIsNotExist ======================================================================
    /**
     *  createTableIfNotExist()
     *  check if the local table of attendance list have been created
     *  Create if does have and do nothing if the table exist.
     *
     */
    @Test
    public void testCreateTableIfNotExist() throws Exception {
        try{
            db.createTableIfNotExist();     //Create when the table doesn't exist
            Connection con = db.estaConnection();
            Statement stmt = con.createStatement();

            ResultSet test = stmt.executeQuery("SELECT * FROM AttdTable;");
            assertFalse(test.first());

            test.close();
            stmt.close();
            con.close();

            db.createTableIfNotExist();     //Ignore creation as table already exist
        } catch(Exception err){
            fail("No exception expected but " + err.getMessage() + " was thrown.");
        }
    }

    //= isEmpty() ==================================================================================
    /**
     * isEmpty()
     *
     * create an empty table and return a false
     * when the target table does not exist in the database
     */
    @Test
    public void testIsEmpty_whenTheTableExist() throws Exception {
        try{
            db.createTableIfNotExist();
            assertTrue(db.isEmpty());

            Connection con = db.estaConnection();
            Statement stmt = con.createStatement();
            stmt.executeUpdate(SAVE_ATTENDANCE
                    + "'Steven', '15WAU11111', 16, 'PRESENT', 'BAME 0001', 'RMB3')");

            stmt.close();
            con.close();
            assertFalse(db.isEmpty());
        } catch(Exception err){
            fail("No exception expected but " + err.getMessage() + " was thrown.");
        }
    }

    /**********************************************************************
     * isEmpty()
     * should return true when the database is empty without table
     * should return false when the database is not empty
     *
     * At first, the database was empty and isEmpty() return true
     * When a Candidate was saved, isEmpty() return false
     **********************************************************************/
    @Test
    public void testIsEmpty_whenTheTable_NOT_Exist() throws Exception {
        try{
            assertTrue(db.isEmpty());
            Connection con = db.estaConnection();
            Statement stmt = con.createStatement();
            stmt.executeUpdate(SAVE_ATTENDANCE
                    + "'Steven', '15WAU11111', 16, 'PRESENT', 'BAME 0001', 'RMB3')");

            stmt.close();
            con.close();
            assertFalse(db.isEmpty());
        } catch(Exception err){
            fail("No exception expected but " + err.getMessage() + " was thrown.");
        }
    }
    //= ClearDatabase() ============================================================================

    /************************************************************
     * clearDatabase()
     * should delete all the data in the database
     *
     * Initially, the database is not empty
     * after clearDatabase(), the database become empty
     ************************************************************/
    @Test
    public void testClearDatabase() throws Exception {
        try{
            db.createTableIfNotExist();
            Connection con = db.estaConnection();
            Statement stmt = con.createStatement();
            stmt.executeUpdate(SAVE_ATTENDANCE
                    + "'Steven', '15WAU11111', 16, 'PRESENT', 'BAME 0001', 'RMB3')");
            stmt.close();
            con.close();
            //End of initialization
            assertFalse(db.isEmpty());
            db.clearDatabase();
            assertTrue(db.isEmpty());


        } catch(Exception err){
            fail("No exception expected but " + err.getMessage() + " was thrown.");
        }
    }

    /****************************************************************************
     * clearDatabase()
     *
     * should not throw SQLiteException when the target table doesn't exist
     * In fact, it does nothing
     ****************************************************************************/
    @Test
    public void testClearDatabase_Do_nothing_if_table_does_not_exist() throws Exception {
        try{
            db.clearDatabase();
            assertTrue(true);
        } catch(Exception err){
            fail("No exception expected but " + err.getMessage() + " was thrown.");
        }
    }

    //= SaveAttendanceList() =======================================================================
    /****************************************************************
     * saveAttendanceList()
     * should save the 2 Candidate into the database
     ****************************************************************/
    @Test
    public void testSaveAttendanceList() throws Exception {
        try{
            Candidate cdd1 = new Candidate(1, "RMB3", "FGY", "15WAU00001", "BAME 0001",
                    AttendanceList.Status.ABSENT);
            Candidate cdd2 = new Candidate(1, "RMB3", "NYN", "15WAU00002", "BAME 0001",
                    AttendanceList.Status.ABSENT);
            AttendanceList attdList = new AttendanceList();
            attdList.addCandidate(cdd1, cdd1.getPaperCode(), cdd1.getStatus(), cdd1.getProgramme());
            attdList.addCandidate(cdd2, cdd2.getPaperCode(), cdd2.getStatus(), cdd2.getProgramme());


            db.createTableIfNotExist();
            db.saveAttendanceList(attdList);

            Connection con = db.estaConnection();
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM AttdTable;");

            assertTrue(rs.first());
            assertEquals("FGY",        rs.getString("Name"));
            assertEquals("15WAU00001", rs.getString("RegNum"));
            assertEquals(1,            rs.getInt("TableNo"));
            assertEquals("ABSENT",     rs.getString("Status"));
            assertEquals("BAME 0001",  rs.getString("Code"));
            assertEquals("RMB3",       rs.getString("Programme"));

            assertTrue(rs.next());
            assertEquals("NYN",        rs.getString("Name"));
            assertEquals("15WAU00002", rs.getString("RegNum"));
            assertEquals(1,            rs.getInt("TableNo"));
            assertEquals("ABSENT",     rs.getString("Status"));
            assertEquals("BAME 0001",  rs.getString("Code"));
            assertEquals("RMB3",       rs.getString("Programme"));

            rs.close();
            stmt.close();
            con.close();
        } catch(Exception err){
            fail("No exception expected but " + err.getMessage() + " was thrown.");
        }
    }

    /****************************************************************
     * saveAttendanceList()
     * should save the 2 Candidate into the database
     ****************************************************************/
    @Test
    public void testSaveAttendanceList_TableDoesNotExist() throws Exception {
        try{
            Candidate cdd1 = new Candidate(1, "RMB3", "FGY", "15WAU00001", "BAME 0001",
                    AttendanceList.Status.ABSENT);
            Candidate cdd2 = new Candidate(1, "RMB3", "NYN", "15WAU00002", "BAME 0001",
                    AttendanceList.Status.ABSENT);
            AttendanceList attdList = new AttendanceList();
            attdList.addCandidate(cdd1, cdd1.getPaperCode(), cdd1.getStatus(), cdd1.getProgramme());
            attdList.addCandidate(cdd2, cdd2.getPaperCode(), cdd2.getStatus(), cdd2.getProgramme());

            db.saveAttendanceList(attdList);

            Connection con = db.estaConnection();
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM AttdTable;");

            assertTrue(rs.first());
            assertEquals("FGY",        rs.getString("Name"));
            assertEquals("15WAU00001", rs.getString("RegNum"));
            assertEquals(1,            rs.getInt("TableNo"));
            assertEquals("ABSENT",     rs.getString("Status"));
            assertEquals("BAME 0001",  rs.getString("Code"));
            assertEquals("RMB3",       rs.getString("Programme"));

            assertTrue(rs.next());
            assertEquals("NYN",        rs.getString("Name"));
            assertEquals("15WAU00002", rs.getString("RegNum"));
            assertEquals(1,            rs.getInt("TableNo"));
            assertEquals("ABSENT",     rs.getString("Status"));
            assertEquals("BAME 0001",  rs.getString("Code"));
            assertEquals("RMB3",       rs.getString("Programme"));

            rs.close();
            stmt.close();
            con.close();

        } catch(Exception err){
            fail("No exception expected but " + err.getMessage() + " was thrown.");
        }
    }

    //= getLastSavedAttendanceList() ===============================================================

    /**
     *  getLastSavedAttendanceList()
     *
     *  if Table doesn't not exist in the database
     *  An empty table will be created and this method return an empty HashMap<>
     *
     */
    @Test
    public void testGetLastSavedAttendanceList_EmptyDatabase() throws Exception {
        try{
            HashMap<AttendanceList.Status, HashMap<String, HashMap<String, HashMap<String, Candidate>>>>
                    map1 = db.getLastSavedAttendanceList();

            assertNotNull(map1);
            assertEquals(4, map1.size());
            assertEquals(0, map1.get(AttendanceList.Status.PRESENT).size());
            assertEquals(0, map1.get(AttendanceList.Status.ABSENT).size());
            assertEquals(0, map1.get(AttendanceList.Status.BARRED).size());
            assertEquals(0, map1.get(AttendanceList.Status.EXEMPTED).size());

        } catch (Exception err){
            fail("No exception expected but " + err.getMessage() + " was thrown.");
        }
    }//Add try catch in the methods to throw another Error

    /*********************************************************************
     * getLastSavedAttd()
     *
     * should return an empty HashMap of AttendanceList
     * instead of null when the database is empty.
     **********************************************************************/
    @Test
    public void testGetLastSavedAttendanceList_EmptyTable() throws Exception {
        try{
            db.createTableIfNotExist();
            HashMap<AttendanceList.Status, HashMap<String, HashMap<String, HashMap<String, Candidate>>>>
                    map1 = db.getLastSavedAttendanceList();

            assertNotNull(map1);
            assertEquals(4, map1.size());
            assertEquals(0, map1.get(AttendanceList.Status.PRESENT).size());
            assertEquals(0, map1.get(AttendanceList.Status.ABSENT).size());
            assertEquals(0, map1.get(AttendanceList.Status.BARRED).size());
            assertEquals(0, map1.get(AttendanceList.Status.EXEMPTED).size());

        } catch (Exception err){
            fail("No exception expected but " + err.getMessage() + " was thrown.");
        }
    }

    /*********************************************************************
     * getLastSavedAttd()
     *
     * should return a HashMap of AttendanceList as in the database
     **********************************************************************/
    @Test
    public void testGetLastSavedAttendanceList() throws Exception {
        try{
            db.createTableIfNotExist();
            Candidate cdd1 = new Candidate(1, "RMB3", "FGY", "15WAU00001", "BAME 0001",
                    AttendanceList.Status.ABSENT);
            AttendanceList attdList = new AttendanceList();
            attdList.addCandidate(cdd1, cdd1.getPaperCode(), cdd1.getStatus(), cdd1.getProgramme());
            db.saveAttendanceList(attdList);

            HashMap<AttendanceList.Status, HashMap<String, HashMap<String, HashMap<String, Candidate>>>>
                    map1 = db.getLastSavedAttendanceList();

            assertNotNull(map1);
            assertEquals(4, map1.size());
            assertEquals(0, map1.get(AttendanceList.Status.PRESENT).size());
            assertEquals(1, map1.get(AttendanceList.Status.ABSENT).size());
            assertEquals(0, map1.get(AttendanceList.Status.BARRED).size());
            assertEquals(0, map1.get(AttendanceList.Status.EXEMPTED).size());
        } catch(Exception err){
            fail("No exception expected but " + err.getMessage() + " was thrown.");
        }
    }
}