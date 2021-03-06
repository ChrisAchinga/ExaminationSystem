package com.info.ghiny.examsystem.manager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.info.ghiny.examsystem.CollectionActivity;
import com.info.ghiny.examsystem.DistributionActivity;
import com.info.ghiny.examsystem.InfoGrabActivity;
import com.info.ghiny.examsystem.PopUpLogin;
import com.info.ghiny.examsystem.SettingActivity;
import com.info.ghiny.examsystem.TakeAttdActivity;
import com.info.ghiny.examsystem.database.AttendanceList;
import com.info.ghiny.examsystem.database.ExternalDbLoader;
import com.info.ghiny.examsystem.interfacer.HomeOptionMVP;
import com.info.ghiny.examsystem.interfacer.TakeAttdMVP;
import com.info.ghiny.examsystem.model.ConnectionTask;
import com.info.ghiny.examsystem.model.JavaHost;
import com.info.ghiny.examsystem.model.ProcessException;
import com.info.ghiny.examsystem.model.TakeAttdModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
public class HomeOptionPresenterTest {

    private HomeOptionPresenter manager;
    private HomeOptionMVP.MvpModel taskModel;
    private HomeOptionMVP.MvpView taskView;
    private ConnectionTask task;
    private JavaHost javaHost;
    private Handler handler;
    private DialogInterface dialog;


    @Before
    public void setUp() throws Exception {
        taskView    = Mockito.mock(HomeOptionMVP.MvpView.class);
        taskModel   = Mockito.mock(HomeOptionMVP.MvpModel.class);
        handler     = Mockito.mock(Handler.class);
        dialog      = Mockito.mock(DialogInterface.class);
        javaHost    = Mockito.mock(JavaHost.class);
        task        = Mockito.mock(ConnectionTask.class);

        ExternalDbLoader.setConnectionTask(task);
        ExternalDbLoader.setJavaHost(javaHost);
        ConnectionTask.setCompleteFlag(false);

        manager = new HomeOptionPresenter(taskView);
        manager.setTaskModel(taskModel);
        manager.setHandler(handler);
        TakeAttdModel.setAttdList(null);
    }

    @After
    public void tearDown() throws Exception {}

    //= OnResume(...) ==============================================================================
    /**
     * onResume(ErrorManager errManager)
     *
     * Tests:
     * 1. Set message listener to JavaHost and download the attendance list (org is null)
     * 2. Set message listener to JavaHost and do nothing (attendance list is not null)
     * 3. Set message listener to JavaHost and retrieve attendance list from db (org is null)
     *
     */
    @Test
    public void testOnResume1_AttdListIsNull_Download() throws Exception {
        ErrorManager errorManager = Mockito.mock(ErrorManager.class);
        TakeAttdModel.setAttdList(null);
        when(taskModel.isInitialized()).thenReturn(false);

        manager.onResume(errorManager);

        verify(javaHost).setMessageListener(any(JavaHost.OnMessageReceived.class));
        verify(taskModel).initAttendance();
    }

    @Test
    public void testOnResume2_AttdListNotNull_DoNothing() throws Exception {
        ErrorManager errorManager = Mockito.mock(ErrorManager.class);
        TakeAttdModel.setAttdList(new AttendanceList());
        when(taskModel.isInitialized()).thenReturn(true);

        manager.onResume(errorManager);

        verify(javaHost).setMessageListener(any(JavaHost.OnMessageReceived.class));
        verify(taskModel, never()).initAttendance();
    }

    @Test
    public void testOnResume3_AttdListIsNull_LoadDatabase() throws Exception {
        ErrorManager errorManager = Mockito.mock(ErrorManager.class);
        TakeAttdModel.setAttdList(null);
        when(taskModel.isInitialized()).thenReturn(true);

        manager.onResume(errorManager);

        verify(javaHost).setMessageListener(any(JavaHost.OnMessageReceived.class));
        verify(taskModel).initAttendance();
        verify(taskView, never()).openProgressWindow(anyString(), anyString());
        verify(handler, never()).postDelayed(any(Runnable.class), anyInt());
    }

    //= OnRestart() ================================================================================
    /**
     * onRestart()
     *
     * ask user to key in password when the activity restarted
     *
     * @throws Exception
     */
    @Test
    public void testOnRestart1_NotNavigationTrigger() throws Exception {
        manager.setNavFlag(false);

        manager.onRestart();

        verify(taskView).securityPrompt(false);
    }

