package com.info.ghiny.examsystem.manager;

import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;

import com.info.ghiny.examsystem.R;
import com.info.ghiny.examsystem.database.ExternalDbLoader;
import com.info.ghiny.examsystem.interfacer.SubmissionMVP;
import com.info.ghiny.examsystem.model.ConnectionTask;
import com.info.ghiny.examsystem.model.JavaHost;
import com.info.ghiny.examsystem.model.TakeAttdModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;
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

public class SubmissionPresenterTest_2_ViewTestRunner {

    private SubmissionPresenter manager;
    private SubmissionMVP.MvpModel taskModel;
    private SubmissionMVP.MvpView taskView;
    private ConnectionTask task;
    private JavaHost javaHost;
    private Handler handler;
    private DialogInterface dialog;

    @Before
    public void setUp() throws Exception {
        taskView    = Mockito.mock(SubmissionMVP.MvpView.class);
        taskModel   = Mockito.mock(SubmissionMVP.MvpModel.class);
        handler     = Mockito.mock(Handler.class);
        dialog      = Mockito.mock(DialogInterface.class);
        javaHost    = Mockito.mock(JavaHost.class);
        task        = Mockito.mock(ConnectionTask.class);

        ExternalDbLoader.setConnectionTask(task);
        ExternalDbLoader.setJavaHost(javaHost);
        ConnectionTask.setCompleteFlag(false);

        manager = new SubmissionPresenter(taskView);
        manager.setTaskModel(taskModel);
        manager.setHandler(handler);
        TakeAttdModel.setAttdList(null);
    }

    @After
    public void tearDown() throws Exception {}


    //= OnNavigationItemSelected ===================================================================

    /**
     * onNavigationItemSelected(...)
     *
     * This method handle the item selected from the drawer menu
     * 1. It create instance of the Fragment of the selection as the content of the activity
     * 2. It initialize the model and presenter layer
     * 3. It hide back the drawer as it was selected
     *
     */

    @Test
    public void onNavigationItemSelected() throws Exception {
        ErrorManager errorManager   = Mockito.mock(ErrorManager.class);
        Toolbar toolbar             = Mockito.mock(Toolbar.class);
        DrawerLayout drawerLayout   = Mockito.mock(DrawerLayout.class);
        FragmentManager fragManager = Mockito.mock(FragmentManager.class);
        FragmentTransaction ft      = Mockito.mock(FragmentTransaction.class);
        when(fragManager.beginTransaction()).thenReturn(ft);

        assertTrue(manager.onNavigationItemSelected(toolbar, R.id.nav_present, errorManager,
                fragManager, drawerLayout));

        verify(fragManager).beginTransaction();
        verify(drawerLayout).closeDrawer(GravityCompat.START);
    }



}