package com.myBlog.controller;

import com.myBlog.entity.Role;
import com.myBlog.entity.User;
import com.myBlog.payload.LoginDTO;
import com.myBlog.payload.SignUpDTO;
import com.myBlog.repository.RoleRepository;
import com.myBlog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final User user;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    //this will get details from db based on authentic username/email through config file via spring security

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            AuthenticationManager authenticationManager,
            User user) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository= roleRepository;
        this.authenticationManager = authenticationManager;
        this.user= user;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody SignUpDTO signUpDTO){
        if(userRepository.existsByUsername(signUpDTO.getUsername()))
            return new ResponseEntity<>("Please pick different username", HttpStatus.BAD_REQUEST);

        if(userRepository.existsByEmail(signUpDTO.getEmail()))
            return new ResponseEntity<>("Please pick different emailID", HttpStatus.BAD_REQUEST);

//        User user = new User();
        user.setName(signUpDTO.getName());
        user.setUsername(signUpDTO.getUsername());
        user.setEmail(signUpDTO.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDTO.getPassword()));

        if(roleRepository.findByName(signUpDTO.getRoleType()).isPresent()){
            Role roles = roleRepository.findByName(signUpDTO.getRoleType()).get();
            //before saving we need to add role too, but roles in role entity has the set return type, so we need to convert to set first
           //approach 1:
                Set<Role> convertRoleToSet= new HashSet<>();
                convertRoleToSet.add(roles);
                user.setRoles(convertRoleToSet);

            //approach 2:
             //user.setRoles(Collections.singleton(roles));
        }

        userRepository.save(user);
        return new ResponseEntity<>("User registered successfully!",HttpStatus.OK);
    }

    @PostMapping("/signin")
    public ResponseEntity<String> authenticateUser(@RequestBody LoginDTO loginDTO){

        /*here we are providing username and password from view through LoginDTO, and now we want to compare the details with db,
         For this to perform, we need to create a class and implement the same from UserDetailsService(a default interface provided
         by spring security people). In the same class we need to override an important method called loadUserByUsername() But we can't
         access that directly as that is not the ordinary method.

        For this issue to resolve, we need to use UsernamePasswordAuthenticationToken class and supply username and password to it (entered from view).
        This class has multiple constructors, and internally it will call loadUserByUsername() */

        System.err.println("1");
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = //user entered details
                new UsernamePasswordAuthenticationToken(loginDTO.getUsernameOrEmail(), loginDTO.getPassword());
        System.err.println("2");

        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        System.err.println("3");
        //authenticationManager - data comes from db, usernamePasswordAuthenticationToken data from user via view
        //if valid data by user, then create a session variable
        //create a session variable
         SecurityContextHolder.getContext().setAuthentication(authentication);
        System.err.println("4");
        return new ResponseEntity<>("User signed in successfully",HttpStatus.OK);
    }
}