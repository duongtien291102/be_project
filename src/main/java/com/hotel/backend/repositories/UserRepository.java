package com.hotel.backend.repositories;

import com.hotel.backend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByCccd(String cccd);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
