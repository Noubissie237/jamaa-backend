package com.jamaa.service_notifications.events;

import java.io.Serializable;
import java.util.Date;

/**
 * Événement émis lorsqu'une activité suspecte est détectée sur un compte
 */
public class SuspiciousActivityEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String activityId;
    private String userId;
    private String email;
    private String activityType;
    private String deviceInfo;
    private String location;
    private String ipAddress;
    private Date activityTime;
    private String accountNumber;
    private Double amount;
    private String additionalInfo;
    private int riskLevel; // 1-5, où 5 est le risque le plus élevé
    
    // Constructeur par défaut nécessaire pour la désérialisation
    public SuspiciousActivityEvent() {
    }
    
    // Constructeur avec les champs essentiels
    public SuspiciousActivityEvent(String activityId, String userId, String email, String activityType, 
                                  String deviceInfo, String location, Date activityTime) {
        this.activityId = activityId;
        this.userId = userId;
        this.email = email;
        this.activityType = activityType;
        this.deviceInfo = deviceInfo;
        this.location = location;
        this.activityTime = activityTime;
    }
    
    // Getters et setters
    public String getActivityId() {
        return activityId;
    }
    
    public void setActivityId(String activityId) {
        this.activityId = activityId;
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
    
    public String getActivityType() {
        return activityType;
    }
    
    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public Date getActivityTime() {
        return activityTime;
    }
    
    public void setActivityTime(Date activityTime) {
        this.activityTime = activityTime;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public String getAdditionalInfo() {
        return additionalInfo;
    }
    
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    public int getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(int riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    @Override
    public String toString() {
        return "SuspiciousActivityEvent{" +
                "activityId='" + activityId + '\'' +
                ", userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", activityType='" + activityType + '\'' +
                ", deviceInfo='" + deviceInfo + '\'' +
                ", location='" + location + '\'' +
                ", activityTime=" + activityTime +
                ", riskLevel=" + riskLevel +
                '}';
    }
}