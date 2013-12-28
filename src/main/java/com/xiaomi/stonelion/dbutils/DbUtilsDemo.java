package com.xiaomi.stonelion.dbutils;

import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Test Common-DbUtils and Common-DBCP.
 * Require src/main/SQL/dbutils.sql
 */
public class DbUtilsDemo {
    private static final DataSource basicDataSource = DBCPDemo.getBasicDataSource();
    private static final DataSource poolingDataSource = DBCPDemo.getPoolingDataSource();

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final String I_DBUTILS = "insert into dbutils" +
            "(tinyint_v,smallint_v,mediumint_v,int_v,bigint_v,float_v,double_v,char_v,varchar_v,blob_v,text_v,date_v,time_v,year_v,datetime_v,timestamp_v)" +
            "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) on duplicate key update smallint_v=?";

    private static final String Q_DBUTILS = "select tinyint_v as tinyIntValue," +
            "smallint_v as smallIntValue," +
            "mediumint_v as mediumIntValue," +
            "int_v as intValue," +
            "bigint_v as bigIntValue," +
            "float_v as floatValue," +
            "double_v as doubleValue," +
            "char_v as charValue," +
            "varchar_v as varcharValue," +
            "blob_v as blobValue," +
            "text_v as textValue," +
            "date_v as dateValue," +
            "time_v as timeValue," +
            "year_v as yearValue," +
            "datetime_v as datetimeValue," +
            "timestamp_v as timestampValue from dbutils";

    public static void main(String[] args) {
        //insert a record by BasicDataSource
        int insertNumRows = insertData(basicDataSource, 1);
        System.out.println("insert number of rows : " + insertNumRows);

        //insert a record by PoolingDataSource
        insertNumRows = insertData(poolingDataSource, 2);
        System.out.println("insert number of rows : " + insertNumRows);

        // query all use customized ResultSetHandler
        queryUseRawResultSetHandler(basicDataSource);

        // query all use BeanListHandler
        queryUseBeanListHandler(basicDataSource);

        // async version of using BeanListHandler
        asyncQuery(basicDataSource);
    }

    /**
     * Insert data to mysql.
     *
     * @param dataSource
     * @param index
     * @return
     */
    private static int insertData(DataSource dataSource, int index) {
        try {
            Object[] params = new Object[17];

            params[0] = index;
            params[1] = index;
            params[2] = index;
            params[3] = index;
            params[4] = index;

            params[5] = index + 0.123f;
            params[6] = index + 0.123d;

            params[7] = "i am index " + index + " at char type field.";
            params[8] = "i am index " + index + " at varchar type field.";
            params[9] = ("i am index " + index + " at blob type field.").getBytes("utf-8");
            params[10] = "i am index " + index + " at text type field.";

            java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
            params[11] = date;//for date

            java.sql.Time time = new java.sql.Time(System.currentTimeMillis());
            params[12] = time;// for time

            params[13] = "2013";//for year

            java.sql.Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            params[14] = timestamp;// for datetime
            params[15] = timestamp;// for timestamp

            params[16] = index; // for duplicate key.

            QueryRunner queryRunner = new QueryRunner(dataSource);
            return queryRunner.update(I_DBUTILS, params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Select all records from mysql.
     * Use customized ResultSetHandler.
     *
     * @param dataSource
     */
    private static void queryUseRawResultSetHandler(DataSource dataSource) {
        ResultSetHandler<List<DbUtilsBean>> resultSetHandler = new ResultSetHandler<List<DbUtilsBean>>() {
            @Override
            public List<DbUtilsBean> handle(ResultSet resultSet) throws SQLException {
                List<DbUtilsBean> ret = new ArrayList<DbUtilsBean>();
                while (resultSet.next()) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    System.out.println("Column count " + metaData.getColumnCount());

                    DbUtilsBean dbUtilsBean = new DbUtilsBean();
                    dbUtilsBean.setTinyIntValue(resultSet.getInt(1));
                    dbUtilsBean.setSmallIntValue(resultSet.getInt(2));
                    dbUtilsBean.setMediumIntValue(resultSet.getInt(3));
                    dbUtilsBean.setIntValue(resultSet.getInt(4));
                    dbUtilsBean.setBigIntValue(resultSet.getInt(5));
                    dbUtilsBean.setFloatValue(resultSet.getFloat(6));
                    dbUtilsBean.setDoubleValue(resultSet.getDouble(7));
                    dbUtilsBean.setCharValue(resultSet.getString(8));
                    dbUtilsBean.setVarcharValue(resultSet.getString(9));
                    dbUtilsBean.setBlobValue(resultSet.getBytes(10));
                    dbUtilsBean.setTextValue(resultSet.getString(11));
                    dbUtilsBean.setDateValue(resultSet.getDate(12));
                    dbUtilsBean.setTimeValue(resultSet.getTime(13));
                    dbUtilsBean.setYearValue(resultSet.getString(14));
                    dbUtilsBean.setDatetimeValue(resultSet.getTimestamp(15));
                    dbUtilsBean.setTimestampValue(resultSet.getTimestamp(16));

                    ret.add(dbUtilsBean);
                }
                return ret;
            }
        };

        try {
            QueryRunner queryRunner = new QueryRunner(dataSource);
            List<DbUtilsBean> list = queryRunner.query("select * from dbutils", resultSetHandler);
            for (DbUtilsBean bean : list) {
                bean.print();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Select all records from mysql.
     * Use BeanListHandler.
     * There are plenty of ResultSetHandler implementations.
     * See also http://commons.apache.org/proper/commons-dbutils/examples.html
     *
     * @param dataSource
     */
    private static void queryUseBeanListHandler(DataSource dataSource) {
        try {
            BeanListHandler<DbUtilsBean> beanBeanListHandler = new BeanListHandler<DbUtilsBean>(DbUtilsBean.class);
            QueryRunner queryRunner = new QueryRunner(dataSource);
            List<DbUtilsBean> list = queryRunner.query(Q_DBUTILS, beanBeanListHandler);
            for (DbUtilsBean bean : list) {
                bean.print();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * asynchronous version.
     *
     * @param dataSource
     */
    private static void asyncQuery(DataSource dataSource) {
        try {
            BeanListHandler<DbUtilsBean> beanBeanListHandler = new BeanListHandler<DbUtilsBean>(DbUtilsBean.class);
            AsyncQueryRunner queryRunner = new AsyncQueryRunner(executorService, new QueryRunner(dataSource));
            Future<List<DbUtilsBean>> future = queryRunner.query(Q_DBUTILS, beanBeanListHandler);
            List<DbUtilsBean> list = future.get();
            for (DbUtilsBean bean : list) {
                bean.print();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
