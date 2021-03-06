package com.info.ghiny.examsystem.database;

import android.os.PowerManager;

import com.info.ghiny.examsystem.interfacer.DistributionMVP;
import com.info.ghiny.examsystem.model.AndroidClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;
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
public class ThreadManagerTest {

    private DistributionMVP.MvpView tempView;
    private DistributionMVP.MvpModel tempModel;

    private HashMap<Integer, AndroidClient> testMap;
    private AndroidClient thread1;
    private AndroidClient thread2;
    private AndroidClient thread3;
    private AndroidClient thread4;

    @Before
    public void setUp() throws Exception {
        tempView    = Mockito.mock(DistributionMVP.MvpView.class);
        tempModel   = Mockito.mock(DistributionMVP.MvpModel.class);
        testMap     = new HashMap<>();

        thread1     = Mockito.mock(AndroidClient.class);
        thread2     = Mockito.mock(AndroidClient.class);
        thread3     = Mockito.mock(AndroidClient.class);
        thread4     = Mockito.mock(AndroidClient.class);

        testMap.put(10001, thread1);
        testMap.put(10002, thread2);
        testMap.put(10003, thread3);
        testMap.put(10004, thread4);
    }

    @After
    public void tearDown() throws Exception {
        ThreadManager.setClientsMap(null);
    }

    //= OnCreate() =================================================================================
    /**
     * onCreate()
     *
     * When the Service first created, set the running to true to indicate the service is running
     */

    @Test
    public void onCreate() throws Exception {
        assertFalse(ThreadManager.isRunning());
        PowerManager powerManager   = Mockito.mock(PowerManager.class);
        PowerManager.WakeLock wakeLock   = Mockito.mock(PowerManager.WakeLock.class);

        ThreadManager synchronizer  = new ThreadManager();
        ThreadManager.setPowerManager(powerManager);
        ThreadManager.setWakeLock(wakeLock);

        synchronizer.onCreate();

        assertTrue(ThreadManager.isRunning());
    }

    //= OnDestroy() =================================================================================
    /**
     * onDestroy()
     *
     * When the Service is destroyed, stop all the client and clear the Client Map
     */

    @Test
    public void onDestroy() throws Exception {
        ThreadManager.setClientsMap(testMap);
        assertEquals(4, testMap.size());

        ThreadManager synchronizer  = new ThreadManager();
        synchronizer.onDestroy();

        verify(thread1).stopClient();
        verify(thread2).stopClient();
        verify(thread3).stopClient();
        verify(thread4).stopClient();

        assertEquals(0, testMap.size());

    }

    //= StartNewThread =============================================================================

    /**
     * startNewThread(...)
     *
     * As the name suggest, this method start a new Android Client Thread to allow
     * another Client to connect
     *
     */

    @Test
    public void startNewThread() throws Exception {
        assertNull(ThreadManager.getWaitingThread());

        ThreadManager.startNewThread(tempView, tempModel);

        AndroidClient testThread    = ThreadManager.getWaitingThread();
        assertNotNull(testThread);
        assertEquals(tempView, testThread.getTempView());
        assertEquals(tempModel, testThread.getTempModel());
    }

    //= UpdateAttendance ===========================================================================

    /**
     * updateAttendance(...)
     *
     * This method is called if and ONLY if the user is In-Charge
     * This method is take in an Array of Candidate Attendance and send to all it's client
     *
     * IMPORTANT NOTE: The Array of Candidate Attendance is the attendance collected
     *                 by the IN_CHARGE himself/herself, NOT attendance collected by
     *                 other client.
     *
     * Tests:
     * 1. Send the Update to all Clients when the list have candidates
     * 2. Do nothing when the input updating list got nothing
     * 3. Do nothing when there is no client connected
     */

    @Test
    public void updateAttendance_1_PositiveTest() throws Exception {
        ThreadManager.setClientsMap(testMap);
        ArrayList<Candidate> cdds = new ArrayList<>();
        Candidate cdd1  = new Candidate();
        cdd1.setRegNum("15WAU00001");
        cdd1.setTableNumber(10);
        cdd1.setStatus(Status.PRESENT);
        cdd1.setCollector("000001");
        cdd1.setLate(false);
        cdds.add(cdd1);
        assertEquals(1, cdds.size());

        ThreadManager.updateAttendance(cdds);

        String MSG_OUT  = "{\"Type\":\"AttendanceUpdate\"," +
                "\"DeviceId\":0," +
                "\"UpdateList\":[{" +
                "\"Attendance\":\"PRESENT\"," +
                "\"AttdCollector\":\"000001\"," +
                "\"Late\":false," +
                "\"TableNo\":10," +
                "\"RegNum\":\"15WAU00001\"}]}";

        verify(thread1).putMessageIntoSendQueue(MSG_OUT);
        verify(thread2).putMessageIntoSendQueue(MSG_OUT);
        verify(thread3).putMessageIntoSendQueue(MSG_OUT);
        verify(thread4).putMessageIntoSendQueue(MSG_OUT);
    }

