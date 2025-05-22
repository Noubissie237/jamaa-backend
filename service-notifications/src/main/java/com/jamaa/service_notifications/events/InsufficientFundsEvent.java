package com.jamaa.service_notifications.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class InsufficientFundsEvent extends BaseEvent {
    
    @JsonProperty("account_number")
    private String accountNumber;
    
    @JsonProperty("current_balance")
    private Double currentBalance;
    
    @JsonProperty("required_amount")
    private Double requiredAmount;
    
    @JsonProperty("transaction_type")
    private String transactionType;
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("beneficiary_name")
    private String beneficiaryName;
    
    @JsonProperty("beneficiary_account")
    private String beneficiaryAccount;
    
    @JsonProperty("currency")
    private String currency;

    // Getters et Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Calcule le montant manquant pour réaliser la transaction
     * @return La différence entre le montant requis et le solde actuel
     */
    public Double getMissingAmount() {
        if (requiredAmount != null && currentBalance != null) {
            return requiredAmount - currentBalance;
        }
        return 0.0;
    }
}