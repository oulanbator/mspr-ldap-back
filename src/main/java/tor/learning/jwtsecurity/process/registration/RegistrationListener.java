package tor.learning.jwtsecurity.process.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.service.UserService;

import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    @Autowired
    private UserService userService;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.front-end.domain}")
    private String frontDomain;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        this.userService.createVerificationToken(user, token);

        String recipientAddress = user.getEmail();
        String subject = "MyApp - Registration Confirmation";
        String confirmationUri = event.getAppUrl() + "/confirm-registration?token=" + token;
        String message = "Please verify your email adress.";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + "\r\n" + frontDomain + confirmationUri);
        mailSender.send(email);
    }
}
