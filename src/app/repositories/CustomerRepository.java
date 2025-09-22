package app.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import app.interfaces.RepositoryBase;
import app.models.Account;
import app.models.Customer;

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
        customers.remove(c);
    }

}
