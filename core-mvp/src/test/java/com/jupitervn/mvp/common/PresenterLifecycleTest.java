package com.jupitervn.mvp.common;

import com.jupitervn.mvp.BuildConfig;
import com.jupitervn.mvp.common.presenter.BaseViewPresenter;
import com.jupitervn.mvp.common.presenter.PresenterFactory;
import com.jupitervn.mvp.common.presenter.PresenterLifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/13/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class PresenterLifecycleTest {

    private StubPresenterStorage stubPresenterStorage;
    @Mock
    private PresenterFactory mockPresenterFactory;
    private PresenterLifecycle presenterLifecycle;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        stubPresenterStorage = new StubPresenterStorage();
        when(mockPresenterFactory.createPresenter())
                .thenReturn(mock(BaseViewPresenter.class), mock(BaseViewPresenter.class));
        presenterLifecycle = spy(new PresenterLifecycle(stubPresenterStorage, mockPresenterFactory));
    }

    @Test
    public void testOnCreateShouldCreateNewFirstTime() throws Exception {
        BaseViewPresenter baseViewPresenter = presenterLifecycle.onCreate(null);
        verify(mockPresenterFactory).createPresenter();
        assertThat(stubPresenterStorage.presenterIdMap).hasSize(1);
        assertThat(stubPresenterStorage.presenterMap).hasSize(1);
        verify(baseViewPresenter).create(Mockito.<Bundle>eq(null));
    }

    @Test
    public void testOnCreateShouldNotCreateNewSecondTime() throws Exception {
        BaseViewPresenter beforeViewPresenter = presenterLifecycle.onCreate(null);
        verify(mockPresenterFactory).createPresenter();
        verify(beforeViewPresenter).create(Mockito.<Bundle>eq(null));
        Bundle outState = generateActivityBundle();
        presenterLifecycle.onSavePresenterState(outState);
        BaseViewPresenter afterViewPresenter = presenterLifecycle.onCreate(outState);
        verifyNoMoreInteractions(mockPresenterFactory);
        assertThat(beforeViewPresenter).isEqualTo(afterViewPresenter);
    }

    @NonNull
    private Bundle generateActivityBundle() {
        Bundle outState = new Bundle();
        outState.putString("test_key", "test_value");
        return outState;
    }


    @Test
    public void testOnCreateShouldCreateNewWithSavedStateIfPresenterDestroyed() throws Exception {
        BaseViewPresenter beforeViewPresenter = presenterLifecycle.onCreate(null);
        verify(mockPresenterFactory).createPresenter();
        verify(beforeViewPresenter).create(Mockito.<Bundle>eq(null));
        Bundle outState = generateActivityBundle();
        presenterLifecycle.onSavePresenterState(outState);
        presenterLifecycle.onPause(true);
        BaseViewPresenter afterViewPresenter = presenterLifecycle.onCreate(outState);
        verify(afterViewPresenter).create(eq(outState.getBundle(PresenterLifecycle.PRESENTER_DATA_KEY)));
        assertThat(beforeViewPresenter).isNotEqualTo(afterViewPresenter);
    }

    @Test
    public void testOnResumeShouldSetView() throws Exception {
        BaseViewPresenter viewPresenter = presenterLifecycle.onCreate(null);
        View mockView = mock(View.class);
        presenterLifecycle.onResume(mockView);
        verify(viewPresenter).setView(eq(mockView));
        verify(viewPresenter).onResume();
    }

    @Test
    public void testOnPauseShouldRemoveView() throws Exception {
        BaseViewPresenter viewPresenter = presenterLifecycle.onCreate(null);
        View mockView = mock(View.class);
        presenterLifecycle.onResume(mockView);
        presenterLifecycle.onPause(false);
        verify(viewPresenter).setView(eq(null));
    }

    @Test
    public void testOnPauseShouldDestroyPresenterIfNeeded() throws Exception {
        BaseViewPresenter viewPresenter = presenterLifecycle.onCreate(null);
        View mockView = mock(View.class);
        presenterLifecycle.onResume(mockView);
        presenterLifecycle.onPause(true);
        verify(viewPresenter).setView(eq(null));
        verify(presenterLifecycle).destroyPresenterIfNeeded(eq(true));
    }

    @Test
    public void testDestroyPresenterShouldRemovePresenterOutOfStorage() throws Exception {
        BaseViewPresenter viewPresenter = presenterLifecycle.onCreate(null);
        presenterLifecycle.destroyPresenterIfNeeded(true);
        verify(viewPresenter).onDestroy();
        assertThat(stubPresenterStorage.presenterMap).doesNotContainValue(viewPresenter);
        assertThat(stubPresenterStorage.presenterIdMap).doesNotContainKey(viewPresenter);
        assertThat(presenterLifecycle.getPresenter()).isNull();
    }

    @Test
    public void testSavedInstanceStateShouldContainsPresenterKey() throws Exception {
        BaseViewPresenter viewPresenter = presenterLifecycle.onCreate(null);
        Bundle outState = generateActivityBundle();
        presenterLifecycle.onSavePresenterState(outState);
        String presenterId = stubPresenterStorage.getIdOfPresenter(viewPresenter);
        assertThat(outState.getString(PresenterLifecycle.PRESENTER_ID_KEY)).isEqualTo(presenterId);
    }

    @Test
    public void testSavedInstanceStateShouldContainsPresenterState() throws Exception {
        BaseViewPresenter viewPresenter = presenterLifecycle.onCreate(null);
        Bundle presenterBundle = new Bundle();
        when(viewPresenter.saveState()).thenReturn(presenterBundle);
        Bundle outState = generateActivityBundle();
        presenterLifecycle.onSavePresenterState(outState);
        String presenterId = stubPresenterStorage.getIdOfPresenter(viewPresenter);
        assertThat(outState.getString(PresenterLifecycle.PRESENTER_ID_KEY)).isEqualTo(presenterId);
        assertThat(outState.getBundle(PresenterLifecycle.PRESENTER_DATA_KEY)).isEqualTo(presenterBundle);
    }
}