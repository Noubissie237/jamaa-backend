package com.jamaa_bank.service_transactions.services;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.ResolvedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jamaa_bank.service_transactions.event.TransactionEvent;
import com.jamaa_bank.service_transactions.utils.Util;


@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private static final String TRANSACTION_STREAM = "jamaaTransactionStream";

    private final EventStoreDBClient eventStoreDBClient;
    private final ObjectMapper objectMapper;
    private final Util util;

    public TransactionService(EventStoreDBClient eventStoreDBClient, Util util) {
        this.util = util;
        this.eventStoreDBClient = eventStoreDBClient;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        logger.info("TransactionService initialized with EventStoreDBClient");
    }

    public CompletableFuture<String> saveTransaction(TransactionEvent event) {
        logger.info("Saving transaction: type={}, sender={}, receiver={}, amount={}, bankId={}",
                event.getTransactionType(), event.getIdAccountSender(),
                event.getIdAccountReceiver(), event.getAmount(), event.getBankId());

        TransactionEvent transactionEvent = new TransactionEvent();
        transactionEvent.setDateEvent(LocalDateTime.now());
        transactionEvent.setIdAccountSender(event.getIdAccountSender());
        transactionEvent.setIdAccountReceiver(event.getIdAccountReceiver());
        transactionEvent.setAmount(event.getAmount());
        transactionEvent.setStatus(event.getStatus());
        transactionEvent.setTransactionType(event.getTransactionType());
        transactionEvent.setCreatedAt(event.getCreatedAt());

        String transactionId = UUID.randomUUID().toString(); 
        return writeEvent(TRANSACTION_STREAM, transactionEvent.getTransactionType().toString(), transactionId, transactionEvent).thenApply(v -> transactionId);
    }


    private CompletableFuture<Void> writeEvent(String streamName, String type, String transactionId, TransactionEvent event) {
        try {
            // Inclure transactionId dans les métadonnées ou dans le payload
            Map<String, Object> eventMap = objectMapper.convertValue(
                event, 
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
            );

            eventMap.put("transactionId", transactionId);

            String data = objectMapper.writeValueAsString(eventMap);
            EventData eventData = EventData.builderAsJson(type, data).build();

            logger.debug("Writing event to stream {}: {}", streamName, type);
            return eventStoreDBClient.appendToStream(streamName, eventData)
                    .thenAccept(result -> logger.info("Event successfully appended to stream {}, revision: {}",
                            streamName, result.getNextExpectedRevision()))
                    .exceptionally(ex -> {
                        logger.error("Failed to write event to stream {}: {}", streamName, ex.getMessage(), ex);
                        throw new RuntimeException("Failed to write event", ex);
                    });
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(new RuntimeException("Failed to serialize event", e));
        }
    }


    public List<TransactionEvent> getAllTransactions() {
        logger.info("Fetching all transactions");
        try {
            List<ResolvedEvent> events = eventStoreDBClient.readStream(TRANSACTION_STREAM, ReadStreamOptions.get())
                    .get().getEvents();

            logger.debug("Retrieved {} events from stream {}", events.size(), TRANSACTION_STREAM);
            return events.stream()
                    .map(this::deserializeEvent)
                    .filter(event -> event != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to fetch transactions: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<TransactionEvent> getTransactionByIdAccount(Long idAccount) {
        logger.info("Fetching transactions for account ID: {}", idAccount);
        try {
            List<ResolvedEvent> events = eventStoreDBClient.readStream(TRANSACTION_STREAM, ReadStreamOptions.get())
                    .get().getEvents();

            logger.debug("Retrieved {} events from stream {}", events.size(), TRANSACTION_STREAM);
            return events.stream()
                    .map(this::deserializeEvent)
                    .filter(event -> event != null &&
                            (idAccount.equals(event.getIdAccountSender()) ||
                                    idAccount.equals(event.getIdAccountReceiver())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to fetch transactions for account ID {}: {}", idAccount, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<TransactionEvent> getTransactionsByUserId(Long userId) {
        logger.info("Fetching transactions for user ID: {}", userId);
        List<TransactionEvent> allTransactions = new ArrayList<>();

        try {
            List<Long> accountIds = util.getAccountIdsByUserId(userId);

            for (Long accountId : accountIds) {
                List<TransactionEvent> transactions = getTransactionByIdAccount(accountId);
                allTransactions.addAll(transactions);
            }

            logger.info("Found total {} transactions for user ID: {}", allTransactions.size(), userId);
        } catch (Exception e) {
            logger.error("Failed to fetch transactions for user ID {}: {}", userId, e.getMessage(), e);
        }

        return allTransactions;
    }

    private TransactionEvent deserializeEvent(ResolvedEvent resolvedEvent) {
        try {
            String jsonData = new String(resolvedEvent.getEvent().getEventData(), StandardCharsets.UTF_8);

            // Nettoyer les données JSON si nécessaire
            if (jsonData.startsWith("\"") && jsonData.endsWith("\"")) {
                jsonData = jsonData.substring(1, jsonData.length() - 1);
                jsonData = jsonData.replace("\\\"", "\"");
            }

            return objectMapper.readValue(jsonData, TransactionEvent.class);
        } catch (Exception e) {
            logger.error("Failed to deserialize event {}: {}",
                    resolvedEvent.getEvent().getEventId(), e.getMessage(), e);
            return null;
        }
    }

    public CompletableFuture<Void> deleteTransactionStream() {
        logger.warn("Tombstoning (deleting) stream: {}", TRANSACTION_STREAM);

        return eventStoreDBClient.tombstoneStream(TRANSACTION_STREAM)
                .thenAccept(result -> logger.info("Stream {} has been successfully tombstoned", TRANSACTION_STREAM))
                .exceptionally(ex -> {
                    logger.error("Failed to tombstone stream {}: {}", TRANSACTION_STREAM, ex.getMessage(), ex);
                    throw new RuntimeException("Failed to tombstone stream", ex);
                });
    }

}