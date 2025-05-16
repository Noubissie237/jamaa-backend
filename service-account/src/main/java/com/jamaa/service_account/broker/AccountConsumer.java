package com.jamaa.service_account.broker;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jamaa.service_account.service.AccountService;
import com.jamaa.service_account.events.CustomerEvent;
import com.jamaa.service_account.model.Account;

@Service
public class AccountConsumer {

    @Autowired
    private AccountService accountService;

    @RabbitListener(queues = "customerCreateQueueAccount")
    public Account customerCreateConsumer(CustomerEvent event) throws IOException {
        return accountService.createAccount(event.getId());
    }
}