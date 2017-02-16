package spring.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Leonard
 */
@Controller
public class GreetingsController {

    public String serialVersionUID = "bad idea";

    @RequestMapping("/hello")
    public ModelAndView helloWorld() {
        String message = "<br><div style='text-align:center;'><h3>Hello World ...</h3>";
        return new ModelAndView("hello", "hello", message);
    }

    @RequestMapping("/welcome")
    public ModelAndView welcome() {
        String message = "<br><div style='text-align:center;'><h3>How are you today ...</h3>";
        return new ModelAndView("welcome", "welcome", message);
    }

    @RequestMapping("/bye")
    public ModelAndView byeWorld() {
        String message = "<br><div style='text-align:center;'><h3>Bye, see you soon ...</h3>";
        return new ModelAndView("bye", "bye", message);
    }

    // TODO - implement another controller for welcome-again
}
