package com.melodyguard.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.melodyguard.api.dto.auth.LoginRequest;
import com.melodyguard.api.entity.Role;
import com.melodyguard.api.entity.User;
import com.melodyguard.api.exception.InvalidCredentioalsException;
import com.melodyguard.api.repository.UserRepository;
import com.melodyguard.api.security.JWTUtil;

import java.util.Collections;
import java.util.Map;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private JWTUtil jwtUtil;
    @Autowired private AuthenticationManager authManager;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Map<String, Object> registerHandler(@Valid @RequestBody User user){
        String encodedPass = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPass);
        user.setRole(Role.USER);
        user = userRepo.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return Collections.singletonMap("jwt-token", token);
    }

    @PostMapping("/login")
    public Map<String, Object> loginHandler(@Valid @RequestBody LoginRequest body){
        try {
            UsernamePasswordAuthenticationToken authInputToken =
                    new UsernamePasswordAuthenticationToken(body.getEmail(), body.getPassword());

            User userByEmail = userRepo.findByEmail(body.getEmail()).get();

            
            if (passwordEncoder.matches(body.getPassword(), userByEmail.getPassword())) {
                authManager.authenticate(authInputToken);
                String token = jwtUtil.generateToken(body.getEmail(), userByEmail.getRole());
                return Collections.singletonMap("jwt-token", token);
            } else throw new InvalidCredentioalsException("Invalid credentioals, invalid password.");



        }catch (AuthenticationException authExc){
            throw new RuntimeException("Invalid Login Credentials");
        }
    }


}