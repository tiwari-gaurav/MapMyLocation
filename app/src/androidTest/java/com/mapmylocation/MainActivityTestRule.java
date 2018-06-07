package com.mapmylocation;

import android.app.Activity;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

public class MainActivityTestRule<A extends Activity> extends ActivityTestRule<A> {

    public MainActivityTestRule(Class<A> activityClass) {
        super(activityClass);
    }

    @Override
    protected Intent getActivityIntent() {
        return super.getActivityIntent();
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
    }

    @Override
    protected void afterActivityLaunched() {
        super.afterActivityLaunched();
    }
}
