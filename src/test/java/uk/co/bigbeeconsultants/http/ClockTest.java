package uk.co.bigbeeconsultants.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClockTest {

    @Test
    public void testFixedClockShouldBeRepeatable() throws Exception {
        Instant instant = new Instant(123456789L);
        Clock f1 = Clock.fixed(new DateTime(instant, DateTimeZone.UTC));
        DateTimeZone tz8 = DateTimeZone.forOffsetHours(8);
        Clock f2 = Clock.fixed(new DateTime(instant, tz8));
        DateTime f1a = f1.now();
        Thread.sleep(100);

        DateTime f1b = f1.now();
        assertEquals(f1a, f1b);

        DateTime f2a = f2.now();
        assertEquals(f1a.toDateTime(tz8), f2a);
    }

    @Test
    public void testSequenceClockShouldBeRepeatable() throws Exception {
        DateTime i1 = new DateTime(123456700L);
        DateTime i2 = new DateTime(123456710L);
        DateTime i3 = new DateTime(123456720L);
        Clock c1 = Clock.fixed(i1, i2, i3);
        DateTime f1a = c1.now();
        assertEquals(i1, f1a);
        Thread.sleep(123);

        DateTime f1b = c1.now();
        assertEquals(i2, f1b);
        Thread.sleep(101);

        DateTime f1c = c1.now();
        assertEquals(i3, f1c);
        Thread.sleep(137);

        DateTime f1d = c1.now();
        assertEquals(i3, f1d);
    }

    @Test
    public void testSystemClockShouldRiseEachInvocation() throws Exception {
        Clock s1 = Clock.systemUTC();
        DateTime s1a = s1.now();
        Thread.sleep(101);
        DateTime s1b = s1.now();
        Thread.sleep(123);
        DateTime s1c = s1.now();
        assertTrue(s1a.getMillis() < s1b.getMillis());
        assertTrue(s1b.getMillis() < s1c.getMillis());
    }
}
