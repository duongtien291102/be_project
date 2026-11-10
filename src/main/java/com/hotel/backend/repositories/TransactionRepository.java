package com.hotel.backend.repositories;

import com.hotel.backend.models.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findAllByOrderByCreatedAtInstantDesc();
    List<Transaction> findByYear(Integer year);
}
