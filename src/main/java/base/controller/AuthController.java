package base.controller;

import base.security.BaseUserDetailsService;
import base.security.jwt.JwtAuthenticationRequest;
import base.security.jwt.JwtTokenUtil;
import base.user.User;
import base.user.UserRepository;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class AuthController {

    @Value("${jwt.requestHeader}")
    private String tokenHeader;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private BaseUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "${jwt.route.authentication.path}", method = RequestMethod.POST)
    public ResponseEntity createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest, Device device) throws AuthenticationException {

        // Perform the security
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password post-security so we can generate token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        boolean isVerified = userDetailsService.checkUserIsVerified(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails, device);

        // Return the token
        return isVerified ? ResponseEntity.ok(token) : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @RequestMapping(value = "${jwt.route.authentication.refresh}", method = RequestMethod.GET)
    public ResponseEntity refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        String username = jwtTokenUtil.getUsernameFromToken(token);

        return ResponseEntity.ok(jwtTokenUtil.refreshToken(token));
    }

    @RequestMapping(value = "/verify/{verificationToken}", method = RequestMethod.GET)
    public void verifyUser(@PathVariable String verificationToken, HttpServletResponse response) throws IOException {
        User user = userRepository.findByVerificationToken(verificationToken);
        if(user != null) {
            user.setIsVerified(true);
            userRepository.save(user);
            response.sendRedirect("/signIn.html");
        }
        else {
            response.sendRedirect("/signUp.html");
        }
    }

}
