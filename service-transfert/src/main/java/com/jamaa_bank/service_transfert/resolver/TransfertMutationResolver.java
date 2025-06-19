package com.jamaa_bank.service_transfert.resolver;

import java.math.BigDecimal;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import com.jamaa_bank.service_transfert.model.Transfert;
import com.jamaa_bank.service_transfert.service.TransfertService;
import com.jamaa_bank.service_transfert.dto.TransferRequest;
import com.jamaa_bank.service_transfert.service.TransferService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TransfertMutationResolver {
    
    private final TransfertService transfertService;
    private final TransferService transferService;

    @MutationMapping
    public Transfert makeAppTransfert(@Argument Long idSenderAccount, @Argument Long idReceiverAccount, @Argument BigDecimal amount) {
        return transfertService.transfertAppAccounts(idSenderAccount, idReceiverAccount, amount);
    }

    @MutationMapping
    public Transfert makeBankTransfert(@Argument Long idSenderBank, @Argument Long idReceiverBank, @Argument BigDecimal amount) {
        return transfertService.transfertBank(idSenderBank, idReceiverBank, amount);
    }

    @MutationMapping
    public Boolean transfer(@Argument TransferRequest request) {
        transferService.transfer(request);
        return true;
    }
}
