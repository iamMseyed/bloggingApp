package com.myBlog.security;

import com.myBlog.entity.Role;
import com.myBlog.entity.User;
import com.myBlog.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

//to check data from db to perform authentication (login)
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; //to interact with db

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    } //constructor based injection

    //Remember this method will be called from AuthController class by UsernamePasswordAuthenticationToken() constructor, not by calling loadUserByUsername() itself!
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { //here we are supplying username or username as email
        User user = userRepository.findByUsernameOrEmail(username,username)//username or email
                .orElseThrow( ()->
                    new UsernameNotFoundException("Invalid username/email: "+username));

        //"user" will have username, and decoded password

        return new org.springframework.security.core.userdetails.User(user.getEmail(),user.getPassword(),
                mapRolesToAuthorities(user.getRoles()));
        // we have same Classes, thus using with package name. Remember there we are calling constructor of spring security User class

        /* User() constructor  takes 3 arguments of type String, String and GrantedAuthority type Collection.
         user.getRoles() will return Set, so creating a method to convert Set and supply to User class constructor (from spring security package)
        */

        /*this method is returning the details from db on the basis of user entered username/email. In other words a User object is created
        holding details from db based upon the authentic username/email provided.
         */
    }
    private Collection< ? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles){
        return roles.stream().
                map( role->
                        new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
        //GrantedAuthority is the interface in spring security which takes care of authorization based on roles
    }
}
