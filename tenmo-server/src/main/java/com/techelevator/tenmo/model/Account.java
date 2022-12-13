package com.techelevator.tenmo.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class Account {
    private int accountId;
    @NotNull
    private int userId;
    @Min(value = 0, message = "Account balance cannot be negative.")
    BigDecimal balance;
    @NotNull
    String username;

    public Account(){}

    public Account(int accountId, int userId, BigDecimal balance, String username) {
        this.accountId = accountId;
        this.userId = userId;
        this.balance = balance;
        this.username = username;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
