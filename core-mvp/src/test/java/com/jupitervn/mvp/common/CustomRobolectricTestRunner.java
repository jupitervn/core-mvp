package com.jupitervn.mvp.common;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/29/16.
 */
public class CustomRobolectricTestRunner extends RobolectricGradleTestRunner {

    public CustomRobolectricTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        AndroidManifest appManifest = super.getAppManifest(config);
        FileFsFile configFileManifest = FileFsFile.from(config.manifest());
        if (configFileManifest.exists()) {
            appManifest = new AndroidManifest(configFileManifest, appManifest.getResDirectory(), appManifest.getAssetsDirectory(), appManifest.getPackageName());
        }
        return appManifest;
    }
}
