package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public UserService(String url) {
        this.baseUrl = url;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(transfer,headers);
    }

    private HttpEntity<Account> makeAccountEntity(Account account) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(account,headers);
    }

    public Account getAccountByUserId(int userId) {
        Account account = null;
        try {
            ResponseEntity<Account> response = restTemplate.exchange(baseUrl + "users/" + userId + "/account", HttpMethod.GET,makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

    public Account getAccountByAccountId(int accountId) {
        Account account = null;
        try {
            ResponseEntity<Account> response = restTemplate.exchange(baseUrl + "accounts/" + accountId,HttpMethod.GET,makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

    public User[] listUsers() {
        User[] users = null;
        try {
            ResponseEntity<User[]> response = restTemplate.exchange(baseUrl + "users",HttpMethod.GET,makeAuthEntity(),User[].class);
            users = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return users;
    }

    public User getUser(int userId) {
        User user = null;
        try {
            ResponseEntity<User> response = restTemplate.exchange(baseUrl + "users/" + userId,HttpMethod.GET,makeAuthEntity(),User.class);
            user = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return user;
    }

    public Transfer newTransfer(Transfer transfer) {
        Transfer returnedTransfer = null;
        try {
            returnedTransfer = restTemplate.postForObject(baseUrl + "transfers",makeTransferEntity(transfer),Transfer.class);
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return returnedTransfer;
    }

    public Transfer[] getTransfersByAccountId(int accountId) {
        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(baseUrl + "accounts/" + accountId + "/transfers",HttpMethod.GET,makeAuthEntity(),Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public Transfer getTransfer(int transferId) {
        Transfer transfer = null;
        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(baseUrl + "transfers/" + transferId,HttpMethod.GET,makeAuthEntity(), Transfer.class);
            transfer = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfer;
    }

    public boolean updateAccountBalance(Account account) {
        boolean success = false;
        try {
            restTemplate.put(baseUrl + "accounts/" + account.getAccountId(),makeAccountEntity(account));
            success = true;
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }

        return success;
    }

}
