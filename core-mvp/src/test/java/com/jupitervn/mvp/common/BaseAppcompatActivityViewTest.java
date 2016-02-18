package com.jupitervn.mvp.common;

import com.jupiter.mvp.test.common.AppCompatActivityViewForTest;
import com.jupitervn.mvp.BuildConfig;
import com.jupitervn.mvp.common.presenter.PresenterLifecycle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import android.os.Bundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/13/16.
 */
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/test/AndroidManifest.xml")
@RunWith(CustomRobolectricTestRunner.class)
public class BaseAppcompatActivityViewTest {

    private ActivityController<AppCompatActivityViewForTest> activityController;

    @Before
    public void setUp() throws Exception {
        activityController = Robolectric.buildActivity(AppCompatActivityViewForTest.class);
    }

    @After
    public void tearDown() throws Exception {
        activityController.pause().destroy();
    }

    @Test
    public void testOnCreateShouldAlsoCreatePresenter() throws Exception {
        AppCompatActivityViewForTest appCompatActivityViewForTest = activityController.create().postCreate(null).get();
        PresenterLifecycle mockPresenterLifecycle = appCompatActivityViewForTest.getPresenterLifecycle();
        verify(mockPresenterLifecycle).onCreate(Mockito.isNull(Bundle.class));
    }

    @Test
    public void testOnCreateShouldCreatePresenterIfRestart() throws Exception {
        Bundle savedState = new Bundle();
        final Bundle[] presenterSavedState = {new Bundle()};
        activityController.setup();
        AppCompatActivityViewForTest appCompatActivityViewForTest = activityController.get();
        PresenterLifecycle mockPresenterLifecycle = appCompatActivityViewForTest.getPresenterLifecycle();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                presenterSavedState[0] = (Bundle) invocation.getArguments()[0];
                return null;
            }
        }).when(mockPresenterLifecycle).onSavePresenterState(any(Bundle.class));
        activityController.pause().saveInstanceState(savedState).destroy();
        verify(mockPresenterLifecycle).onCreate(Mockito.isNull(Bundle.class));
        verify(mockPresenterLifecycle).onSavePresenterState(eq(presenterSavedState[0]));
        assertThat(appCompatActivityViewForTest.isFinishing()).isFalse();
        verify(mockPresenterLifecycle).onPause(Mockito.eq(false));
        activityController = Robolectric.buildActivity(AppCompatActivityViewForTest.class).setup(savedState);
        appCompatActivityViewForTest = activityController.get();
        mockPresenterLifecycle = appCompatActivityViewForTest.getPresenterLifecycle();
        verify(mockPresenterLifecycle).onCreate(Mockito.eq(presenterSavedState[0]));
    }
}