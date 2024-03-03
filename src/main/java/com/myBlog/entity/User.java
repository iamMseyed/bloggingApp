package com.myBlog.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Set;
@Component
@Data
@Entity
@Table(name="users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"email"})
})

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String username;
    private String email;
    private String password;

    @ManyToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL) //many -> user, Many-> role
    @JoinTable(name="user_roles",
               joinColumns = @JoinColumn(name="user_id", referencedColumnName = "id"),
                inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles;

}