package com.info.ghiny.examsystem.model;

import com.info.ghiny.examsystem.database.LocalDbLoader;
import com.info.ghiny.examsystem.database.Connector;
import com.info.ghiny.examsystem.database.ExternalDbLoader;
import com.info.ghiny.examsystem.database.Role;
import com.info.ghiny.examsystem.database.StaffIdentity;
import com.info.ghiny.examsystem.interfacer.LinkChiefMVP;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
public class LinkChiefModelTest {
    private LocalDbLoader dbLoader;
    private LinkChiefMVP.MPresenter taskPresenter;
    private LinkChiefModel model;
    private ConnectionTask task;
    private JavaHost javaHost;
    private StaffIdentity staff;

    @Before
    public void setUp() throws Exception {
        dbLoader        = Mockito.mock(LocalDbLoader.class);
        taskPresenter   = Mockito.mock(LinkChiefMVP.MPresenter.class);
        task            = Mockito.mock(ConnectionTask.class);
        staff           = new StaffIdentity("id", true, "name", "M4");
        LoginModel.setStaff(staff);

        model           = new LinkChiefModel(dbLoader, taskPresenter);
        model.setTask(task);

        JavaHost.setConnector(null);
        ExternalDbLoader.setConnectionTask(null);
    }
    
    //= TryConnectWithQR() =========================================================================
    /**
     *  tryConnectWithQR()
     *
     *  1.  when input String was correct Chief Address format
     *      TCP Client ServerIP and ServerPort will be set
     *      and new Thread will be started to establish connection
     *
     *  2.  when input String was incorrect Chief Address format
     *      MESSAGE TOAST shall be thrown
     */
    @Test
    public void testTryConnectWithQR_If_correct_String_format() throws Exception{
        assertNull(JavaHost.getConnector());
        assertNull(ExternalDbLoader.getConnectionTask());

        model.prepareConnector(new String[]{"$CHIEF", "192.168.0.1", "5000", "DUEL", "$"});

        verify(dbLoader).saveConnector(any(Connector.class));
        assertEquals("192.168.0.1", JavaHost.getConnector().getIpAddress());
        assertEquals(Integer.valueOf(5000), JavaHost.getConnector().getPortNumber());
        assertEquals(Role.CHIEF, JavaHost.getConnector().getMyHost());

    }

    @Test
    public void testTryConnectWithQR_If_wrong_String_format() throws Exception{
        try{
            assertNull(JavaHost.getConnector());
            assertNull(ExternalDbLoader.getConnectionTask());

            model.tryConnectWithQR("$CHIEF:192.168.0.1:5000:DUEL:");

            fail("Expected MESSAFE TOAST Exception but none were thrown");
        } catch (ProcessException err){
            assertNull(JavaHost.getConnector());
            assertEquals(ProcessException.MESSAGE_TOAST, err.getErrorType());
            assertEquals("Not a chief address", err.getErrorMsg());
            verify(dbLoader, never()).saveConnector(any(Connector.class));
            assertNull(ExternalDbLoader.getConnectionTask());
        }
    }

    //= TryConnectWithDatabase() ===================================================================

    /**
     * tryConnectWithDatabase(...)
     *
     * 1. Database is empty, return false and did not start any thread
     * 2. Database stored invalid connector, return false and did not start any thread
     * 3. Database contain valid connector, start a new thread and return true
     */
    @Test
    public void testTryConnectWithDatabase_withEmptyDb_return_false() throws Exception {
        when(dbLoader.queryConnector()).thenReturn(null);
        assertNull(ExternalDbLoader.getConnectionTask());

        assertFalse(model.tryConnectWithDatabase());
        assertNull(ExternalDbLoader.getConnectionTask());
        verify(dbLoader, never()).clearDatabase();
        verify(dbLoader, never()).clearUserDatabase();
    }

    @Test
    public void testTryConnectWithDatabase_withInvalidDb_return_false() throws Exception {
        Connector connector         = new Connector("127.0.0.1", 6666, "DUEL");
        Calendar date               = Calendar.getInstance();
        date.set(2016, 7, 18);
        connector.setDate(date);
        assertNull(ExternalDbLoader.getConnectionTask());
        when(dbLoader.queryConnector()).thenReturn(connector);

        assertFalse(model.tryConnectWithDatabase());

        assertNull(ExternalDbLoader.getConnectionTask());
        verify(dbLoader).clearDatabase();
        verify(dbLoader).clearUserDatabase();
    }

    @Test
    public void testTryConnectWithDatabase_withValidDb_return_true() throws Exception {
        Connector connector         = new Connector("127.0.0.1", 6666, "DUEL");
        when(dbLoader.queryConnector()).thenReturn(connector);
        assertNull(ExternalDbLoader.getConnectionTask());

        assertTrue(model.tryConnectWithDatabase());
        assertNotNull(ExternalDbLoader.getConnectionTask());
        verify(dbLoader, never()).clearDatabase();
        verify(dbLoader, never()).clearUserDatabase();
    }

    //= CloseConnection() ==========================================================================
    /**
     * closeConnection()
     *
     * 1. When ConnectTask = null, JavaHost = null, do nothing
     * 2. When ConnectTask != null, JavaHost = null, cancel ConnectTask
     * 3. When ConnectTask = null, JavaHost != null, stop JavaHost
     * 4. When ConnectTask != null, JavaHost != null, stop TCP and cancel ConnectTask
     */
    @Test
    public void testCloseConnection_BothNull() throws Exception {
        ConnectionTask task = Mockito.mock(ConnectionTask.class);
        JavaHost client    = Mockito.mock(JavaHost.class);

        model.closeConnection();

        verify(client, never()).stopClient();
        verify(task, never()).cancel(true);
        assertNull(ExternalDbLoader.getConnectionTask());
    }

