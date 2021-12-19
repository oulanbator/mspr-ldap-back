package tor.learning.jwtsecurity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
public class HelloResource {
    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }
}
