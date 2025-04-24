package ru.rsreu.MosaiCraft.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    @Column
    @Size(min = 6, max = 50, message = "Пароль должен быть от 6 до 50 символов")
    @NotBlank(message = "Пароль обязателен")
    private String password;

    @Column
    private String email;

    @Transient
    private String passwordConfirm;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @CreationTimestamp  // Автоматически при создании
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp  // Автоматически при обновлении
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Template> templates;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Album> albums;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mosaic> mosaics;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = new HashSet<>();
        this.templates = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.mosaics = new ArrayList<>();
    }

    public void addMosaic(Mosaic mosaic) {
        mosaic.setUser(this);
        mosaics.add(mosaic);
    }

    public void removeMosaic(Mosaic mosaic) {
        mosaics.remove(mosaic);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void addTemplate(Template template) {
        template.setUser(this);
        templates.add(template);
    }

    public void removeTemplate(Template template) {
        templates.remove(template);
    }

    public void addAlbum(Album album) {
        album.setUser(this);
        albums.add(album);
    }

    public void removeAlbum(Album album) {
        albums.remove(album);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
