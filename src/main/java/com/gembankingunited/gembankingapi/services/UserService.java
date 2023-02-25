package com.gembankingunited.gembankingapi.services;

import com.gembankingunited.gembankingapi.models.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    public AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        String userLogin = "user_" + userName;
        try {
            Account user = accountService.getAccount(userLogin);

            return new User(user.getUsername(), user.getPassword(), new ArrayList<>());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }
}
