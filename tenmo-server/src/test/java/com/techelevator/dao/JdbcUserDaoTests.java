package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.List;

public class JdbcUserDaoTests extends BaseDaoTests {
    protected static final User USER_1 = new User(1001, "user1", "user1", "USER");
    protected static final User USER_2 = new User(1002, "user2", "user2", "USER");
    private static final User USER_3 = new User(1003, "user3", "user3", "USER");
    protected static final Account ACCOUNT_1 = new Account(9999, 1001, BigDecimal.valueOf(50.00), "user1");
    private static final Account ACCOUNT_2 = new Account(9998, 1002, BigDecimal.valueOf(3000), "user2");
    private static final Account ACCOUNT_3 = new Account(9997, 1003, BigDecimal.valueOf(10), "user3");
    private static final Transfer TRANSFER_1 = new Transfer(3999, 1, 1, 9998, 9999, BigDecimal.valueOf(25));
    private static final Transfer TRANSFER_2 = new Transfer(3998, 1, 1, 9997, 9999, BigDecimal.valueOf(9));
    private static final Transfer TRANSFER_3 = new Transfer(3997, 1, 1, 9999, 9997, BigDecimal.valueOf(2500));

    private User testUser;
    private JdbcUserDao sut;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcUserDao(jdbcTemplate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findIdByUsername_given_null_throws_exception() {
        sut.findIdByUsername(null);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void findIdByUsername_given_invalid_username_throws_exception() {
        sut.findIdByUsername("invalid");
    }

    @Test
    public void findIdByUsername_given_valid_user_returns_user_id() {
        int actualUserId = sut.findIdByUsername(USER_1.getUsername());

        Assert.assertEquals(USER_1.getId(), actualUserId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findByUsername_given_null_throws_exception() {
        sut.findByUsername(null);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void findByUsername_given_invalid_username_throws_exception() {
        sut.findByUsername("invalid");
    }

    @Test
    public void findByUsername_given_valid_user_returns_user() {
        User actualUser = sut.findByUsername(USER_1.getUsername());

        Assert.assertEquals(USER_1, actualUser);
    }

    @Test
    public void getUserById_given_invalid_user_id_returns_null() {
        User actualUser = sut.getUserById(-1);

        Assert.assertNull(actualUser);
    }

    @Test
    public void getUserById_given_valid_user_id_returns_user() {
        User actualUser = sut.getUserById(USER_1.getId());

        Assert.assertEquals(USER_1, actualUser);
    }

    @Test
    public void findAll_returns_all_users() {
        List<User> users = sut.findAll();

        Assert.assertNotNull(users);
        Assert.assertEquals(3, users.size());
        Assert.assertEquals(USER_1, users.get(0));
        Assert.assertEquals(USER_2, users.get(1));
        Assert.assertEquals(USER_3, users.get(2));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void create_user_with_null_username() {
        sut.create(null, USER_3.getPassword());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void create_user_with_existing_username() {
        sut.create(USER_1.getUsername(), USER_3.getPassword());
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_user_with_null_password() {
        sut.create(USER_3.getUsername(), null);
    }

    @Test
    public void create_user_creates_a_user() {
        User newUser = new User(-1, "new", "user", "USER");

        boolean userWasCreated = sut.create(newUser.getUsername(), newUser.getPassword());

        Assert.assertTrue(userWasCreated);

        User actualUser = sut.findByUsername(newUser.getUsername());
        newUser.setId(actualUser.getId());

        actualUser.setPassword(newUser.getPassword()); // reset password back to unhashed password for testing
        Assert.assertEquals(newUser, actualUser);
    }

    @Test
    public void getAccountByUserId_given_valid_user_id_returns_account() {
        Account actualAccount = sut.getAccountByUserId(USER_1.getId());

        assertAccountsMatch(ACCOUNT_1, actualAccount);
    }

    @Test
    public void getAccountByUserId_given_invalid_user_id_returns_null() {
        Account actualAccount = sut.getAccountByUserId(-1);

        Assert.assertNull(actualAccount);
    }

    @Test
    public void getAccountByAccountId_given_valid_account_id_returns_account() {
        Account actualAccount = sut.getAccountByAccountId(ACCOUNT_2.getAccountId());

        assertAccountsMatch(ACCOUNT_2, actualAccount);
    }

    @Test
    public void getAccountByAccountId_given_invalid_account_id_returns_null(){
        Account actualAccount = sut.getAccountByAccountId(-1);

        Assert.assertNull(actualAccount);
    }

    @Test
    public void getTransferByTransferId_given_valid_transfer_id_returns_transfer() {
        Transfer actualTransfer = sut.getTransfer(TRANSFER_1.getTransferId());

        assertTransfersMatch(TRANSFER_1, actualTransfer);
    }

    @Test
    public void getTransferByTransferId_given_invalid_transfer_id_returns_null(){
        //TODO
        Transfer actualTransfer = sut.getTransfer(-1);

        Assert.assertNull(actualTransfer);
    }


    @Test
    public void getTransferByAccountId_given_valid_account_id_returns_transfer_list() {
        //TODO
        List<Transfer> transfers = sut.getTransfersByAccountId(ACCOUNT_1.getAccountId());

        Assert.assertNotNull(transfers);
        Assert.assertEquals(3, transfers.size());
        assertTransfersMatch(TRANSFER_1, transfers.get(0));
        assertTransfersMatch(TRANSFER_2, transfers.get(1));
        assertTransfersMatch(TRANSFER_3, transfers.get(2));
    }

    @Test
    public void createTransfer_creates_a_transfer() {
        Transfer newTransfer = new Transfer(-1,1,1,9999,9998,BigDecimal.valueOf(50));

        Transfer returnedTransfer = sut.createTransfer(newTransfer);
        newTransfer.setTransferId(returnedTransfer.getTransferId());

        Transfer actualTransfer = sut.getTransfer(newTransfer.getTransferId());
        assertTransfersMatch(newTransfer,actualTransfer);
    }

    @Test
    public void updateAccountBalance_updates_account_with_expected_value() {
        Account accountToUpdate = sut.getAccountByAccountId(9999);

        accountToUpdate.setBalance(new BigDecimal(100.00));

        sut.updateAccountBalance(9999,accountToUpdate);

        Account retrievedAccount = sut.getAccountByAccountId(9999);
        assertAccountsMatch(accountToUpdate,retrievedAccount);


    }


    public void assertAccountsMatch(Account expected, Account actual){
        Assert.assertEquals(expected.getAccountId(), actual.getAccountId());
        Assert.assertEquals(expected.getUserId(), actual.getUserId());
        Assert.assertTrue(expected.getBalance().compareTo(actual.getBalance()) == 0);
        Assert.assertEquals(expected.getUsername(), actual.getUsername());
    }

    public void assertTransfersMatch(Transfer expected, Transfer actual){
        Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(expected.getTransferTypeId(), actual.getTransferTypeId());
        Assert.assertEquals(expected.getTransferStatusId(), actual.getTransferStatusId());
        Assert.assertEquals(expected.getAccountFrom(), actual.getAccountFrom());
        Assert.assertEquals(expected.getAccountTo(), actual.getAccountTo());
        Assert.assertTrue(expected.getAmount().compareTo(actual.getAmount()) == 0);
    }
//private static final Transfer TRANSFER_1 = new Transfer(3999, 1, 1, 9998, 9999, BigDecimal.valueOf(25));
}

