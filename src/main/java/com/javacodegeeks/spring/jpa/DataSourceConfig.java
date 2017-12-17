package com.javacodegeeks.spring.jpa;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
//@Profile("dev")
@ActiveProfiles("dev")
@EnableTransactionManagement
public class DataSourceConfig {

//	@Bean(destroyMethod = "shutdown")
//	@Profile("h2")
//	public DataSource embeddedDataSource() {
//		EmbeddedDatabase build = new EmbeddedDatabaseBuilder()
//				.setType(EmbeddedDatabaseType.H2)
//				.addScript("classpath:schema.sql")
//				.addScript("classpath:test-data.sql")
//				.build();
//		return build;
//	}

//	@Bean
//	@Profile("prod")
//	public DataSource jndiDataSource() {
//		JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
//		jndiObjectFactoryBean.setJndiName("jdbc/myDS");
//		jndiObjectFactoryBean.setResourceRef(true);
//		jndiObjectFactoryBean.setProxyInterface(javax.sql.DataSource.class);
//		return (DataSource) jndiObjectFactoryBean.getObject();
//	}
	/**
	 * Bootstraps an in-memory HSQL database.
	 */
	@Bean
	@Profile("hsql")
	public DataSource dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		return builder.setType(EmbeddedDatabaseType.HSQL).build();
	}

	/**
	 * Picks up entities from the project's base package.
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setDatabase(Database.HSQL);
		vendorAdapter.setGenerateDdl(true);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan(getClass().getPackage().getName());
		factory.setDataSource(dataSource());

		return factory;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {

		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory().getObject());
		return txManager;
	}
	
	@Bean
	@Lazy(false)
	public ResourceDatabasePopulator populateDatabase() throws SQLException {

		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource("data.sql"));

		Connection connection = null;

		try {
			connection = DataSourceUtils.getConnection(dataSource());
			populator.populate(connection);
		} finally {
			if (connection != null) {
				DataSourceUtils.releaseConnection(connection, dataSource());
			}
		}
		return populator;
	}
}
