package com.xiaomi.stonelion.dbutils;

import org.apache.commons.dbcp.*;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;

/**
 * Created with IntelliJ IDEA.
 * User: shixin
 * Date: 12/28/13
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBCPDemo {
    /**
     * Create pooling datasource with BasicDataSource.
     *
     * @return
     */
    public static DataSource getBasicDataSource() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        basicDataSource.setUrl("jdbc:mysql://localhost:3306/stonelion");
        basicDataSource.setUsername("root");
        basicDataSource.setPassword("shixin");
        basicDataSource.setMaxActive(100);
        basicDataSource.setMaxIdle(20);
        basicDataSource.setValidationQuery("/* ping */ select 1");
        basicDataSource.setTestOnBorrow(true);
        return basicDataSource;
    }

    public static DataSource getBasicDataSource(String host, String db, String username, String password) {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        basicDataSource.setUrl("jdbc:mysql://" + host + ":3306/" + db);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
        basicDataSource.setMaxActive(100);
        basicDataSource.setMaxIdle(20);
        basicDataSource.setValidationQuery("/* ping */ select 1");
        basicDataSource.setTestOnBorrow(true);
        return basicDataSource;
    }

    /**
     * Create pooling datasource.
     * Use it can add more custom features.
     *
     * @return
     */
    public static DataSource getPoolingDataSource() {
        // ConnectionFactory is to create database connection used by PoolableConnectionFactory.

        // There are two implementations of ConnectionFactory
        // 1 DriverManagerConnectionFactory
        // 2 DataSourceConnectionFactory
        ConnectionFactory factory = new DriverManagerConnectionFactory("jdbc:mysql://localhost:3306/stonelion", "root", "shixin");

        // A object pool. See Common-Pool
        ObjectPool objectPool = new GenericObjectPool();

        // Used to create poolable database connection.
        PoolableConnectionFactory poolableFactory = new PoolableConnectionFactory(factory, objectPool, null, null, false, true);

        PoolingDataSource poolingDataSource = new PoolingDataSource(objectPool);
        return poolingDataSource;
    }
}