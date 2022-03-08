package epsi.mspr.ldapback.service;

import epsi.mspr.ldapback.model.entity.MailVerificationToken;
import epsi.mspr.ldapback.model.entity.User;
import epsi.mspr.ldapback.model.repository.MailVerificationTokenRepository;
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
    @Autowired
    private MailVerificationTokenRepository mailVerificationTokenRepository;

    private final String domain = "https://localhost:4200";


    public void sendBrowserCheckEmail(User user, String browser) {
        // Create token
        String token = UUID.randomUUID().toString();
        this.userService.createVerificationToken(token, user.getUsername(), browser);

        // Create URL to check token
        String confirmationUri = domain + "/verify-identity?token=" + token;

        // Create message elements
        String emailAddress = "victor.matheron@gmail.com";
        String subject = "MSPR - Connexion suspecte";
        String message = "Vous vous êtes connecté depuis un nouveau navigateur.\n" +
                "Veuillez confirmer votre identité en cliquant sur ce lien : \n" +
                confirmationUri;

        send(emailAddress, subject, message);
    }

    public void sendNewIpNotificationEmail(User user, String ip) {
        String emailAddress = "victor.matheron@gmail.com";
        String subject = "MSPR - Connexion inhabituelle";
        String message = "Vous vous êtes connecté depuis une nouvelle adresse IP : " + ip + "\n\n" +
                "Si ce n'était pas vous, vos identifiants sont probablement compromis.\n" +
                "Merci de contacter au plus vite les services informatiques, et modifier dès à présent votre mot de passe.";

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

    public void deleteToken(MailVerificationToken tokenEntity) {
        mailVerificationTokenRepository.delete(tokenEntity);
    }
}
