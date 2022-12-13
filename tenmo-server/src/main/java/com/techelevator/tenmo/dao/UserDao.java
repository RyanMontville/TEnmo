package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface UserDao {

    List<User> findAll();

    User getUserById(int id);

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean create(String username, String password);

    Account getAccountByUserId(int userId);
    Account getAccountByAccountId(int accountId);

    Transfer getTransfer(int transferId);

    List<Transfer> getTransfersByAccountId(int accountId);

    Transfer createTransfer(Transfer transfer);

    //void updateAccountBalances(BigDecimal amount, Account accountFrom, Account accountTo);

    //void updateAccountBalances(Transfer transfer);

    public void updateAccountBalance(int accountId, Account account);




}
