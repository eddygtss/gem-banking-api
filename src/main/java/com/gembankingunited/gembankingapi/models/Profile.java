package com.gembankingunited.gembankingapi.models;

import com.gembankingunited.gembankingapi.enums.PrivacyLevel;
import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
public class Profile {
    private String documentId;
    private String username;
    private String firstName;
    private String lastName;
    private PrivacyLevel privacySetting;
    private String status;

    public Profile(String documentId, String username, String firstName, String lastName, PrivacyLevel privacySetting, String status) {
        this.documentId = documentId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.privacySetting = privacySetting;
        this.status = status;
    }
}
