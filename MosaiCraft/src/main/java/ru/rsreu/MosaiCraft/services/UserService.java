package ru.rsreu.MosaiCraft.services;



import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.MosaiCraft.entities.database.*;
import ru.rsreu.MosaiCraft.repo.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Autowired
    MosaicRepository mosaicRepository;
    @Autowired
    AlbumRepository albumRepository;
    @Autowired
    TemplateRepository templateRepository;

    public long countUsers() {
        return userRepository.count();
    }

    public long countMosaics() {
        return mosaicRepository.count();
    }

    public long countTemplates() {
        return templateRepository.count();
    }

    public long countAlbums() {
        return albumRepository.count();
    }

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
    public void addUserAlbum(Long userId, Album album) {
        User user = userRepository.findById(userId).orElseThrow();
        album.setUser(user);
        user.getAlbums().add(album);
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
    public Mosaic findMosaicById(long id) {
        return mosaicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Мозаика не найдена"));
    }

    @Transactional
    public void deleteMosaic(long userId, long mosaicId) {
        // Проверяем, что мозаика принадлежит пользователю
        Mosaic mosaic = findMosaicById(mosaicId);
        if (mosaic.getUser().getId() != userId) {
            throw new AccessDeniedException("Мозаика не принадлежит пользователю");
        }

        mosaicRepository.delete(mosaic);
    }

    @Transactional
    public boolean deleteMosaicById(Long mosaicId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        return user.getMosaics().removeIf(m -> m.getId().equals(mosaicId));
    }

    @Transactional
    public boolean changeRole(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(null);
        if (user.getRoles().contains(roleRepository.findById(2L).orElseThrow(null))) {
            user.getRoles().remove(roleRepository.findById(2L).orElseThrow(null));
            user.getRoles().add(roleRepository.findById(1L).orElseThrow(null));
            return false;
        } else {
            user.getRoles().remove(roleRepository.findById(1L).orElseThrow(null));
            user.getRoles().add(roleRepository.findById(2L).orElseThrow(null));
            return true;
        }
    }

    @Transactional
    public boolean deleteUserTemplateById(Long templateId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        return user.getTemplates().removeIf(t -> t.getId().equals(templateId));
    }

    public Album findAlbumById(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Альбом с ID " + id + " не найден"));
    }

    public void deleteAlbumById(Long id) {
        albumRepository.deleteById(id);
    }


}




