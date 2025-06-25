package com.smsrz.springsecurity.Service;


import com.smsrz.springsecurity.Model.User;
import com.smsrz.springsecurity.Repository.UserRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepo userRepo;
    public UserService(UserRepo userRepo,EmailService emailService ){
        this.userRepo=userRepo;
    }
    public List<User> getAllUsers(){
        return userRepo.findAll();
    }
}
