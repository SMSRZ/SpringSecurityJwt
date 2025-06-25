package com.smsrz.springsecurity.Repository;

import com.smsrz.springsecurity.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User> findUserByEmail (String email);

    Optional<User> findByVerificationCode (String verificationCode);
}
