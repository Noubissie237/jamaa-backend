package com.jamaa.banks.dto;


import lombok.Data;
import lombok.Builder;

import java.util.List;

/**
 * DTO pour les statistiques de souscription d'une banque
 */
@Data
@Builder
public class BankSubscriptionStats {
    private Long bankId;
    private String bankName;
    private Integer totalSubscriptions;
    private Integer activeSubscriptions;
    private Integer pendingSubscriptions;
    private Integer rejectedSubscriptions;
    private Integer closedSubscriptions;
    private Integer approvedSubscriptions;
    private List<StatusCount> subscriptionsByStatus;
    private Double approvalRate;
    private Double activeRate;

    @Data
    @Builder
    public static class StatusCount {
        private String status;
        private String label;
        private Integer count;
    }
} 