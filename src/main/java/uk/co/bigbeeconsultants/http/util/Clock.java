package uk.co.bigbeeconsultants.http.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Provides a source of time information that is compatible with dependency injection and precise unit testing.
 */
public abstract class Clock {

    /**
     * Gets the current time from this clock.
     */
    public abstract DateTime now();

    /**
     * Creates a concrete Clock implementation based on the underlying system clock. It operates in any timezone.
     */
    public static class SystemClock extends Clock {
        private final DateTimeZone tz;

        public SystemClock(DateTimeZone tz) {
            this.tz = tz;
        }

        public DateTime now() {
            return new DateTime(System.currentTimeMillis(), tz);
        }
    }

    private static final Clock systemUTC = new SystemClock(DateTimeZone.UTC);
    private static final Clock systemPlatformDefault = new SystemClock(DateTimeZone.getDefault());

    /**
     * Gets a concrete Clock implementation based on the underlying system clock, operating only in UTC.
     */
    public static Clock systemUTC() {
        return systemUTC;
    }

    /**
     * Gets a concrete Clock implementation based on the underlying system clock, operating only in the
     * platform-default timezone.
     */
    public static Clock systemDefaultZone() {
        return systemPlatformDefault;
    }

    /**
     * Creates a concrete Clock implementation primarily for testing purposes.
     * Successive calls on the clock yield the sequence of instants specified to the constructor.
     */
    public static Clock fixed(final DateTime... requiredInstants) {
        if (requiredInstants.length <= 0)
            throw new IllegalArgumentException("Must provide at least one value.");

        return new Clock() {
            int i = -1;

            public DateTime now() {
                if (i < requiredInstants.length - 1)
                    i++;
                return requiredInstants[i];
            }
        };
    }
}
