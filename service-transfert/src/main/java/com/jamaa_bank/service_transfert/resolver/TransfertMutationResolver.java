package com.jamaa_bank.service_transfert.resolver;

import java.math.BigDecimal;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import com.jamaa_bank.service_transfert.model.Transfert;
import com.jamaa_bank.service_transfert.service.TransfertService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TransfertMutationResolver {
    
    private final TransfertService transfertService;

    @MutationMapping
    public Transfert makeAppTransfert(@Argument Long idSenderAccount, @Argument Long idReceiverAccount, @Argument BigDecimal amount) {
        return transfertService.transfertAppAccounts(idSenderAccount, idReceiverAccount, amount);
    }
}