    @Test
    public void testCloseConnection_TaskNotNull() throws Exception {
        ConnectionTask task = Mockito.mock(ConnectionTask.class);
        JavaHost client    = Mockito.mock(JavaHost.class);
        ExternalDbLoader.setConnectionTask(task);

        model.closeConnection();

        verify(client, never()).stopClient();
        verify(task).cancel(true);
        assertNull(ExternalDbLoader.getConnectionTask());
    }

    @Test
    public void testCloseConnection_TCPClientNotNull() throws Exception {
        ConnectionTask task = Mockito.mock(ConnectionTask.class);
        JavaHost client    = Mockito.mock(JavaHost.class);
        ExternalDbLoader.setJavaHost(client);

        model.closeConnection();

        verify(client).stopClient();
        verify(task, never()).cancel(true);
        assertNull(ExternalDbLoader.getConnectionTask());
    }

    @Test
    public void testCloseConnection_BothNotNull() throws Exception {
        ConnectionTask task = Mockito.mock(ConnectionTask.class);
        JavaHost client    = Mockito.mock(JavaHost.class);
        ExternalDbLoader.setJavaHost(client);
        ExternalDbLoader.setConnectionTask(task);

        model.closeConnection();

        verify(client).stopClient();
        verify(task).cancel(true);
        assertNull(ExternalDbLoader.getConnectionTask());
    }

    //= OnChallengeMessageReceived(...) ============================================================
    /**
     * onChallengeMessageReceived(...)
     *
     * parse the Chief Message and set the ChallengeMessage of the connector
     *
     * Tests:
     * 1. Correct format, set the challenge message
     * 2. Wrong format, throw error and did not set the message
     */
    @Test
    public void testOnChallengeMessage1_PositiveTest() throws Exception {
        try{
            javaHost = Mockito.mock(JavaHost.class);
            task = Mockito.mock(ConnectionTask.class);
            Connector connector = Mockito.mock(Connector.class);
            ExternalDbLoader.setConnectionTask(task);
            ExternalDbLoader.setJavaHost(javaHost);
            JavaHost.setConnector(connector);

            model.onChallengeMessageReceived("{\"Result\":true, \"DuelMsg\":\"AxPS9a0dvwde\"}");

            verify(connector).setDuelMessage("AxPS9a0dvwde");
        } catch (ProcessException err) {
            fail("Exception --" + err.getErrorMsg() + "-- is not expected");
        }
    }

    @Test
    public void testOnChallengeMessage2_NegativeTest() throws Exception {
        task = Mockito.mock(ConnectionTask.class);
        javaHost = Mockito.mock(JavaHost.class);
        Connector connector = Mockito.mock(Connector.class);
        ExternalDbLoader.setConnectionTask(task);
        ExternalDbLoader.setJavaHost(javaHost);
        JavaHost.setConnector(connector);

        try{
            model.onChallengeMessageReceived("{\"Result\":true}");
            fail("Expected MESSAGE_DIALOG but none thrown");
        } catch (ProcessException err) {
            assertEquals(ProcessException.MESSAGE_DIALOG, err.getErrorType());
            assertEquals("Failed to read data from Chief\n" +
                    "Please consult developer!", err.getErrorMsg());
            verify(connector, never()).setDuelMessage(anyString());
        }
    }

    //= Reconnect() ================================================================================

    /**
     * reconnect()
     *
     * Check if the request of challenge message was sent to the Chief
     *
     * Tests:
     * 1. Return true when there is a user in the database
     * 2. Return false when there is no user in the database
     *
     */
    @Test
    public void testReconnect1_PositiveTest() throws Exception {
        task = Mockito.mock(ConnectionTask.class);
        javaHost = Mockito.mock(JavaHost.class);
        ExternalDbLoader.setConnectionTask(task);
        ExternalDbLoader.setJavaHost(javaHost);
        when(dbLoader.emptyUserInDB()).thenReturn(false);
        when(dbLoader.queryUser()).thenReturn(staff);

        assertTrue(model.reconnect());

        verify(javaHost).putMessageIntoSendQueue(anyString());
    }

    @Test
    public void testReconnect2_NegativeTest() throws Exception {
        task = Mockito.mock(ConnectionTask.class);
        javaHost = Mockito.mock(JavaHost.class);
        ExternalDbLoader.setConnectionTask(task);
        ExternalDbLoader.setJavaHost(javaHost);
        when(dbLoader.emptyUserInDB()).thenReturn(true);

        assertFalse(model.reconnect());

        verify(javaHost, never()).putMessageIntoSendQueue(anyString());
    }

    //= Run() ======================================================================================
    /**
     * run()
     *
     * 1. When ConnectionTask is complete, do nothing
     * 2. When ConnectionTask is not complete, throw an error and the presenter shall handle
     */
    @Test
    public void testRun_ChiefDoRespond() throws Exception {
        when(taskPresenter.isRequestComplete()).thenReturn(true);
        model.run();
        verify(taskPresenter, never()).onTimesOut(any(ProcessException.class));
    }

    @Test
    public void testRun_ChiefNoRespond() throws Exception {
        when(taskPresenter.isRequestComplete()).thenReturn(false);
        model.run();
        verify(taskPresenter).onTimesOut(any(ProcessException.class));
    }

}