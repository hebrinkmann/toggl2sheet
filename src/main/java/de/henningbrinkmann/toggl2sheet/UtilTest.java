package de.henningbrinkmann.toggl2sheet;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sun.rmi.runtime.Log;

import static org.junit.Assert.*;

/**
 * Created by henning on 09.05.17.
 */
@RunWith(SpringJUnit4ClassRunner.class)

public class UtilTest {
    private static final Logger logger = Logger.getLogger(UtilTest.class);

    private Util util;

    public UtilTest(Util util) {
        this.util = util;
    }

    @Test
    public void getSollarbeitszeit() throws Exception {
        logger.info(util.longToHourString(util.getSollarbeitszeit(DateTime.now(), DateTime.now().plusDays(1))));
    }

}