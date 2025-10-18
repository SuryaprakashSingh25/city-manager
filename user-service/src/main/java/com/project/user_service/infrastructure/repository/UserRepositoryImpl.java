package com.project.user_service.infrastructure.repository;

import com.project.user_service.domain.model.Role;
import com.project.user_service.domain.model.User;
import com.project.user_service.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMongoRepository userMongoRepository;

    @Override
    public User save(User user) {
        UserDocument saved = userMongoRepository.save(UserMapper.toDocument(user));
        return UserMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userMongoRepository.findByEmail(email)
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findById(String id) {
        return userMongoRepository.findById(id)
                .map(UserMapper::toDomain);
    }


    @Override
    public Optional<User> findByVerificationToken(String token) {
        return userMongoRepository.findByVerificationToken(token)
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByResetToken(String token) {
        return userMongoRepository.findByResetToken(token)
                .map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByRole(Role role) {
        return userMongoRepository.existsByRole(role);
    }

    @Override
    public List<User> findAll(){
        return userMongoRepository.findAll()
                .stream().map(UserMapper::toDomain).toList();
    }

}
