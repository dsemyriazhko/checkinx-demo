package com.checkinx;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.checkinx.demo2.Application;

@Test
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public abstract class AbstractDbTest extends AbstractTestNGSpringContextTests {
}
