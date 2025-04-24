package ru.rsreu.MosaiCraft.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "roles")
public class Role implements GrantedAuthority {
    @Id
    private long id;
    private String name;
    @Transient
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    @Override
    public String getAuthority() {
        return getName();
    }

    public Role(Long id) {
        this.id = id;
        this.users = new HashSet<>();
    }

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
        this.users = new HashSet<>();
    }

    public void addUser(User user) {
        this.users.add(user);
    }

}
