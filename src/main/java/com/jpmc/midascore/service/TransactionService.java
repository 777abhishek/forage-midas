package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final UserRepository userRepository;
    private final IncentiveService incentiveService;

    public TransactionService(UserRepository userRepository, IncentiveService incentiveService) {
        this.userRepository = userRepository;
        this.incentiveService = incentiveService;
    }

    @Transactional
    public void processTransaction(Transaction transaction) {
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender == null || recipient == null) {
            logger.warn("Invalid transaction: sender or recipient not found");
            return;
        }

        float amount = transaction.getAmount();
        float incentive = incentiveService.getIncentive(transaction);

        // Deduct from sender
        sender.setBalance(sender.getBalance() - amount);
        
        // Add to recipient (amount + incentive)
        recipient.setBalance(recipient.getBalance() + amount + incentive);

        userRepository.save(sender);
        userRepository.save(recipient);

        logger.info("Processed transaction: sender={}, recipient={}, amount={}, incentive={}", 
            sender.getName(), recipient.getName(), amount, incentive);
    }
}
