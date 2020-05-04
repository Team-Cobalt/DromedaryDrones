package com.dromedarydrones.mainapp;

import org.junit.Test;

public class TrialTest {

    @Test(expected = IllegalArgumentException.class)
    public void run() {
        new Trial(null);
    }
}