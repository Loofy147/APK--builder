package com.jomra.history;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.jomra.history", appContext.getPackageName());
    }

    @Test
    public void testAssetsExist() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String[] assets = appContext.getAssets().list("");
        boolean found = false;
        for (String asset : assets) {
            if (asset.equals("questions.json")) {
                found = true;
                break;
            }
        }
        assertTrue("questions.json should be in assets", found);
    }
}
