package com.myBlog.security;

import com.myBlog.entity.User;
import com.myBlog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/user/getUser")
public class getUserByAdminOnlyController {
    private final UserRepository userRepository;

    public getUserByAdminOnlyController(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @GetMapping("byEmail/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable("email") String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Provided email not found!",HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("byUsername/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable("username") String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Provided username not found!",HttpStatus.NOT_FOUND);
        }
    }
}
