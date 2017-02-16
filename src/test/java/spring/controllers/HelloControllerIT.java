package spring.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author newlife
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:hello-context.xml")
@WebAppConfiguration
public class HelloControllerIT {

    private static final Logger LOG = Logger.getLogger(HelloControllerIT.class.getName());

    @Autowired
    WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void dummyDBSmokeTest() throws ClassNotFoundException, SQLException {
        LOG.info("---------------------------");
        LOG.info("Running #dummyDBSmokeTest() smoke test ...");
        LOG.info("---------------------------");

        Class.forName("org.postgresql.Driver");

        LOG.info("PostgreSQL JDBC Driver Was Successfully Registered!");

        Connection defaultConnection;
        defaultConnection = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");

        Connection oakConnection;
        oakConnection = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/oakdb", "oak", "oakpwd");

        Assert.assertNotNull(defaultConnection);
        Assert.assertNotNull(oakConnection);

        LOG.info("Connection was successfully acquired!");
    }

    @Test
    public void dummyIntegrationTest() {
        //FIXME
        LOG.info("---------------------------");
        LOG.info("Running #dummyIntegrationTest() integration test ...");
        LOG.info("---------------------------");

        int x = -1;
        if (x < 0) {
            new IllegalArgumentException("x must be nonnegative");
        }
    }

    @Test
    public void dummyHelloWorldIntegrationTest() throws Exception {
        LOG.info("---------------------------");
        LOG.info("Running #dummyHelloWorldIntegrationTest() integration test ...");
        LOG.info("---------------------------");
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk());
    }

    @Test
    public void dummyByeWorldIntegrationTest() throws Exception {
        LOG.info("---------------------------");
        LOG.info("Running #dummyByeWorldIntegrationTest() integration test ...");
        LOG.info("---------------------------");
        mockMvc.perform(get("/bye"))
                .andExpect(status().isOk());
    }

    // add more integration tests
}
