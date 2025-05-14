package com.jamaa.service_users.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.jamaa.service_users.model.Customer;
import com.jamaa.service_users.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CustomerQueryResolver {
    
    private final CustomerService customerService;

    @QueryMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @QueryMapping
    public Customer getCustomerById(@Argument Long id) {
        return customerService.getCustomerById(id);
    }

    @QueryMapping
    public Customer getCustomerByEmail(@Argument String email) {
        return customerService.getCustomerByEmail(email);
    }
}
