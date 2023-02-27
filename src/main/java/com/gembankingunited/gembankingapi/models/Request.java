package com.gembankingunited.gembankingapi.models;

import com.gembankingunited.gembankingapi.enums.PrivacyLevel;
import com.gembankingunited.gembankingapi.enums.Status;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    private String id = UUID.randomUUID().toString();
    private String memo = "";
    private String requester = "";
    private String responder = "";
    private double amount;
    private String date;
    private Status requestStatus;
    private PrivacyLevel privacySetting;

    public Request (String id) {
        this.id = id;
    }

    public Request(Request copy) {
        this.id = copy.id;
        this.memo = copy.memo;
        this.requester = copy.requester;
        this.responder = copy.responder;
        this.amount = copy.amount;
        this.date = copy.date;
        this.requestStatus = copy.requestStatus;
        this.privacySetting = copy.privacySetting;
    }
}
