package spring.controllers;

import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import spring.services.IHello;

/**
 *
 * @author newlife
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:hello-context.xml")
public class HelloControllerTest {

    private static final Logger LOG = Logger.getLogger(HelloControllerTest.class.getName());

    @Autowired
    IHello helloService;

    @Test
    public void dummyUnitTest() {
        LOG.info("---------------------------");
        LOG.info("Running #dummyUnitTest() unit test ...");
        LOG.info("---------------------------");
    }

    @Test
    public void dummyServiceUnitTest() {
        LOG.info("---------------------------");
        LOG.info("Mock some repository ...");
        LOG.info("Running #dummyServiceUnitTest() unit test ...");
        String message = helloService.helloAction();
        assertEquals(message, "Hello World");
        LOG.info("---------------------------");
    }
}
