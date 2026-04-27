package backend.hobbiebackend.utility;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    
    private final JWTUtility jwtUtility;
    
    public JwtUtil(JWTUtility jwtUtility) {
        this.jwtUtility = jwtUtility;
    }
    
    public String generateToken(String username) {
        // Create a simple UserDetails object for JWT generation
        UserDetails userDetails = User.withUsername(username)
            .password("")
            .roles("USER")
            .build();
        
        return jwtUtility.generateToken(userDetails);
    }
}
