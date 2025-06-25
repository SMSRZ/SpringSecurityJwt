package com.smsrz.springsecurity.Service;


import com.smsrz.springsecurity.DTO.LoginUserDto;
import com.smsrz.springsecurity.DTO.RegisterUserDto;
import com.smsrz.springsecurity.DTO.VerifyUserDto;
import com.smsrz.springsecurity.Model.User;
import com.smsrz.springsecurity.Repository.UserRepo;
import jakarta.mail.MessagingException;
import org.springframework.mail.MailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthencationService {

    private final UserRepo  userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthencationService(UserRepo userRepo, AuthenticationManager authenticationManager, EmailService emailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public User signup(RegisterUserDto userDtoInput) {
        User user = new User(userDtoInput.username(), passwordEncoder.encode(userDtoInput.password()), userDtoInput.email());
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);

        return userRepo.save(user);
    }



    public User Authenticate(LoginUserDto  loginUserDto) {
          User user = userRepo
                  .findUserByEmail(loginUserDto.email())
                  .orElseThrow(()->new UsernameNotFoundException("User not found"));

          if (!user.isEnabled()){
              throw new RuntimeException("Account not verified");
          }

          authenticationManager.authenticate(
                  new UsernamePasswordAuthenticationToken(
                          loginUserDto.email(),
                          loginUserDto.password()
                  )
          );
          return user;
    }

    public void verifyUser(VerifyUserDto userDto){
        Optional<User> optUser = userRepo.findUserByEmail(userDto.email());
        System.out.println(userDto.email());
        if (optUser.isPresent()){
            User user = optUser.get();
            if(user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())){
                throw new RuntimeException("Verification code expired");
            }
             if (user.getVerificationCode().equals(userDto.verificationCode())){
                 user.setEnabled(true);
                 user.setVerificationCode(null);
                 user.setVerificationCodeExpiresAt(null);
                 userRepo.save(user);
             }else{
                 throw new RuntimeException("Invalid verification code");
             }
        }else{
            throw new UsernameNotFoundException("User not found");
        }
    }

    public void resendVerificationCode(String email){
        Optional<User> optUser = userRepo.findUserByEmail(email);
        if (optUser.isPresent()){
            User user = optUser.get();
            if(user.isEnabled()){
                throw new RuntimeException("Account is already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            sendVerificationEmail(user);
            userRepo.save(user);
        }else{
            throw new UsernameNotFoundException("User not found");
        }
    }
    public void sendVerificationEmail(User user){
        String subject = "Verification Code";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try{
            emailService.sendVerificationMail(user.getEmail(), subject,htmlMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateVerificationCode(){
        Random random = new Random();
        int code = random.nextInt(900000);
        return String.valueOf(code);
    }
}
