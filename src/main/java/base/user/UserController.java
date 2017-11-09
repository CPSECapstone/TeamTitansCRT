package base.user;


import base.security.CurrentUser;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@RestController
@RequestMapping("/user")
public class UserController  {

    @Autowired
    private UserRepository userRepository;

    // Return only logged in user
    @GetMapping
    public UserDetails getCurrentUser(@CurrentUser UserDetails currentUser) {
        return currentUser;
    }

    @RequestMapping("/all")
    public List<User> getUsers(@CurrentUser UserDetails currentUser) {
        ArrayList<User> users = new ArrayList<>();
        if (currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            userRepository.findAll().forEach(users::add);
        }
        return users;
    }

    private boolean runningOnLocalHost() {
        InetAddress ip;
        try {

            ip = InetAddress.getLocalHost();
            System.out.println(ip.getHostAddress());
            System.out.println(ip.getHostName());


            System.out.println(System.getenv().toString());

            System.out.println(System.getProperties().stringPropertyNames());
            return ip.isSiteLocalAddress();

        } catch (UnknownHostException e) {
            e.printStackTrace();

        }
        return false;
    }

    private void sendEmail(String recipient, String verificationToken, String verificationLink) {
        // Recipient's email ID needs to be mentioned.
        String to = recipient;

        // Sender's email ID needs to be mentioned
        String from = "TheFellowshipMeter@gmail.com";
        final String username = from;//change accordingly
        final String password = "cpe308-309";//change accordingly

        // Assuming you are sending email through relay.jangosmtp.net
        String host = "smtp.gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject("Verify your Fellowship account!");

            message.setText("Hi! Thank you for signing up with The Fellowship. Please click" +
                            " on the link to activate your account: " + verificationLink
                    + verificationToken);

            // Send message
            Transport.send(message);

            System.out.println("Sent message successfully....");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping
    public User create(@Valid @RequestBody User reqUser, HttpServletRequest request) {
        User user = new User();
        user.setEmail(reqUser.getEmail());
        user.setName(reqUser.getName());
        user.setPassword(new BCryptPasswordEncoder().encode(reqUser.getPassword()));
        user.setRole(reqUser.getRole());
        user.setVerificationToken(java.util.UUID.randomUUID().toString());

        System.out.println(request.getRequestURL());
        System.out.println(request.getLocalName());
        System.out.println(request.getServerName());
        //send email websites/verify

        String verificationLink = "";
        if(request.getServerName().equals("localhost")) {
            verificationLink = "http://localhost:8080/verify/";
        }
        else {
            verificationLink = "http://cp-fellowship.herokuapp.com/verify/";
        }

        try {
            sendEmail(user.getEmail(), user.getVerificationToken(), verificationLink);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return userRepository.save(user);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id) {
        // ADMIN Route
        userRepository.delete(id);
    }

    @PutMapping("{id}")
    public User update(@PathVariable Long id, @RequestBody User reqUser) {
        User user = userRepository.findOne(id);
        if (user == null) {
            return null;
        } else {
            user.setEmail(reqUser.getEmail());
            user.setName(reqUser.getName());
            user.setEmail(reqUser.getEmail());
            user.setPassword(new BCryptPasswordEncoder().encode(reqUser.getPassword()));
            user.setRole(reqUser.getRole());
            return userRepository.save(user);
        }
    }
}
