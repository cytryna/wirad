package com.javacodegeeks.spring.jpa;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

import com.javacodegeeks.spring.repositories.EmployeeRepository;


@Configuration
@Repository
public class SpringDataJpaExampleUsingXML {
	@Autowired
	private EmployeeRepository repository;
	
	@Autowired
	private DataSource dataSource;

	public static void main(String[] args) throws URISyntaxException, Exception {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		try {
			SpringDataJpaExampleUsingXML s = (SpringDataJpaExampleUsingXML) ctx.getBean("mainBean");
			System.out.println("Add employees");
			s.addEmployees();
			System.out.println("Find all employees");
			s.findAllEmployees();
			System.out.println("Find employee by name 'Joe'");
			s.findEmployee("Joe");
			System.out.println("Find employee by name 'John'");
			s.findEmployee("John");
			System.out.println("Find employees by age");
			s.findEmployeesByAge(32);
			System.out.println("Find employees between 30 and 45");
			s.findEmployeesBetweenAge(30, 45);
			System.out.println("Find employees greater than 20");
			s.findEmployeesGreaterThanAgePageWise(20, 1, 0);
			s.findEmployeesGreaterThanAgePageWise(20, 1, 1);
			s.findEmployeesGreaterThanAgePageWise(20, 2, 0);
			s.findEmployeesGreaterThanAgePageWise(20, 2, 1);
		} finally {
			ctx.close();
		}
	}

	public void addEmployees() {
		Employee emp1 = new Employee("Richard", 32);
		Employee emp2 = new Employee("Satish", 30);
		Employee emp3 = new Employee("Priya", 16);
		Employee emp4 = new Employee("Rimi", 30);
		
		repository.save(emp1);
		repository.save(emp2);
		repository.save(emp3);
		repository.save(emp4);
	}

	public void findAllEmployees() {
		repository.findAll().forEach(System.out::println);
	}

	public void findEmployee(String name) {
		List<Employee> empList = repository.findEmployeesByName(name);
		System.out.println("Employee list: " + empList);
	}

	public void findEmployeesByAge(int age) {
		List<Employee> empList = repository.findEmployeesByAge(age);
		System.out.println("Employee list: " + empList);
	}
	
	public void findEmployeesBetweenAge(int from, int to) {
		List<Employee> empList = repository.findEmployeesBetweenAge(from, to);
		System.out.println("Employee list: " + empList);
	}
	
	public void populateDatabase() throws SQLException {

		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource("data.sql"));

		Connection connection = null;

		try {
			connection = DataSourceUtils.getConnection(dataSource);
			populator.populate(connection);
		} finally {
			if (connection != null) {
				DataSourceUtils.releaseConnection(connection, dataSource);
			}
		}
	}
	
	public void findEmployeesGreaterThanAgePageWise(int age, int pageSize, int pageNbr) {
		System.out.println("Page size: " + pageSize + ", page " + pageNbr);
		Pageable pageable = new PageRequest(pageNbr, pageSize, Direction.DESC, "name", "age");
		Page<Employee> page = repository.findEmployeesByAgeGreaterThan(age, pageable);
		System.out.println(page.getContent());
	}
}
