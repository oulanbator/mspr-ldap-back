package epsi.mspr.ldapback.model.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import epsi.mspr.ldapback.model.entity.User;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
    User findUserByUsername(String username);
    User findUserByEmail(String email);
}