    @Test
    public void testOnRestart_TriggerByNavigation() throws Exception {
        manager.setNavFlag(true);

        manager.onRestart();

        verify(taskView, never()).securityPrompt(false);
    }

    //= onDestroy() ================================================================================
    /**
     * onDestroy()
     *
     * When this screen is about to destroy
     * save the data into database, close any window and stop any timer
     */
    @Test
    public void testOnDestroy() throws Exception {
        manager.onDestroy();

        verify(taskView).closeProgressWindow();
        verify(taskModel).saveAttendance();
        verify(handler).removeCallbacks(taskModel);
    }

    //= OnPasswordReceived() ========================================================================
    /**
     * onPasswordReceived()
     *
     * 1. when the password is correct, do nothing
     * 2. when the password is incorrect, display the error and prompt the user password again
     *
     * @throws Exception
     */
    @Test
    public void testOnPasswordReceived_CorrectPassword() throws Exception {
        Intent pw    = Mockito.mock(Intent.class);
        when(pw.getStringExtra("Password")).thenReturn("123456");
        doNothing().when(taskModel).matchPassword("123456");

        manager.onPasswordReceived(PopUpLogin.PASSWORD_REQ_CODE, Activity.RESULT_OK, pw);

        verify(taskModel).matchPassword("123456");
        verify(taskView, never()).displayError(any(ProcessException.class));
        verify(taskView, never()).securityPrompt(false);
    }

    @Test
    public void testOnPasswordReceived_IncorrectPassword() throws Exception {
        Intent pw    = Mockito.mock(Intent.class);
        when(pw.getStringExtra("Password")).thenReturn("wrong_Password");
        ProcessException err = new ProcessException(ProcessException.MESSAGE_TOAST);
        doThrow(err).when(taskModel).matchPassword("wrong_Password");

        manager.onPasswordReceived(PopUpLogin.PASSWORD_REQ_CODE, Activity.RESULT_OK, pw);

        verify(taskModel).matchPassword("wrong_Password");
        verify(taskView).displayError(err);
        verify(taskView).securityPrompt(false);
    }

    //= OnBackPressed() ============================================================================

    /**
     * onBackPressed()
     *
     * This method is important in Home Activity to prevent the user from accidentally logout
     * due to pressing back button to rapidly by asking user confirmation
     *
     */
    @Test
    public void onBackPressed() throws Exception {
        manager.onBackPressed();

        verify(taskModel).prepareLogout();
        verify(taskView).displayError(any(ProcessException.class));
    }

    //= OnSetting ==================================================================================

    /**
     * onSetting()
     *
     * Simple navigating method
     * make sure nav flag was flagged
     */
    @Test
    public void onSetting() throws Exception {
        manager.setNavFlag(false);

        manager.onSetting();

        assertTrue(manager.isNavFlag());
        verify(taskView).navigateActivity(SettingActivity.class);
    }

    //= OnAttendance ===============================================================================

    /**
     * OnAttendance()
     *
     * Simple navigating method
     * make sure nav flag was flagged
     */
    @Test
    public void onAttendance() throws Exception {
        manager.setNavFlag(false);

        manager.onAttendance();

        assertTrue(manager.isNavFlag());
        verify(taskView).navigateActivity(TakeAttdActivity.class);
    }

    //= OnCollection ===============================================================================

    /**
     * OnCollection()
     *
     * Simple navigating method
     * make sure nav flag was flagged
     */
    @Test
    public void onCollection() throws Exception {
        manager.setNavFlag(false);

        manager.onCollection();

        assertTrue(manager.isNavFlag());
        verify(taskView).navigateActivity(CollectionActivity.class);
    }

    //= OnDistribution =============================================================================
    /**
     * OnDistribution()
     *
     * Simple navigating method
     * make sure nav flag was flagged
     */
    @Test
    public void onDistribution() throws Exception {
        manager.setNavFlag(false);

        manager.onDistribution();

        assertTrue(manager.isNavFlag());
        verify(taskView).navigateActivity(DistributionActivity.class);
    }

    //= OnInfo =====================================================================================
    /**
     * OnInfo()
     *
     * Simple navigating method
     * make sure nav flag was flagged
     */
    @Test
    public void onInfo() throws Exception {
        manager.setNavFlag(false);

        manager.onInfo();

        assertTrue(manager.isNavFlag());
        verify(taskView).navigateActivity(InfoGrabActivity.class);
    }

