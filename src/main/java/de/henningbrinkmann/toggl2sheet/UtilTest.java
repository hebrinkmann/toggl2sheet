package de.henningbrinkmann.toggl2sheet;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Test;
import sun.rmi.runtime.Log;

import static org.junit.Assert.*;

/**
 * Created by henning on 09.05.17.
 */
public class UtilTest {
    private static final Logger logger = Logger.getLogger(UtilTest.class);
    @Test
    public void getSollarbeitszeit() throws Exception {
        logger.info(Util.longToHourString(Util.getSollarbeitszeit(DateTime.now(), DateTime.now().plusDays(1))));
    }

}