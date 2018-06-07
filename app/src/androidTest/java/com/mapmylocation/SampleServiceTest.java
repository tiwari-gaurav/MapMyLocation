package com.mapmylocation;


import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mapmylocation.Service.BackgroundService;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)

public class SampleServiceTest {

    @Rule
    public ServiceTestRule myServiceRule = new ServiceTestRule();
    @Test
    public void testService() throws TimeoutException {
        myServiceRule.startService(new Intent(InstrumentationRegistry.getTargetContext(), BackgroundService.class));
    }
}
