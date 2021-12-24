package tor.learning.jwtsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.model.repository.UserRepository;

import java.util.NoSuchElementException;

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

    public User saveUser(User user) {
        return this.userRepository.save(user);
    }
}
