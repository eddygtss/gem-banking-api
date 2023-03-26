package com.gembankingunited.gembankingapi.services;

import com.gembankingunited.gembankingapi.models.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
public class UserService implements UserDetailsService {
    public AccountService accountService;

    public UserService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) {

        String userLogin = "user_" + userName;
        try {
            Account user = accountService.getAccount(userLogin);

            return new User(user.getUsername(), user.getPassword(), new ArrayList<>());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }
}
