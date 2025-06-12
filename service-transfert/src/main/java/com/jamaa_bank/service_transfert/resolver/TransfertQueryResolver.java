package com.jamaa_bank.service_transfert.resolver;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.jamaa_bank.service_transfert.model.Transfert;
import com.jamaa_bank.service_transfert.service.TransfertService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TransfertQueryResolver {
    
    @Autowired
    TransfertService transfertService;

    @QueryMapping
    public List<Transfert> getAllTransferts() {
        return transfertService.getAllTransferts();
    }
}
