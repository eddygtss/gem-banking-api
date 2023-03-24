package com.gembankingunited.gembankingapi.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.gembankingunited.gembankingapi.enums.PrivacyLevel;
import com.gembankingunited.gembankingapi.exceptions.AccountInvalidException;
import com.gembankingunited.gembankingapi.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.emptyList;

@Service
@Slf4j
public class AccountService {
    public static final String COL_USERS ="users";
    public static final String COL_BANK_ACCOUNTS ="bank_accounts";
    public static final String COL_BUDDIES ="buddies";
    public static final String COL_PROFILES ="profiles";

    public void createAccount(Account account, AccountInfo accountInfo, Buddy buddies, Profile profile) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COL_USERS).document(account.getDocumentId()).set(account);
        dbFirestore.collection(COL_BANK_ACCOUNTS).document(accountInfo.getDocumentId()).set(accountInfo);
        dbFirestore.collection(COL_BUDDIES).document(buddies.getDocumentId()).set(buddies);
        dbFirestore.collection(COL_PROFILES).document(profile.getDocumentId()).set(profile);
        collectionsApiFuture.get();
    }

    public Account getAccount(String documentId) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            DocumentReference userDocumentReference = dbFirestore.collection(COL_USERS).document(documentId);
            ApiFuture<DocumentSnapshot> userFuture = userDocumentReference.get();
            DocumentSnapshot userDocument = userFuture.get();
            Account account;

            if (userDocument.exists()) {
                account = userDocument.toObject(Account.class);
                return account;
            } else {
                throw new AccountInvalidException();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public List<String> getAllDocumentIds() throws ExecutionException, InterruptedException {
        List<String> allAccounts = new ArrayList<>();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        Iterable<DocumentReference> userDocumentReferences = dbFirestore.collection(COL_USERS).listDocuments();

        for (DocumentReference documentReference: userDocumentReferences) {
            ApiFuture<DocumentSnapshot> userFuture = documentReference.get();
            DocumentSnapshot userDocument = userFuture.get();
            Account account = userDocument.toObject(Account.class);

            assert account != null;
            allAccounts.add(account.getDocumentId());
        }

        return allAccounts;
    }

    public List<String> getAllUsernames() {
        try {
            List<String> allUsernames = new ArrayList<>();
            Firestore dbFirestore = FirestoreClient.getFirestore();
            Iterable<DocumentReference> userDocumentReferences = dbFirestore.collection(COL_PROFILES).listDocuments();

            for (DocumentReference documentReference : userDocumentReferences) {
                ApiFuture<DocumentSnapshot> userFuture = documentReference.get();
                DocumentSnapshot userDocument = userFuture.get();
                Profile profile = userDocument.toObject(Profile.class);

                assert profile != null;
                allUsernames.add(profile.getUsername());
            }

            return allUsernames;
        } catch (Exception e) {
            log.error(e.getMessage());
            return emptyList();
        }
    }

    public List<Account> getAllAccounts() throws ExecutionException, InterruptedException {
        List<Account> allAccounts = new ArrayList<>();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        Iterable<DocumentReference> userDocumentReferences = dbFirestore.collection(COL_USERS).listDocuments();

        for (DocumentReference documentReference: userDocumentReferences) {
            ApiFuture<DocumentSnapshot> userFuture = documentReference.get();
            DocumentSnapshot userDocument = userFuture.get();
            Account account = userDocument.toObject(Account.class);

            assert account != null;
            allAccounts.add(account);
        }

        return allAccounts;
    }

    public AccountInfo getAccountInfo(String documentId) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            DocumentReference bankAccDocumentReference = dbFirestore.collection(COL_BANK_ACCOUNTS).document(documentId);
            ApiFuture<DocumentSnapshot> bankAccFuture = bankAccDocumentReference.get();
            DocumentSnapshot bankAccDocument = bankAccFuture.get();
            AccountInfo accountInfo;

            if (bankAccDocument.exists()) {
                accountInfo = bankAccDocument.toObject(AccountInfo.class);
                return accountInfo;
            } else {
                throw new AccountInvalidException("The account " + documentId.substring(5) + " is not valid.");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return AccountInfo.builder().build();
        }
    }

    public List<String> getBuddyListDocumentIds(List<Profile> profiles) {
        List<String> documentIds = new ArrayList<>();
        try {
            for (Profile profile : profiles) {
                documentIds.add(profile.getDocumentId());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return documentIds;
    }

    public List<Transaction> getBuddyTransactions(Buddy buddy) {
        try {
            List<Transaction> allTransactions = new ArrayList<>();
            List<Transaction> publicTransactions = new ArrayList<>();
            List<String> myBuddies = getBuddyListDocumentIds(buddy.getBuddyList());
            List<Buddy> myBuddiesInfo = getListOfBuddyInfo(myBuddies);

            for (Transaction transaction : buddy.getBuddyTransactions()) {
                if (transaction.getPrivacySetting().equals(PrivacyLevel.PRIVATE)) {
                    publicTransactions.add(transaction);
                }
            }

            for (Buddy buddyInfo : myBuddiesInfo) {
                allTransactions.addAll(buddyInfo.getBuddyTransactions());
            }

            for (Transaction transaction : allTransactions) {
                if (transaction.getPrivacySetting().equals(PrivacyLevel.PUBLIC)) {
                    publicTransactions.add(transaction);
                }
            }

            return publicTransactions;
        } catch (Exception e) {
            log.error(e.getMessage());
            return emptyList();
        }
    }

    public List<Buddy> getListOfBuddyInfo(List<String> documentIds) {
        try {
            List<Buddy> allBuddies = new ArrayList<>();

            for (String buddyDocumentId : documentIds) {
                allBuddies.add(getBuddy(buddyDocumentId));
            }

            return allBuddies;
        } catch (Exception e) {
            log.error(e.getMessage());
            return emptyList();
        }
    }

    public Buddy getBuddy(String documentId) {
        Buddy currentUsersBuddyInfo = Buddy.builder().build();
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();

            DocumentReference buddiesDocumentReference = dbFirestore.collection(COL_BUDDIES).document(documentId);
            ApiFuture<DocumentSnapshot> buddiesFuture = buddiesDocumentReference.get();
            DocumentSnapshot buddiesDocument = buddiesFuture.get();

            if (buddiesDocument.exists()) {
                currentUsersBuddyInfo = buddiesDocument.toObject(Buddy.class);
                assert currentUsersBuddyInfo != null;
                if (currentUsersBuddyInfo.getBuddyList().size() > 0) {
                    List<Profile> profiles = currentUsersBuddyInfo.getBuddyList();
                    ArrayList<Profile> updatedProfiles = new ArrayList<>();
                    for (Profile profile : profiles) {
                        Profile updated = getProfile(profile.getDocumentId());

                        updatedProfiles.add(updated);
                    }
                    currentUsersBuddyInfo.setBuddyList(updatedProfiles);
                }

                return currentUsersBuddyInfo;
            } else {
                throw new AccountInvalidException("The account " + documentId.substring(5) + " is not valid.");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return currentUsersBuddyInfo;
    }

    public Profile getProfile(String documentId) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            DocumentReference profileDocumentReference = dbFirestore.collection(COL_PROFILES).document(documentId);
            ApiFuture<DocumentSnapshot> buddiesFuture = profileDocumentReference.get();
            DocumentSnapshot profileDocument = buddiesFuture.get();
            Profile profile;

            if (profileDocument.exists()) {
                profile = profileDocument.toObject(Profile.class);
                return profile;
            } else {
                throw new AccountInvalidException("The account " + documentId.substring(5) + " is not valid.");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Profile.builder().build();
        }
    }

    public String updateAccount(Account account) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COL_USERS).document(account.getDocumentId()).set(account);
            return "Account successfully updated at " + collectionsApiFuture.get().getUpdateTime();
        } catch (Exception e) {
            log.error(e.getMessage());
            return e.getMessage();
        }
    }

    public void updateAccountInfo(AccountInfo account) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COL_BANK_ACCOUNTS).document(account.getDocumentId()).set(account);
            collectionsApiFuture.get();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void updateBuddy(Buddy buddies) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COL_BUDDIES).document(buddies.getDocumentId()).set(buddies);
            collectionsApiFuture.get();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void updateProfile(Profile profile) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COL_PROFILES).document(profile.getDocumentId()).set(profile);
            collectionsApiFuture.get();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public String deleteAccount(String documentId) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            dbFirestore.collection(COL_USERS).document(documentId).delete();
            dbFirestore.collection(COL_BANK_ACCOUNTS).document(documentId).delete();
            dbFirestore.collection(COL_BUDDIES).document(documentId).delete();
            return "Successfully deleted " + documentId;
        } catch (Exception e) {
            log.error(e.getMessage());
            return e.getMessage();
        }
    }

}
