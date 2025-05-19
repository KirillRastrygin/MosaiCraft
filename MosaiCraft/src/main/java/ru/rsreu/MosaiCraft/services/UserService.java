package ru.rsreu.MosaiCraft.services;



import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.MosaiCraft.entities.database.*;
import ru.rsreu.MosaiCraft.repo.RoleRepository;
import ru.rsreu.MosaiCraft.repo.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return user;
    }

    public User findUserById(Long userId) {
        Optional<User> userFromDb = userRepository.findById(userId);
        return userFromDb.orElse(new User());
    }

    public List<User> allUsers() {
        return userRepository.findAll();
    }

    public boolean saveUser(User user) {
        User userFromDB = userRepository.findByUsername(user.getUsername());

        if (userFromDB != null) {
            return false;
        }

        user.setRoles(Collections.singleton(new Role(1L, "ROLE_USER")));
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }

    public boolean updateUser(User user) {
        User userFromDB = userRepository.findByUsername(user.getUsername());
        if (userFromDB == null) {
            return false;
        }
        userRepository.save(user);
        return true;
    }

    public boolean deleteUser(Long userId) {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public List<User> usergtList(Long idMin) {
        return em.createQuery("SELECT u FROM User u WHERE u.id > :paramId", User.class)
                .setParameter("paramId", idMin).getResultList();
    }

    @Transactional
    public void addUserMosaic(Long userId, Mosaic mosaic) {
        User user = userRepository.findById(userId).orElseThrow();
        mosaic.setUser(user);
        user.getMosaics().add(mosaic);
    }

    @Transactional
    public void addUserTemplate(Long userId, Template template) {
        User user = userRepository.findById(userId).orElseThrow();
        template.setUser(user);
        user.getTemplates().add(template);
    }

    @Transactional
    // Методы для работы с мозаиками и альбомами
    public List<Mosaic> getUserMosaics(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? user.getMosaics() : null;
    }

    @Transactional
    public List<Album> getUserAlbums(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? user.getAlbums() : null;
    }

    @Transactional(readOnly = true)
    public User findByIdWithMosaicsAndAlbums(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.getMosaics().size(); // Инициализируем ленивая загрузка
            user.getAlbums().size();
        }
        return user;
    }

    @Transactional
    public boolean deleteMosaicById(Long mosaicId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        return user.getMosaics().removeIf(m -> m.getId().equals(mosaicId));
    }

    @Transactional
    public boolean deleteUserTemplateById(Long templateId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        return user.getTemplates().removeIf(t -> t.getId().equals(templateId));
    }

    @Transactional
    public boolean deleteAlbumById(Long albumId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        return user.getAlbums().removeIf(a -> a.getId().equals(albumId));
    }

    @Transactional
    public boolean createAlbum(Long userId, Album album) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        album.setUser(user);
        user.getAlbums().add(album);
        return true;
    }


}




