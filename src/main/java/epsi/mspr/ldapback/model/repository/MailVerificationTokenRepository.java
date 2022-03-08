package epsi.mspr.ldapback.model.repository;

import epsi.mspr.ldapback.model.entity.MailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailVerificationTokenRepository extends JpaRepository<MailVerificationToken, Long> {

    Optional<MailVerificationToken> findByToken(String token);

}
