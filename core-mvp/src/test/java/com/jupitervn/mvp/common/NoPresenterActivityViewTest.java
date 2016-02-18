package com.jupitervn.mvp.common;

import com.jupiter.mvp.test.common.NoPresenterAppCompatActivityViewForTest;
import com.jupitervn.mvp.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/18/16.
 */
@RunWith(CustomRobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/test/AndroidManifest.xml")
public class NoPresenterActivityViewTest {
    @Test(expected = AssertionError.class)
    public void testShouldAlertErrorIfPresenterLifecycleNotSet() throws Exception {
        ActivityController<NoPresenterAppCompatActivityViewForTest> activityController = Robolectric
                .buildActivity(NoPresenterAppCompatActivityViewForTest.class);
        activityController.setup().get();
        activityController.pause().destroy();
    }


}
