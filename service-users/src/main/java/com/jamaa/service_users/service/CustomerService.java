package com.jamaa.service_users.service;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jamaa.service_users.dto.LoginRequest;
import com.jamaa.service_users.events.CustomerEvent;
import com.jamaa.service_users.events.SupAdminEvent;
import com.jamaa.service_users.model.Customer;
import com.jamaa.service_users.model.SuperAdmin;
import com.jamaa.service_users.repository.CustomerRepository;
import com.jamaa.service_users.repository.SuperAdminRepository;
import com.jamaa.service_users.utils.Util;

@Service
public class CustomerService {
    
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    SuperAdminRepository superAdminRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Util util;

    public Customer createCustomer(Customer customer) {
        try {
            String encodedPassword = passwordEncoder.encode(customer.getPassword());
            String initialPassword = customer.getPassword();
            customer.setPassword(encodedPassword);
            customer.setIsVerified(false);

            CustomerEvent event = new CustomerEvent();
            event.setFirstName(customer.getFirstName());
            event.setLastName(customer.getLastName());
            event.setEmail(customer.getEmail());
            event.setCniNumber(customer.getCniNumber());
            event.setCniRecto(customer.getCniRecto());
            event.setCniVerso(customer.getCniVerso());

            Customer saving = customerRepository.save(customer);
            event.setId(saving.getId());
            
            rabbitTemplate.convertAndSend("CustomerExchange", "customer.create.admin", event);
            rabbitTemplate.convertAndSend("CustomerExchange", "customer.create.account", event);
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
        String url = "http://service-proxy:8079/service-auth/auth/login";
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setLogin(email);
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

    public Customer getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone).orElse(null);
    }

    public SuperAdmin createSuperAdmin(SuperAdmin supAdmin) {
        supAdmin.setPassword(util.generateRandomPassword());
        supAdmin.setUsername(generateUniqueUsername(supAdmin.getFirstName(), supAdmin.getLastName()));

        SupAdminEvent event = new SupAdminEvent();
        event.setEmail(supAdmin.getEmail());
        event.setFirstName(supAdmin.getFirstName());
        event.setLastName(supAdmin.getLastName());
        event.setUsername(supAdmin.getUsername());
        event.setPassword(supAdmin.getPassword());

        SuperAdmin saving = superAdminRepository.save(supAdmin);

        rabbitTemplate.convertAndSend("AdminExchange", "superadmin.create.notification", event);
        
        return saving;
    }

    public List<SuperAdmin> getAllSuperAdmins() {
        return superAdminRepository.findAll();
    }

    public SuperAdmin getSuperAdminById(Long id) {
        return superAdminRepository.findById(id).orElse(null);
    }

    public SuperAdmin getSuperAdminByUsername(String username) {
        return superAdminRepository.findByUsername(username).orElse(null);
    }

    private String generateUniqueUsername(String firstName, String lastName) {
        for(int attempt = 1; attempt <= 10; attempt++) {
            String candidateUsername = util.generateUsername(firstName, lastName);

            if(!superAdminRepository.findByUsername(candidateUsername).isPresent()) {
                return candidateUsername;
            }
        }

        throw new IllegalStateException("Impossible de générer un username unique");
    }

    public Customer updatePasswCustomer(Long id, String password) {
        Customer customer = customerRepository.findById(id).orElse(null);
        customer.setPassword(passwordEncoder.encode(password));
        return customerRepository.save(customer);
    }

    public Customer updateCniNumbCustomer(Long id, String cniNumber) {
        Customer customer = customerRepository.findById(id).orElse(null);
        customer.setCniNumber(cniNumber);
        return customerRepository.save(customer);
    }

    public Customer updatePhoneCustomer(Long id, String phone) {
        Customer customer = customerRepository.findById(id).orElse(null);
        customer.setPhone(phone);
        return customerRepository.save(customer);
    }

    public Customer updateEmailCustomer(Long id, String email) {
        Customer customer = customerRepository.findById(id).orElse(null);
        customer.setEmail(email);
        return customerRepository.save(customer);
    }

    public Customer updateFirstNameCustomer(Long id, String firstName) {
        Customer customer = customerRepository.findById(id).orElse(null);
        customer.setFirstName(firstName);
        return customerRepository.save(customer);
    }

    public Customer updateLastNameCustomer(Long id, String lastName) {
        Customer customer = customerRepository.findById(id).orElse(null);
        customer.setLastName(lastName);
        return customerRepository.save(customer);
    }

    public Customer updateIsVerifiedCustomer(Long id, Boolean isVerified) {
        Customer customer = customerRepository.findById(id).orElse(null);
        customer.setIsVerified(isVerified);
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, String email, String phone, String cniNumber, String firstName, String lastName) {
        Customer customer = customerRepository.findById(id).orElse(null);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setCniNumber(cniNumber);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);

        return customerRepository.save(customer);
    }

}
