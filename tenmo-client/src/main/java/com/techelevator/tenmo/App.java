package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.UserService;

import java.math.BigDecimal;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final UserService userService = new UserService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            userService.setAuthToken(currentUser.getToken());
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        // TODO Auto-generated method stub
        Account currentAccount = userService.getAccountByUserId(currentUser.getUser().getId());
        System.out.println("Your current balance is: $" + currentAccount.getBalance());

    }

    private void viewTransferHistory() {
        System.out.println("-------------------------------------------");
        System.out.println("Transfers");
        System.out.println("ID\t\tFrom/To\t\t\t\tAmount");
        System.out.println("-------------------------------------------");
        Account currentAccount = userService.getAccountByUserId(currentUser.getUser().getId());
        Transfer[] transfers = userService.getTransfersByAccountId(currentAccount.getAccountId());
        if(transfers.length != 0) {
            for (Transfer transfer : transfers) {
                System.out.print(transfer.getTransferId());
                if (transfer.getAccountFrom() != currentAccount.getAccountId()) {
                    System.out.print("\tFrom: " + userService.getAccountByAccountId(transfer.getAccountFrom()).getUsername());
                } else {
                    System.out.print("\tTo: " + userService.getAccountByAccountId(transfer.getAccountTo()).getUsername());
                }
                System.out.println("\t\t\t$" + transfer.getAmount());
            }
            System.out.println("---------");
            int transferId = consoleService.promptForInt("Please enter transfer ID to view details (0 to cancel): ");
            if (transferId != 0) {
                Transfer pastTransfer = userService.getTransfer(transferId);
                System.out.println("--------------------------------------------");
                System.out.println("Transfer Details");
                System.out.println("--------------------------------------------");
                System.out.println("Id: " + pastTransfer.getTransferId());
                if (pastTransfer.getAccountFrom() != currentAccount.getAccountId()) {
                    System.out.println("From: " + userService.getAccountByAccountId(pastTransfer.getAccountFrom()).getUsername());
                    System.out.println("To: Myself");
                    System.out.println("Type: Received");
                } else {
                    System.out.println("From: Me");
                    System.out.println("To: " + userService.getAccountByAccountId(pastTransfer.getAccountTo()).getUsername());
                    System.out.println("Type: Send");
                }
                System.out.println("Status: Approved");
                System.out.println("Amount: $" + pastTransfer.getAmount());
            }
        } else{
            System.out.println("No Transfer History");
        }
    }

    private void viewPendingRequests() {
        // TODO Auto-generated method stub
        System.out.println("Not yet implemented");
    }

    private void sendBucks() {
        User[] users = userService.listUsers();
        System.out.println("-------------------------------------------");
        System.out.println("Users");
        System.out.println("ID\t\tName");
        System.out.println("-------------------------------------------");
        for (User user : users) {
            System.out.println(user.getId() + "\t" + user.getUsername());
        }
        boolean notSelf = false;
        int userId = 0;
        while (!notSelf) {
            userId = consoleService.promptForInt("\nEnter ID of user you are sending to (0 to cancel): ");
            User toUser = userService.getUser(userId);
            if (userId==currentUser.getUser().getId()) {
                System.out.println("You cannot send money to yourself. Please select another user.");
            } else if (userId==0) {
                break;
            } else if (toUser == null) {
                System.out.println("User does not exist. Please select another user.");
            } else {
                notSelf = true;
            }
        }
        Account fromAccount = userService.getAccountByUserId(currentUser.getUser().getId());
        if (userId!=0) {
            User toUser = userService.getUser(userId);
            boolean notZero = false;
            BigDecimal amount = BigDecimal.valueOf(0);
            while (!notZero) {
                amount = consoleService.promptForBigDecimal("Enter amount: ");
                if (amount.compareTo(fromAccount.getBalance())!=1) {
                    if (amount.compareTo(new BigDecimal(0))==1) {
                        notZero = true;
                    } else {
                        System.out.println("Amount must be greater than zero.");
                        continue;
                    }
                } else {
                    System.out.println("Cannot transfer an amount larger than your current account balance.");
                    continue;
                }

                Transfer newTransfer = new Transfer();
                Account toAccount = userService.getAccountByUserId(userId);
                newTransfer.setAccountFrom(fromAccount.getAccountId());
                newTransfer.setAccountTo(toAccount.getAccountId());
                newTransfer.setAmount(amount);
                newTransfer.setTransferTypeId(1);
                newTransfer.setTransferStatusId(1);
                Transfer returnedTransfer = userService.newTransfer(newTransfer);
                Account fromUpdateAccount = new Account();
                fromUpdateAccount.setAccountId(fromAccount.getAccountId());
                fromUpdateAccount.setBalance(fromAccount.getBalance().subtract(amount));
                boolean successFrom = userService.updateAccountBalance(fromUpdateAccount);
                Account toUpdateAccount = new Account();
                toUpdateAccount.setAccountId(toAccount.getAccountId());
                toUpdateAccount.setBalance(toAccount.getBalance().add(amount));
                boolean successTo = userService.updateAccountBalance(toUpdateAccount);
                if (successFrom && successTo) {
                    System.out.println("Successfully transferred $" + amount + " to " + toAccount.getUsername());
                } else {
                    if(!successFrom){
                        System.out.println("Problem updating From account.");
                    } else {
                        System.out.println("something went wrong.");
                    }
                    if (!successTo){
                        System.out.println("Problem updating To account.");
                    } else {
                        System.out.println("Something went wrong.");
                    }

                }

            }


        }
    }

    private void requestBucks() {
        // TODO Auto-generated method stub
        System.out.println("Not yet implemented");
    }

}