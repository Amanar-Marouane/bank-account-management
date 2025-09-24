package repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import models.Account;
import models.Customer;
import exceptions.CustomerNotFoundException;
import exceptions.InvalidTransactionException;

public class CustomerRepository implements RepositoryBase<Customer> {

    private static CustomerRepository instance;
    private ArrayList<Customer> customers;

    private CustomerRepository() {
        this.customers = new ArrayList<>();
    }

    public static CustomerRepository getInstance() {
        if (instance == null) {
            instance = new CustomerRepository();
        }
        return instance;
    }

    @Override
    public List<Customer> all() {
        return customers;
    }

    @Override
    public Optional<Customer> find(String field, String value) {
        switch (field.toLowerCase()) {
            case "id":
                return customers.stream()
                        .filter(c -> c.getId().toString().equals(value))
                        .findFirst();
            case "firstname":
                return customers.stream()
                        .filter(c -> c.getFirstName().equalsIgnoreCase(value))
                        .findFirst();
            case "lastname":
                return customers.stream()
                        .filter(c -> c.getLastName().equalsIgnoreCase(value))
                        .findFirst();
            case "email":
                return customers.stream()
                        .filter(c -> c.getEmail().equalsIgnoreCase(value))
                        .findFirst();
            default:
                return Optional.empty();
        }
    }

    public Optional<Customer> findCustomerByAccount(Account a) {
        return customers.stream()
                .filter(c -> c.getAccounts().contains(a))
                .findFirst();
    }

    @Override
    public void save(Customer customer) {
        if (customer == null) {
            throw new CustomerNotFoundException("Cannot save null customer");
        }
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new InvalidTransactionException("customer creation", "Email cannot be null or empty");
        }
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            throw new InvalidTransactionException("customer creation", "First name cannot be null or empty");
        }
        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            throw new InvalidTransactionException("customer creation", "Last name cannot be null or empty");
        }

        customers.add(customer);
    }

    @Override
    public void delete(String field, String value) {

        switch (field.toLowerCase()) {
            case "id":
                customers.removeIf(c -> c.getId().toString().equals(value));
                break;
            case "firstname":
                customers.removeIf(c -> c.getFirstName().equalsIgnoreCase(value));
                break;
            case "lastname":
                customers.removeIf(c -> c.getLastName().equalsIgnoreCase(value));
                break;
            case "email":
                customers.removeIf(c -> c.getEmail().equalsIgnoreCase(value));
                break;
            default:
                break;
        }
    }

    @Override
    public void delete(Customer c) {
        if (c == null) {
            throw new CustomerNotFoundException("Cannot delete null customer");
        }
        if (!customers.contains(c)) {
            throw new CustomerNotFoundException("Customer not found in repository");
        }
        if (!c.getAccounts().isEmpty()) {
            throw new InvalidTransactionException("customer deletion", "Cannot delete customer with existing accounts");
        }

        customers.remove(c);
    }

}
