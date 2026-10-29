package com.library.lms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_num")
    private Integer rowNum;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 20)
    private String transactionId;

    @Column(name = "transaction_date", length = 50)
    private String transactionDate;

    @Column(name = "timestamp", length = 50)
    private String timestamp;

    @Column(name = "plan_mode", length = 50)
    private String planMode;

    @Column(name = "mem_id", length = 20)
    private String memId;

    @Column(name = "mem_subscription_amount", precision = 10, scale = 2)
    private BigDecimal memSubscriptionAmount;

    @Column(name = "plan_type", length = 50)
    private String planType;

    @Column(name = "plan_start", length = 50)
    private String planStart;

    @Column(name = "plan_end", length = 50)
    private String planEnd;

    @Column(name = "subscription_status")
    private Integer subscriptionStatus;

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPlanMode() {
        return planMode;
    }

    public void setPlanMode(String planMode) {
        this.planMode = planMode;
    }

    public String getMemId() {
        return memId;
    }

    public void setMemId(String memId) {
        this.memId = memId;
    }

    public BigDecimal getMemSubscriptionAmount() {
        return memSubscriptionAmount;
    }

    public void setMemSubscriptionAmount(BigDecimal memSubscriptionAmount) {
        this.memSubscriptionAmount = memSubscriptionAmount;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getPlanStart() {
        return planStart;
    }

    public void setPlanStart(String planStart) {
        this.planStart = planStart;
    }

    public String getPlanEnd() {
        return planEnd;
    }

    public void setPlanEnd(String planEnd) {
        this.planEnd = planEnd;
    }

    public Integer getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(Integer subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }
}
