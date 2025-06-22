package com.jamaa.service_users.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import com.jamaa.service_users.model.Customer;
import com.jamaa.service_users.model.SuperAdmin;
import com.jamaa.service_users.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CustomerMutationResolver {
    
    private final CustomerService customerService;

    @MutationMapping
    public Customer createCustomer(@Argument Customer input) {
        return customerService.createCustomer(input);
    }

    @MutationMapping
    public List<Customer> deleteCustomer(@Argument Long id) {
        return customerService.deleteCustomer(id);
    }

    @MutationMapping
    public String login(@Argument String email, @Argument String password) {
        return customerService.login(email, password);
    }

    @MutationMapping
    public SuperAdmin createSuperAdmin(@Argument SuperAdmin input) {
        return customerService.createSuperAdmin(input);
    }

    @MutationMapping
    public Customer updateCustomer(@Argument Long id, @Argument String email, @Argument String phone, @Argument String cniNumber, @Argument String firstName, @Argument String lastName) {
        return customerService.updateCustomer(id, email, phone, cniNumber, firstName, lastName);
    }

    @MutationMapping
    public Customer updateCustomerPassword(@Argument Long id, @Argument String password) {
        return customerService.updatePasswCustomer(id, password);
    }
    
}
