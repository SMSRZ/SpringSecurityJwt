package com.smsrz.springsecurity.Controller;


import com.smsrz.springsecurity.DTO.LoginUserDto;
import com.smsrz.springsecurity.DTO.RegisterUserDto;
import com.smsrz.springsecurity.DTO.VerifyUserDto;
import com.smsrz.springsecurity.Model.User;
import com.smsrz.springsecurity.Responses.LoginResponse;
import com.smsrz.springsecurity.Service.AuthencationService;
import com.smsrz.springsecurity.Service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthController {

    private final JwtService jwtService;
    private final AuthencationService authencationService;

    public AuthController(JwtService jwtService, AuthencationService authencationService) {
        this.jwtService = jwtService;
        this.authencationService = authencationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto){
        User registeredUser = authencationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto){
        User authenticatedUser = authencationService.Authenticate(loginUserDto);
        String JwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse response = new LoginResponse(JwtToken, jwtService.getJwtExpirationTime());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyUserDto verifyUserDto){
         try{
             authencationService.verifyUser(verifyUserDto);
             return ResponseEntity.ok("Account verified Successfully");
         } catch (RuntimeException e) {
             return ResponseEntity.badRequest().body(e.getMessage());
         }
    }
    @PostMapping("/resend")
    public ResponseEntity<?> resendCode(@RequestParam("email") String email){
        try{
            authencationService.resendVerificationCode(email);
            return ResponseEntity.ok("Code send");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
 }
