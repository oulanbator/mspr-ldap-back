package tor.learning.jwtsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.model.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User getUser(Long userid) {
        return this.userRepository.findById(userid).get();
    }

    public User getUserByUsername(String username) {
        return this.userRepository.findUserByUsername(username);
    }

    public User getUserByEmail(String email) {
        return this.userRepository.findUserByEmail(email);
    }

    public User saveUser(User user) {
        return this.userRepository.save(user);
    }
}
