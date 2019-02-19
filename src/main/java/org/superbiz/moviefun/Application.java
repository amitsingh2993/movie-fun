package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials() {
        String vcapJson = System.getenv("VCAP_SERVICES");
        return new DatabaseServiceCredentials(vcapJson);
    }

    @Bean(name = "albumsDataSource")
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(serviceCredentials.jdbcUrl("albums-mysql"));
        return dataSource;
    }

    @Bean(name = "moviesDataSource")
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(serviceCredentials.jdbcUrl("movies-mysql"));
        return dataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean(name = "albumsEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean albumsEntityManagerFactory(
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter,
            @Qualifier("albumsDataSource") DataSource albumsDataSource) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(albumsDataSource);
        factoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factoryBean.setPackagesToScan("org.superbiz.moviefun");
        return factoryBean;
    }

    @Bean(name = "moviesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean moviesEntityManagerFactory(
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter,
            @Qualifier("moviesDataSource") DataSource moviesDataSource) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(moviesDataSource);
        factoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factoryBean.setPackagesToScan("org.superbiz.moviefun");
        return factoryBean;
    }

    @Bean(name = "albumsTransactionManager")
    @Primary
    public PlatformTransactionManager albumsTransactionManager(@Qualifier("albumsEntityManagerFactory")LocalContainerEntityManagerFactoryBean albumsEntityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        return transactionManager;
    }

    @Bean(name = "moviesTransactionManager")
    public PlatformTransactionManager moviesTransactionManager(@Qualifier("moviesEntityManagerFactory") LocalContainerEntityManagerFactoryBean moviesEntityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        return transactionManager;
    }

    @Bean
    public String blabla() {
        System.out.println("Bla Bla");
        return "blabla";
    }
}
