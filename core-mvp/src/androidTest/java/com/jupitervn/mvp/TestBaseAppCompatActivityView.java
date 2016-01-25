package com.jupitervn.mvp;

import com.jupiter.mvp.test.common.AppCompatActivityViewForTest;
import com.jupitervn.mvp.common.presenter.PresenterLifecycle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.FlakyTest;
import android.support.test.internal.runner.lifecycle.ActivityLifecycleMonitorImpl;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/14/16.
 */
@RunWith(AndroidJUnit4.class)
@FlakyTest(detail = "These tests are flaky because of memory and lifecycle event")
public class TestBaseAppCompatActivityView extends ActivityInstrumentationTestCase2<AppCompatActivityViewForTest> {

    private AppCompatActivityViewForTest activity;
    private Instrumentation.ActivityMonitor monitor;

    public TestBaseAppCompatActivityView() {
        super(AppCompatActivityViewForTest.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir().getPath());
        ActivityLifecycleMonitorRegistry.registerInstance(new ActivityLifecycleMonitorImpl());
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        monitor = new Instrumentation.ActivityMonitor(AppCompatActivityViewForTest.class.getName(), null, false);
        getInstrumentation().addMonitor(monitor);
        activity = getActivity();
        Log.d("Lifecycle", "Activity " + activity + " " + activity.getPresenterLifecycle());
    }

    @After
    public void tearDown() throws Exception {
        Log.d("LifecycleMonitor", "End of test");
        super.tearDown();
        getInstrumentation().removeMonitor(monitor);
        activity = null;
        monitor = null;
        ActivityLifecycleMonitorRegistry.registerInstance(null);
    }

    @Test
    public void testOnCreateShouldCreatePresenter() throws Exception {
        final PresenterLifecycle presenterLifecycle = activity.getPresenterLifecycle();
        verify(presenterLifecycle).onCreate(Mockito.isNull(Bundle.class));
    }

    @Test
    public void testPresenterLifecycleWhenOrientationChanged() throws Exception {
        PresenterLifecycle presenterLifecycle = activity.getPresenterLifecycle();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getInstrumentation().waitForIdleSync();
        verify(presenterLifecycle).onCreate(Mockito.isNull(Bundle.class));
        verify(presenterLifecycle).onResume(eq(activity));
        verify(presenterLifecycle).onPause(eq(false));
        verify(presenterLifecycle).onSavePresenterState(any(Bundle.class));
        verify(presenterLifecycle).destroyPresenterIfNeeded(eq(false));
        getInstrumentation().waitForIdleSync();
        activity = (AppCompatActivityViewForTest) getInstrumentation().waitForMonitor(monitor);
        Log.d("Lifecycle", "New Activity " + activity);
        presenterLifecycle = activity.getPresenterLifecycle();
        verify(presenterLifecycle).onCreate(any(Bundle.class));
        verify(presenterLifecycle).onResume(eq(activity));
    }

    @Test
    public void testPresenterLifecycleWhenOrientationChangedWithSavedState() throws Exception {
        PresenterLifecycle presenterLifecycle = activity.getPresenterLifecycle();
        final Bundle[] presenterSavedState = {new Bundle()};
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                presenterSavedState[0] = (Bundle) invocation.getArguments()[0];
                return null;
            }
        }).when(presenterLifecycle).onSavePresenterState(any(Bundle.class));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getInstrumentation().waitForIdleSync();
        verify(presenterLifecycle).onCreate(Mockito.isNull(Bundle.class));
        verify(presenterLifecycle).onResume(eq(activity));
        verify(presenterLifecycle).onPause(eq(false));
        verify(presenterLifecycle).onSavePresenterState(any(Bundle.class));
        verify(presenterLifecycle).destroyPresenterIfNeeded(eq(false));
        getInstrumentation().waitForIdleSync();
        activity = (AppCompatActivityViewForTest) getInstrumentation().waitForMonitor(monitor);
        Log.d("Lifecycle", "New Activity " + activity);
        presenterLifecycle = activity.getPresenterLifecycle();
        verify(presenterLifecycle).onCreate(Mockito.eq(presenterSavedState[0]));
        verify(presenterLifecycle).onResume(eq(activity));
    }

    @Test
    public void testPresenterLifecycleWhenFinish() throws Throwable {
        PresenterLifecycle presenterLifecycle = activity.getPresenterLifecycle();
        activity.finish();
        setActivity(null);
        getInstrumentation().waitForIdleSync();
        //Trick to wait for onpause and onstop to be triggered
        sleepToWaitForDestroyEvents();
        verify(presenterLifecycle).onCreate(Mockito.isNull(Bundle.class));
        verify(presenterLifecycle).onResume(eq(activity));
        verify(presenterLifecycle).onPause(eq(true));
        verify(presenterLifecycle, times(0)).onSavePresenterState(any(Bundle.class));
        verify(presenterLifecycle).destroyPresenterIfNeeded(eq(true));
    }

    @Test
    public void testPresenterLifecycleWhenOpenNextActivityWithFlagClearTask() throws Exception {
        PresenterLifecycle presenterLifecycle = activity.getPresenterLifecycle();
        getInstrumentation().waitForIdleSync();
        Intent intent = new Intent(activity, AppCompatActivityViewForTest.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        sleepToWaitForDestroyEvents();
        verify(presenterLifecycle).onCreate(Mockito.isNull(Bundle.class));
        verify(presenterLifecycle).onResume(eq(activity));
        verify(presenterLifecycle).onPause(eq(true));
        verify(presenterLifecycle, times(0)).onSavePresenterState(any(Bundle.class));
        verify(presenterLifecycle).destroyPresenterIfNeeded(eq(true));
    }

    @Test
    public void testPresenterLifecycleWhenOpen2ActivitiesWithFlagClearTask() throws Exception {
        PresenterLifecycle presenterLifecycle = activity.getPresenterLifecycle();
        getInstrumentation().waitForIdleSync();
        Intent intent = new Intent(activity, DummyActivity.class);
        activity.startActivity(intent);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();
        sleepToWaitForDestroyEvents();
        verify(presenterLifecycle).onCreate(Mockito.isNull(Bundle.class));
        verify(presenterLifecycle).onResume(eq(activity));
        verify(presenterLifecycle).onPause(eq(false));
        verify(presenterLifecycle, times(0)).onSavePresenterState(any(Bundle.class));
        verify(presenterLifecycle).destroyPresenterIfNeeded(eq(true));
    }

    private void sleepToWaitForDestroyEvents() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Test
    public void testPresenterLifecycleWhenOpenOldActivityWithClearTop() throws Exception {
        PresenterLifecycle presenterLifecycle = activity.getPresenterLifecycle();
        getInstrumentation().waitForIdleSync();
        Intent intent = new Intent(getInstrumentation().getTargetContext(), DummyActivity.class);
        activity.startActivity(intent);
        Intent openBackActivityIntent = new Intent(getInstrumentation().getTargetContext(), AppCompatActivityViewForTest.class);
        openBackActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(openBackActivityIntent);
        getInstrumentation().waitForIdleSync();
        sleepToWaitForDestroyEvents();
        verify(presenterLifecycle).onCreate(Mockito.isNull(Bundle.class));
        verify(presenterLifecycle, times(2)).onResume(eq(activity));
        verify(presenterLifecycle).onPause(eq(false));
        verify(presenterLifecycle, times(0)).onSavePresenterState(any(Bundle.class));
        verify(presenterLifecycle, times(0)).destroyPresenterIfNeeded(Mockito.anyBoolean());
    }
}
