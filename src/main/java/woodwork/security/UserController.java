package woodwork.security;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// this is used to not return html web pages, only text or json 
@RestController
// base path for endpoints in this controller(file)
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(Principal principal) {
        return ResponseEntity.ok("Success! You are authenticated as: " + principal.getName());
    }

    // for testing purposes(safety net testing)
    @GetMapping("/crash")
    public ResponseEntity<String> triggerCrash() {
        throw new IllegalArgumentException("This is a simulated business logic crash!");
    }
}
