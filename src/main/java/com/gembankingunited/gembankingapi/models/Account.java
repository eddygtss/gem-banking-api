package com.gembankingunited.gembankingapi.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

@Getter
@Setter
@RequiredArgsConstructor
public class Account implements Serializable {
    private static final long serialVersionUID = -1764970284520387975L;

    @Id
    private String documentId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String address;
    private String ssn;

    public Account(String documentId, String username, String password, String firstName, String lastName, String address, String ssn) {
        this.documentId = documentId;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.ssn = ssn;
    }
}
