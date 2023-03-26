package com.gembankingunited.gembankingapi.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Slf4j
@Service
public class FirebaseInitialize {
    @PostConstruct
    public void initialize() {
        try {

            InputStream serviceAccount =
                    getClass().getResourceAsStream("/gem-bankers-united-firebase.json");

            if (serviceAccount != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
            } else {
                throw new RuntimeException("Firebase config not found!");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}