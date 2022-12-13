package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;


@RestController
@PreAuthorize("isAuthenticated()")
public class AccountController {

    private UserDao userDao;

    public AccountController(UserDao userDao){
     this.userDao = userDao;
    }

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> listUser() {
        return userDao.findAll();
    }

    @RequestMapping(path = "/users/{userId}", method = RequestMethod.GET)
    public User getUser(@PathVariable int userId) {
        User user = userDao.getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        } else {
            return user;
        }
    }

    @RequestMapping(path = "/users/{userId}/account", method = RequestMethod.GET)
    public Account getAccountByUserId (@Valid @PathVariable int userId) {
        Account account = userDao.getAccountByUserId(userId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found.");
        } else {
            return account;
        }
    }

    @RequestMapping(path = "/accounts/{accountId}", method = RequestMethod.GET)
    public Account getAccountByAccountId(@Valid @PathVariable int accountId) {
        Account account = userDao.getAccountByAccountId(accountId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found.");
        } else {
            return account;
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/transfers", method = RequestMethod.POST)
    public Transfer newTransfer(@Valid @RequestBody Transfer transfer) {
        return userDao.createTransfer(transfer);
    }

    @RequestMapping(path = "/transfers/{transferId}",method = RequestMethod.GET)
    public Transfer getTransferById (@Valid @PathVariable int transferId) {
        Transfer transfer = userDao.getTransfer(transferId);
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found.");
        } else {
            return transfer;
        }
    }

    @RequestMapping(path = "/accounts/{accountId}/transfers", method = RequestMethod.GET)
    public List<Transfer> getTransfersByAccountId(@Valid @PathVariable int accountId) {
        List<Transfer> transfers = userDao.getTransfersByAccountId(accountId);
        if (transfers == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found.");
        } else {
            return transfers;
        }
    }

    @RequestMapping(path = "/accounts/{accountId}", method = RequestMethod.PUT)
    public void updateAccountBalance(@RequestBody Account account, @PathVariable int accountId) {

        userDao.updateAccountBalance(accountId,account);
    }


}
