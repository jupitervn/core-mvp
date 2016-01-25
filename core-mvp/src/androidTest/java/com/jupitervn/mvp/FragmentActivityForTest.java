package com.jupitervn.mvp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/18/16.
 */
public class FragmentActivityForTest extends AppCompatActivity {

    private FrameLayout contentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentView = new FrameLayout(this);
        contentView.setId(android.R.id.custom);
        setContentView(contentView);
    }
}
