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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/api/")
public class AuthController {
    private static final Logger logger = LogManager.getLogger(AuthController.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    //this will get details from db based on authentic username/email through config file via spring security

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository= roleRepository;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("auth/signup")
    public ResponseEntity<String> registerUser(@RequestBody SignUpDTO signUpDTO){
        if(userRepository.existsByUsername(signUpDTO.getUsername()))
            return new ResponseEntity<>("Please pick different username", HttpStatus.BAD_REQUEST);

        if(userRepository.existsByEmail(signUpDTO.getEmail()))
            return new ResponseEntity<>("Please pick different emailID", HttpStatus.BAD_REQUEST);

        User user = new User();
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

    @PostMapping("auth/signin")
    public ResponseEntity<String> authenticateUser(@RequestBody LoginDTO loginDTO){

        /*here we are providing username and password from view through LoginDTO, and now we want to compare the details with db,
         For this to perform, we need to create a class and implement the same from UserDetailsService(a default interface provided
         by spring security). In the same class we need to override an important method called loadUserByUsername() But we can't
         access that directly as that is not the ordinary method.

        For this issue to resolve, we need to use UsernamePasswordAuthenticationToken class and supply username and password to it (entered from view).
        This class has multiple constructors, and internally it will call loadUserByUsername() */
       try{
           UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = //user entered details
                   new UsernamePasswordAuthenticationToken(loginDTO.getUsernameOrEmail(), loginDTO.getPassword()); //this calls loadUserByUserName() itself without us calling this
//            logger.info("hello1");
           //when username entered is ok (because in CustomUserDetailsService class we are checking if username/email enter is valid, and on that basis we have row from db here) now,

           Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
           //authenticationManager - data comes from db, usernamePasswordAuthenticationToken data from user via view
//           logger.info("hello2");
           //if valid data by user, then create a session variable
           //create a session variable
           SecurityContextHolder.getContext().setAuthentication(authentication);
//           logger.info("hello3");
           return new ResponseEntity<>("User signed in successfully",HttpStatus.OK);
       }catch (BadCredentialsException ex) {
           return new ResponseEntity<>("Invalid details!",HttpStatus.UNAUTHORIZED);
       }
    }
}