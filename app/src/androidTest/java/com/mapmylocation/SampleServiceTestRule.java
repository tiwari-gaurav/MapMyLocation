package com.mapmylocation;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.test.rule.ServiceTestRule;
import android.util.Log;

import java.util.concurrent.TimeoutException;

public class SampleServiceTestRule extends ServiceTestRule {

    @Override
    public void startService(@NonNull Intent intent) throws TimeoutException {
        super.startService(intent);
    }

    @Override
    protected void beforeService() {
        Log.e("SampleServiceTestRule", "work before the service starts");
        super.beforeService();
    }

    @Override
    protected void afterService() {
        super.afterService();
    }


}
