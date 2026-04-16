package woodwork.security;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import woodwork.email.EmailService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtGenerator jwtGenerator;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtGenerator jwtGenerator, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
        this.emailService = emailService;
    }

    public String register(RegisterDto dto) {
        // check username and email(taken or not)
        if (userRepository.existsByUsername(dto.getUsername())) {
            return "Error: Username is already taken.";
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            return "Error: Email is already registered.";
        }

        // create user entity
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        
        // hash the password before setting it
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        UserProfile profile = new UserProfile();
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        user.setProfile(profile);
        profile.setUser(user);

        // find(or get) the default role
        Role userRole = roleRepository.findByName("ROLE_CUSTOMER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("ROLE_CUSTOMER");
            return roleRepository.save(newRole);
        });

        // link role and save the user
        user.setRoles(Collections.singletonList(userRole));
        userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), dto.getFirstName());

        return "User registered successfully!";
    }

    public String login(LoginDto dto) {
        // ask database if user exists
        Optional<User> userOptional = userRepository.findByUsername(dto.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // compare the raw password with the hased database password
            if (passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                
                // if matched, return the token
                return jwtGenerator.generateToken(user.getUsername());
            }
        }
        // if the user doesn't exist or the password fails return an error
        return "Error: Invalid username or password.";
    }
}
