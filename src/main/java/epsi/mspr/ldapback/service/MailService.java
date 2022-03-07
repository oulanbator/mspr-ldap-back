package epsi.mspr.ldapback.service;

import epsi.mspr.ldapback.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MailService {
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private UserService userService;

    public void sendBrowserCheckEmail(User user, String browser) {
        // Create token
        String token = UUID.randomUUID().toString();
        this.userService.createVerificationToken(token, user.getId(), browser);

        // Create URL to check token
        String confirmationUri = "https://localhost:4200/confirm-identity?token=" + token;

        // Create message elements
        String subject = "MSPR - Connexion inhabituelle";
        String message = "Vous vous êtes connecté depuis un nouveau navigateur.\n" +
                "Veuillez confirmer votre identité en cliquant sur ce lien : \n" +
                confirmationUri;
        String emailAddress = "victor.matheron@gmail.com";

        send(emailAddress, subject, message);
    }

    public void send(String emailAddress, String subject, String message) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(emailAddress);
        email.setSubject(subject);
        email.setText(message);
        // Send Message!
        this.emailSender.send(email);
        System.out.println("Email Sent!");
    }
}
