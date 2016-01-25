package com.jupitervn.mvp;

import com.jupitervn.mvp.common.presenter.PresenterLifecycle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.app.Instrumentation;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.FlakyTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/18/16.
 */
@RunWith(AndroidJUnit4.class)
@FlakyTest(detail = "These tests are flaky because of memory issue and lifecycle event issues")
public class TestBaseFragmentViewNoRetain extends ActivityInstrumentationTestCase2<FragmentActivityForTest> {
    public static final String FRAGMENT_TEST_TAG = "fragment-test";
    private BaseFragmentViewForTest baseFragmentView;
    private FragmentActivityForTest activity;
    private Instrumentation.ActivityMonitor monitor;

    public TestBaseFragmentViewNoRetain() {
        super(FragmentActivityForTest.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        monitor = new Instrumentation.ActivityMonitor(FragmentActivityForTest.class.getName(), null, false);
        getInstrumentation().addMonitor(monitor);
        activity = getActivity();
        baseFragmentView = (BaseFragmentViewForTest) activity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TEST_TAG);
        if (baseFragmentView == null) {
            baseFragmentView = new BaseFragmentViewForTest();
        }
        baseFragmentView.setRetainInstance(false);
        if (!baseFragmentView.isAdded()) {
            activity.getSupportFragmentManager().beginTransaction().replace(android.R.id.custom, baseFragmentView, FRAGMENT_TEST_TAG).commit();
        }
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        getInstrumentation().removeMonitor(monitor);
        activity = null;
        baseFragmentView = null;
        monitor = null;
    }

    @Test
    public void testFragmentLifecycleWhenStartFragment() throws Exception {
        getInstrumentation().waitForIdleSync();
        PresenterLifecycle presenterLifecycle = baseFragmentView.presenterLifecycle;
        verify(presenterLifecycle).onCreate(isNull(Bundle.class));
        verify(presenterLifecycle).onResume(eq(baseFragmentView));
        activity.finish();
        getInstrumentation().waitForIdleSync();
        Thread.sleep(2000);
        verify(presenterLifecycle).onPause(eq(true));
        verify(presenterLifecycle, times(0)).onSavePresenterState(any(Bundle.class));
        verify(presenterLifecycle).destroyPresenterIfNeeded(eq(true));
    }



    @Test
    public void testFragmentLifecycleIfRotate() throws Exception {
        getInstrumentation().waitForIdleSync();
        Log.d("Lifecycle", "New Activity " + activity + "  "  + baseFragmentView);
        PresenterLifecycle presenterLifecycle = baseFragmentView.presenterLifecycle;
        final Bundle[] savedStateArray = new Bundle[1];
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                savedStateArray[0] = (Bundle) invocation.getArguments()[0];
                savedStateArray[0].putString("test_key", "test_value");
                return null;
            }
        }).when(presenterLifecycle).onSavePresenterState(any(Bundle.class));
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getInstrumentation().waitForIdleSync();
        verify(presenterLifecycle).onCreate(isNull(Bundle.class));
        verify(presenterLifecycle).onResume(eq(baseFragmentView));
        verify(presenterLifecycle).onPause(eq(false));
        verify(presenterLifecycle).onSavePresenterState(any(Bundle.class));
        activity = (FragmentActivityForTest) getInstrumentation().waitForMonitor(monitor);
        baseFragmentView = (BaseFragmentViewForTest) activity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TEST_TAG);
        presenterLifecycle = baseFragmentView.presenterLifecycle;
        Log.d("Lifecycle", "New Activity " + activity + "  "  + baseFragmentView);
        //Fragment with retain instance set to true will not save fragment state.
        verify(presenterLifecycle).onCreate(eq(savedStateArray[0]));
        verify(presenterLifecycle).onResume(eq(baseFragmentView));
    }
}
