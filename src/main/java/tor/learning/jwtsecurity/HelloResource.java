package tor.learning.jwtsecurity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tor.learning.jwtsecurity.model.http.MessageResponse;

@RestController
@CrossOrigin
public class HelloResource {

    @GetMapping("/hello")
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok(new MessageResponse("HelloWorld"));
    }

}
