package com.jamaa.service_users.service;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jamaa.service_users.dto.LoginRequest;
import com.jamaa.service_users.events.CustomerEvent;
import com.jamaa.service_users.model.Customer;
import com.jamaa.service_users.repository.CustomerRepository;

@Service
public class CustomerService {
    
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public Customer createCustomer(Customer customer) {
        try {
            String encodedPassword = passwordEncoder.encode(customer.getPassword());
            String initialPassword = customer.getPassword();
            customer.setPassword(encodedPassword);

            CustomerEvent event = new CustomerEvent();
            event.setFirstName(customer.getFirstName());
            event.setLastName(customer.getLastName());
            event.setEmail(customer.getEmail());
            event.setCniNumber(customer.getCniNumber());
            event.setCniRecto(customer.getCniRecto());
            event.setCniVerso(customer.getCniVerso());

            Customer saving = customerRepository.save(customer);
            rabbitTemplate.convertAndSend("customerExchange", "customer.create", event);

            login(customer.getEmail(), initialPassword);

            return saving;

        } catch (Exception e) {
            throw new RuntimeException("Customer insertion error : ",e);
        }
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    public List<Customer> deleteCustomer(Long id) {
        customerRepository.deleteById(id);
        return customerRepository.findAll();
    }

    public String login(String email, String password) {
        String url = "http://127.0.0.1:8079/SERVICE-AUTH/auth/login";
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        try {
            return restTemplate.postForObject(url, loginRequest, String.class);
        } catch (Exception e) {
            return "Login Failed : "+ e.getMessage();
        }
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }
}
