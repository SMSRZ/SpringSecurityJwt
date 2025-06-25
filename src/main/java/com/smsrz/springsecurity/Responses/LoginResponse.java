package com.smsrz.springsecurity.Responses;

public record LoginResponse(String token,Long expiresIn) {
}
