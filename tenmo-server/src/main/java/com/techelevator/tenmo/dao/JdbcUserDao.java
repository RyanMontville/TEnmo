package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.relational.core.sql.In;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcUserDao implements UserDao {

    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");
    private final JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int findIdByUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");

        int userId;
        try {
            userId = jdbcTemplate.queryForObject("SELECT user_id FROM tenmo_user WHERE username = ?", int.class, username);
        } catch (NullPointerException | EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("User " + username + " was not found.");
        }

        return userId;
    }

    @Override
    public User getUserById(int userId) {
        String sql = "SELECT user_id, username, password_hash FROM tenmo_user WHERE user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
        if (results.next()) {
            return mapRowToUser(results);
        } else {
            return null;
        }
    }


    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash FROM tenmo_user";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while (results.next()) {
            User user = mapRowToUser(results);
            users.add(user);
        }

        return users;
    }

    @Override
    public User findByUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");

        String sql = "SELECT user_id, username, password_hash FROM tenmo_user WHERE username = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        if (rowSet.next()) {
            return mapRowToUser(rowSet);
        }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    @Override
    public boolean create(String username, String password) {

        // create user
        String sql = "INSERT INTO tenmo_user (username, password_hash) VALUES (?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        Integer newUserId;
        newUserId = jdbcTemplate.queryForObject(sql, Integer.class, username, password_hash);

        if (newUserId == null) return false;

        // create account
        sql = "INSERT INTO account (user_id, balance) values(?, ?)";
        try {
            jdbcTemplate.update(sql, newUserId, STARTING_BALANCE);
        } catch (DataAccessException e) {
            return false;
        }

        return true;
    }

    @Override
    public Account getAccountByUserId(int userId) {
        String sql = "SELECT account_id, account.user_id, balance, tenmo_user.username FROM public.account JOIN tenmo_user ON account.user_id = tenmo_user.user_id WHERE account.user_id=?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql,userId);
        if (results.next()) {
            return mapRowToAccount(results);
        } else {
            //throw new UsernameNotFoundException("Account for " + userId + " was not found.");
            return null;
        }
    }

    @Override
    public Account getAccountByAccountId(int accountId) {
        String sql = "SELECT account_id, account.user_id, balance, tenmo_user.username FROM public.account JOIN tenmo_user ON account.user_id = tenmo_user.user_id WHERE account.account_id=?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql,accountId);
        if (results.next()) {
            return mapRowToAccount(results);
        } else{
            //throw new UsernameNotFoundException("Account for " + accountId + " was not found.");
            return null;
        }
    }

    @Override
    public Transfer getTransfer(int transferId) {
        Transfer transfer = null;
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, " +
                "account_from, account_to, amount FROM public.transfer WHERE transfer_id=?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql,transferId);
        if (results.next()) {
            transfer = mapRowToTransfer(results);
        }
        return transfer;
    }

    @Override
    public List<Transfer> getTransfersByAccountId(int accountId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM public.transfer WHERE account_from=? OR account_to=?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql,accountId,accountId);
        while (results.next()) {
            transfers.add(mapRowToTransfer(results));
        }
        return transfers;
    }

    @Override
    public Transfer createTransfer(Transfer newTransfer) {
        String sql = "INSERT INTO public.transfer(transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id;";
        Integer newId = jdbcTemplate.queryForObject(sql, Integer.class,newTransfer.getTransferTypeId(),
                newTransfer.getTransferStatusId(),newTransfer.getAccountFrom(),newTransfer.getAccountTo(),newTransfer.getAmount());
        return getTransfer(newId);
    }

    @Override
    public void updateAccountBalance(int accountId, Account account) {
        String sql = "UPDATE public.account SET balance=? WHERE account_id = ?;";
        jdbcTemplate.update(sql,account.getBalance(),account.getAccountId());
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("USER");
        return user;
    }

    private Transfer mapRowToTransfer(SqlRowSet rowSet) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rowSet.getInt("transfer_id"));
        transfer.setTransferTypeId(rowSet.getInt("transfer_type_id"));
        transfer.setTransferStatusId(rowSet.getInt("transfer_status_id"));
        transfer.setAccountFrom(rowSet.getInt("account_from"));
        transfer.setAccountTo(rowSet.getInt("account_to"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));
        return transfer;
    }

    private Account mapRowToAccount(SqlRowSet rowSet) {
        Account account = new Account();
        account.setAccountId(rowSet.getInt("account_id"));
        account.setUserId(rowSet.getInt("user_id"));
        account.setBalance(rowSet.getBigDecimal("balance"));
        account.setUsername(rowSet.getString("username"));
        return account;
    }
}
