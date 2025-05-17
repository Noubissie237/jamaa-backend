package com.jamaa.service_notifications.events;

import java.io.Serializable;
import java.util.Date;

/**
 * Événement émis lorsqu'une transaction échoue en raison d'un solde insuffisant
 */
public class InsufficientFundsEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private String userId;
    private String email;
    private String accountNumber;
    private String accountType;
    private Double currentBalance;
    private Double requiredAmount;
    private String currency;
    private String transactionType;
    private String transactionId;
    private Date eventTime;
    private String beneficiaryName;
    private String beneficiaryAccount;
    private String additionalInfo;
    
    // Constructeur par défaut nécessaire pour la désérialisation
    public InsufficientFundsEvent() {
    }
    
    // Constructeur avec les champs essentiels
    public InsufficientFundsEvent(String eventId, String userId, String email, String accountNumber, 
                                 Double currentBalance, Double requiredAmount, String transactionType) {
        this.eventId = eventId;
        this.userId = userId;
        this.email = email;
        this.accountNumber = accountNumber;
        this.currentBalance = currentBalance;
        this.requiredAmount = requiredAmount;
        this.transactionType = transactionType;
        this.eventTime = new Date();
        this.currency = "EUR"; // Par défaut en euros
    }
    
    // Getters et setters
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public Double getCurrentBalance() {
        return currentBalance;
    }
    
    public void setCurrentBalance(Double currentBalance) {
        this.currentBalance = currentBalance;
    }
    
    public Double getRequiredAmount() {
        return requiredAmount;
    }
    
    public void setRequiredAmount(Double requiredAmount) {
        this.requiredAmount = requiredAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public Date getEventTime() {
        return eventTime;
    }
    
    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }
    
    public String getBeneficiaryName() {
        return beneficiaryName;
    }
    
    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }
    
    public String getBeneficiaryAccount() {
        return beneficiaryAccount;
    }
    
    public void setBeneficiaryAccount(String beneficiaryAccount) {
        this.beneficiaryAccount = beneficiaryAccount;
    }
    
    public String getAdditionalInfo() {
        return additionalInfo;
    }
    
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    /**
     * Calcule le montant manquant pour réaliser la transaction
     * @return La différence entre le montant requis et le solde actuel
     */
    public Double getMissingAmount() {
        return requiredAmount - currentBalance;
    }
    
    @Override
    public String toString() {
        return "InsufficientFundsEvent{" +
                "eventId='" + eventId + '\'' +
                ", userId='" + userId + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", currentBalance=" + currentBalance +
                ", requiredAmount=" + requiredAmount +
                ", transactionType='" + transactionType + '\'' +
                ", eventTime=" + eventTime +
                '}';
    }
}