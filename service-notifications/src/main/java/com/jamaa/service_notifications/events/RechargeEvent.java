package com.jamaa.service_notifications.events;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class RechargeEvent extends BaseEvent {
    
    @JsonProperty("amount")
    private double amount;
    
    @JsonProperty("recharge_method")
    private String rechargeMethod;
    
    @JsonProperty("reference_number")
    private String referenceNumber;
    
    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @JsonProperty("operator_name")
    private String operatorName;
    
    @JsonProperty("account_number")
    private String accountNumber;
    
    @JsonProperty("transaction_date")
    private String transactionDate;
    
    @JsonProperty("transaction_status")
    private String transactionStatus;
    
    private LocalDateTime rechargeDateTime;
    private String commission;
    private String operatorCode;
    
    // Getters and Setters
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getRechargeMethod() {
        return rechargeMethod;
    }
    
    public void setRechargeMethod(String rechargeMethod) {
        this.rechargeMethod = rechargeMethod;
    }
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getOperatorName() {
        return operatorName;
    }
    
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public String getTransactionStatus() {
        return transactionStatus;
    }
    
    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
    
    public LocalDateTime getRechargeDateTime() {
        return rechargeDateTime;
    }
    
    public void setRechargeDateTime(LocalDateTime rechargeDateTime) {
        this.rechargeDateTime = rechargeDateTime;
    }
    
    public String getCommission() {
        return commission;
    }
    
    public void setCommission(String commission) {
        this.commission = commission;
    }
    
    public String getOperatorCode() {
        return operatorCode;
    }
    
    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }
}