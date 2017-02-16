package spring.services;

import java.util.logging.Logger;
import org.springframework.stereotype.Component;

/**
 *
 * @author newlife
 */
@Component
public class HelloService implements IHello {

    private static final Logger LOG = Logger.getLogger(HelloService.class.getName());

    @Override
    public String helloAction() {
        System.out.println("I'm the helloAction() method ...");
        // FIXME - use the logger

        // some repository can be used here
        return "Hello World";
    }

}
