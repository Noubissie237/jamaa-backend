
package com.jamaa.service_banks_account.messaging;


import com.jamaa.service_banks_account.event.BankCreatedEvent;
import com.jamaa.service_banks_account.rabbit.RabbitConfig;
import com.jamaa.service_banks_account.service.BankAccountService;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankAccountConsumer {

    private final BankAccountService bankAccountService;
    

   @RabbitListener(queues = RabbitConfig.BANK_INFO_QUEUE)
    public void receiveBankCreatedEvent(BankCreatedEvent event) {
        try {
            bankAccountService.createBankAccount(event.getBankInfo());
            bankAccountService.updateBankInfoCache(event.getBankInfo());
     } catch (Exception e) {
         e.printStackTrace();
     }
    }


    
}