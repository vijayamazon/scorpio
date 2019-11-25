package com.ibm.icu.text;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@RunWith(JUnit4.class)
public class ReportEncodingTest {
    @Test
    public void check() throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream("/tmp/report"));
        CharsetDetector detector = new CharsetDetector();
        detector.setText(inputStream);
        detector.detect();
        inputStream.close();
        System.out.println(detector.detect().getName());
    }
}
