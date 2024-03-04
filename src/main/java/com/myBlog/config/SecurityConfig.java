package com.myBlog.config;

import com.myBlog.security.CustomUserDetailsService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//spring security reads securityConfig file

@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true) //to use preAuthorize annotation
public class SecurityConfig extends WebSecurityConfigurerAdapter{

    private final CustomUserDetailsService customUserDetailsService;
    //this object has all the details of valid username/password
    public SecurityConfig(CustomUserDetailsService customUserDetailsService){
        this.customUserDetailsService= customUserDetailsService;
    }//constructor injection
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    @Bean //we can use bean only in Configuration classes with @Configuration annotation
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    } //create an object of BCry....

    @Override
    protected  void configure(HttpSecurity http) throws Exception{
        http
                .csrf().disable()
                .authorizeHttpRequests()
//                .antMatchers(HttpMethod.POST,"/api/auth/signup").hasRole("SUPER") //remember hasRole() will add ROLE_ by default. So, it will check for ROLE_SUPER in db
                .antMatchers(HttpMethod.POST,"/api/auth/**").permitAll()
                .antMatchers(HttpMethod.POST,"/api/posts/**").hasAuthority("USER")
                .antMatchers(HttpMethod.GET,"/api/posts/**").hasAnyAuthority("USER","ADMIN")
                .antMatchers(HttpMethod.DELETE,"/api/posts/**").hasAnyAuthority("USER","ADMIN")
                .antMatchers(HttpMethod.PUT,"/api/posts/**").hasAnyAuthority("USER","ADMIN")
                .antMatchers(HttpMethod.GET,"/user/getUser/**").hasAuthority("ADMIN") //only ADMIN can have access to these endpoints.
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic();
    }

//    Embedded in-memory authentication method responsible to create object in which username and password is to be stored
//    commenting the code just not to authenticate every time while interacting via view (postman here)
//    @Override
//    @Bean
//    protected UserDetailsService userDetailsService(){
//        UserDetails user1 = User.builder().username("${USER1}").password(passwordEncoder().encode("${PWD1}")).roles("${ROLE1}").build();
//        UserDetails user2 = User.builder().username("${USER2}").password(passwordEncoder().encode("${PWD2}")).roles("${ROLE2}").build();
//        return new InMemoryUserDetailsManager(user1,user2); //storing users in inMemory object not in db
//    }

    //This will authenticate the details entered with the data it got from db corresponding to username/email
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
        //customUserDetailsService will have data from db, based on username/email, and we need to set here, so spring security reads the file and has access to it.
        
    }
}