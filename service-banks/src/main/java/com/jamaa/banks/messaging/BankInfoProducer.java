package com.jamaa.banks.messaging;


import com.jamaa.banks.dto.BankInfoDTO;
import com.jamaa.banks.entity.Bank;
import com.jamaa.banks.event.BankCreatedEvent;
import com.jamaa.banks.rabbit.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankInfoProducer {

    private final RabbitTemplate rabbitTemplate;
    public void sendBankCreatedEvent(Bank bank) {
        try {
            BankInfoDTO bankInfo = new BankInfoDTO();
            bankInfo.setId(bank.getId());
            bankInfo.setName(bank.getName());
            bankInfo.setMinimumBalance(bank.getMinimumBalance());
            bankInfo.setWithdrawFees(bank.getWithdrawFees());
            bankInfo.setInternalTransferFees(bank.getInternalTransferFees());
            bankInfo.setExternalTransferFees(bank.getExternalTransferFees());

            BankCreatedEvent event = new BankCreatedEvent();
            event.setBankInfo(bankInfo);

            rabbitTemplate.convertAndSend(RabbitConfig.BANK_EXCHANGE, RabbitConfig.BANK_INFO_ROUTING_KEY, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}