    //= OnClick(...) ===============================================================================
    /**
     * onClick(...)
     *
     * When message pop out, only positive button will finish activity others do nothing
     * Tests
     * 1. Neutral Button Pressed
     * 2. Negative Button Pressed
     * 3. Positive Button Pressed   (finish activity)
     */
    @Test
    public void testOnClickNeutralButton() throws Exception {
        manager.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);

        verify(dialog).cancel();
        verify(taskView, never()).finishActivity();
    }

    @Test
    public void testOnClickNegativeButton() throws Exception {
        manager.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);

        verify(dialog).cancel();
        verify(taskView, never()).finishActivity();
    }

    @Test
    public void testOnClickPositiveButton() throws Exception {
        manager.onClick(dialog, DialogInterface.BUTTON_POSITIVE);

        verify(dialog, never()).cancel();
        verify(taskView).finishActivity();
    }

    //= OnCancel(...) ==============================================================================
    /**
     * onCancel(...)
     *
     * Sometimes, a pop out window could be cancelled by pressing the back button
     * of the phone
     *
     */
    @Test
    public void testOnCancel() throws Exception {
        manager.onCancel(dialog);

        verify(dialog).cancel();
    }

    //= onChiefRespond() ===========================================================================
    /**
     * onChiefRespond()
     *
     * When model start request for attendance list, chief will respond
     * This method will be called when the respond received
     *
     * Tests:
     * 1. Received message, send to model, model did not complain
     * 2. Received message, send to model, model complain with Exception
     *
     */
    @Test
    public void testOnChiefRespond1_PositiveResult() throws Exception {
        ErrorManager errorManager   = Mockito.mock(ErrorManager.class);
        doNothing().when(taskModel).checkDownloadResult("{\"Type\":\"VenueInfo\",\"Value\":\"ATTENDANCE & PAPERS\"}");


        manager.onChiefRespond(errorManager, "{\"Type\":\"VenueInfo\",\"Value\":\"ATTENDANCE & PAPERS\"}");


        verify(taskView).closeProgressWindow();
        verify(taskModel).checkDownloadResult("{\"Type\":\"VenueInfo\",\"Value\":\"ATTENDANCE & PAPERS\"}");
        verify(task, never()).publishError(any(ErrorManager.class), any(ProcessException.class));
    }

    @Test
    public void testOnChiefRespond2_NegativeResult() throws Exception {
        ErrorManager errorManager   = Mockito.mock(ErrorManager.class);
        ProcessException err    = new ProcessException(ProcessException.FATAL_MESSAGE);
        doThrow(err).when(taskModel).checkDownloadResult("{\"Type\":\"VenueInfo\",\"Value\":\"ATTENDANCE & PAPERS\"}");

        manager.onChiefRespond(errorManager, "{\"Type\":\"VenueInfo\",\"Value\":\"ATTENDANCE & PAPERS\"}");

        verify(taskView).closeProgressWindow();
        verify(taskModel).checkDownloadResult("{\"Type\":\"VenueInfo\",\"Value\":\"ATTENDANCE & PAPERS\"}");
        verify(task).publishError(errorManager, err);
    }

    //= OnTimesOut(...) ============================================================================
    /**
     * onTimesOut(...)
     *
     * When a message was sent to the chief to query attendance list,
     * a progress window will pop out and a timer will be started.
     * If there is no respond from the chief for 5 second, onTimesOut(...)
     * will be called.
     *
     * 1. When taskView is null, do nothing
     * 2. When taskView is not null, close the progress window and display the error
     *
     */
    @Test
    public void testOnTimesOutWithNullView() throws Exception {
        ProcessException err = new ProcessException(ProcessException.MESSAGE_TOAST);
        TakeAttdPresenter manager   = new TakeAttdPresenter(null, null);

        manager.onTimesOut(err);

        verify(taskView, never()).closeProgressWindow();
        verify(taskView, never()).displayError(err);
    }

    @Test
    public void testOnTimesOutWithView() throws Exception {
        ProcessException err = new ProcessException(ProcessException.MESSAGE_TOAST);

        manager.onTimesOut(err);

        verify(taskView).closeProgressWindow();
        verify(taskView).displayError(err);
    }

}