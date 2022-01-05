package tor.learning.jwtsecurity.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.model.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    VerificationToken findByUser(User user);
}
