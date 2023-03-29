package com.gembankingunited.gembankingapi.controllers;

import com.gembankingunited.gembankingapi.models.Request;
import com.gembankingunited.gembankingapi.services.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin("http://localhost:3000")
@RequestMapping("/api/v1")
public class RequestController {
    public RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/request")
    public ResponseEntity<String> requestFunds(@RequestBody Request requestFundsTransaction) {
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
