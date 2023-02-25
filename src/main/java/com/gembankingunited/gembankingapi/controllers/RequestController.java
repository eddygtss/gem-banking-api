package com.gembankingunited.gembankingapi.controllers;

import com.gembankingunited.gembankingapi.models.Request;
import com.gembankingunited.gembankingapi.services.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class RequestController {
    @Autowired
    public RequestService requestService;

    @PostMapping("/request")
    public ResponseEntity<String> requestFunds(@RequestBody Request requestFundsTransaction) throws Exception {
        return requestService.Request(requestFundsTransaction);
    }

    @PostMapping("/approve-request")
    public ResponseEntity<String> approveRequestedFunds(@RequestBody String id) throws Exception {
        return requestService.approveRequest(id);
    }

    @PostMapping("/deny-request")
    public ResponseEntity<String> denyRequestedFunds(@RequestBody String id) throws Exception {
        return requestService.denyRequest(id);
    }
}