    @Test
    public void updateAttendance_2_NegativeTest1_EmptyArray() throws Exception {
        ThreadManager.setClientsMap(testMap);
        ArrayList<Candidate> cdds = new ArrayList<>();
        assertEquals(0, cdds.size());

        ThreadManager.updateAttendance(cdds);

        verify(thread1).putMessageIntoSendQueue(anyString());
        verify(thread2).putMessageIntoSendQueue(anyString());
        verify(thread3).putMessageIntoSendQueue(anyString());
        verify(thread4).putMessageIntoSendQueue(anyString());
    }

    @Test
    public void updateAttendance_2_NegativeTest2_NoClientConnected() throws Exception {
        ThreadManager.setClientsMap(new HashMap<Integer, AndroidClient>());
        ArrayList<Candidate> cdds = new ArrayList<>();
        Candidate cdd1  = new Candidate();
        cdd1.setRegNum("15WAU00001");
        cdd1.setTableNumber(10);
        cdd1.setStatus(Status.PRESENT);
        cdd1.setCollector("000001");
        cdd1.setLate(false);
        cdds.add(cdd1);
        assertEquals(1, cdds.size());

        ThreadManager.updateAttendance(cdds);

        verify(thread1, never()).putMessageIntoSendQueue(anyString());
        verify(thread2, never()).putMessageIntoSendQueue(anyString());
        verify(thread3, never()).putMessageIntoSendQueue(anyString());
        verify(thread4, never()).putMessageIntoSendQueue(anyString());
    }


    //= PassMessageBack ============================================================================

    /**
     * passMessageBack(...)
     *
     * This method pass any message that was pre-requested by any Client to them
     *
     * Tests:
     * 1. When the desired client is still connected to the user.
     * 2. When the desired client is no longer connected to the user.
     */

    @Test
    public void passMessageBack_1_ClientStillConnected() throws Exception {
        ThreadManager.setClientsMap(testMap);

        ThreadManager.passMessageBack(10001, "Msg from Chief To Client");

        verify(thread1).putMessageIntoSendQueue("Msg from Chief To Client");
        verify(thread2, never()).putMessageIntoSendQueue(anyString());
        verify(thread3, never()).putMessageIntoSendQueue(anyString());
        verify(thread4, never()).putMessageIntoSendQueue(anyString());
    }

    @Test
    public void passMessageBack_2_ClientDisconnected() throws Exception {
        ThreadManager.setClientsMap(testMap);

        ThreadManager.passMessageBack(10005, "Msg from Chief To Client");

        verify(thread1, never()).putMessageIntoSendQueue(anyString());
        verify(thread2, never()).putMessageIntoSendQueue(anyString());
        verify(thread3, never()).putMessageIntoSendQueue(anyString());
        verify(thread4, never()).putMessageIntoSendQueue(anyString());
    }

    //= NotifyClientConnected ======================================================================

    /**
     * notifyClientConnected(...)
     *
     * This method were called when the last thread have connected to somebody.
     * It will then start a new Thread
     */
    @Test
    public void notifyClientConnected() throws Exception {
        ThreadManager.setClientsMap(testMap);
        assertEquals(4, testMap.size());
        assertFalse(testMap.containsKey(10005));
        assertFalse(ThreadManager.isDistributed());

        AndroidClient thread5 = Mockito.mock(AndroidClient.class);
        when(thread5.getLocalPort()).thenReturn(10005);
        ThreadManager.setWaitingThread(thread5);
        assertEquals(thread5, ThreadManager.getWaitingThread());


        ThreadManager.notifyClientConnected(thread5);

        assertEquals(5, testMap.size());
        assertTrue(testMap.containsKey(10005));
        assertTrue(ThreadManager.isDistributed());
        assertNotEquals(thread5, ThreadManager.getWaitingThread());
    }

    //= RemoveUnconnectedThread ====================================================================

    /**
     * removeUnconnectedThread(...)
     *
     * This method was called when Distribution activity was destroyed
     * So that the thread no longer have to wait for connection at Server.accept
     */
    @Test
    public void removeUnconnectedThread() throws Exception {
        AndroidClient thread5 = Mockito.mock(AndroidClient.class);
        ThreadManager.setWaitingThread(thread5);

        ThreadManager.removeUnconnectedThread();

        verify(thread5).stopClient();
        assertNull(ThreadManager.getWaitingThread());
    }

    //= GetThisIpv4 ================================================================================

    /**
     * getThisIpv4()
     *
     * This method return a String that consist the IP address of the user device in the network
     * This test will fail due to changes of ip address in different network
     */
    public void getThisIpv4() throws Exception {
        String ip   = ThreadManager.getThisIpv4();
        assertEquals("192.168.95.1", ip);
    }
}