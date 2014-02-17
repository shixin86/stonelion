package com.xiaomi.stonelion.miliao;

import com.xiaomi.miliao.dal.*;
import com.xiaomi.miliao.zookeeper.*;
import com.xiaomi.stonelion.dbutils.DBCPDemo;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: shixin
 * Date: 1/8/14
 * Time: 4:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class NearbyPOIWorker {
    private static final Logger logger = Logger.getLogger(NearbyPOIWorker.class);

    private static final String INPUT_FILE = "group_users_2.txt";
    private static final String OUTPUT_FILE = "group_users_result.xls";

    private static final String BAIDU_GET_POIS_URL = "http://api.map.baidu.com/place/v2/search";
    private static final String BAIDU_GET_POIS_SECURE_KEY = "E5842c82f03cc3594a8d0031a59a7903";
    // URLConnection timeout.
    private static final int BAIDU_HTTP_CONNECTION_TIMEOUT = 500;
    // future get timeout.
    private static final int FUTURE_TIMEOUT = 3000;

    private static final String DB_HOST = "192.168.1.205";
    private static final String db = "location";
    private static final String DB_USERNAME = "readonly";
    private static final String DB_PASSWORD = "readonly";

    private static final String QUERY_LOCATION_STR = "select latitude,longitude from lbs_history where user_id=? and app_id=1";

    private static final DataSource dataSource = DBCPDemo.getBasicDataSource(DB_HOST, db, DB_USERNAME, DB_PASSWORD);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private static final String DB_PRATITION_CONF_PATH = "/databases/partitions";
    private static final String DB_PARTITION_CONF_NAME = "senseidb_partition";
    private static final int MY_CON_NUM = 10;

    private static void initDBConnection() {
        ZKSettings zkSettings = ZKFacade.getZKSettings();
        zkSettings.setEnviromentType(EnvironmentType.PRODUCTION);

        ZKClient client = ZKFacade.getClient(DB_PRATITION_CONF_PATH);
        String data = client.getData(String.class, DB_PARTITION_CONF_NAME);
        DbAccessor.createInstance(IOUtils.toInputStream(data), MY_CON_NUM, true);

        client.registerDataChanges(String.class, DB_PARTITION_CONF_NAME, new ZKDataChangeListener<String>() {
            @Override
            public void onChanged(String path, String data) {
                DbAccessor.createInstance(IOUtils.toInputStream(data), MY_CON_NUM, true);
            }
        });
    }

    public static void main(String[] args) throws IOException, WriteException {
        logger.info("Starting NearbyPOIWorker...");

        initDBConnection();

        File inputFile = new File(INPUT_FILE);
        if (!inputFile.exists()) {
            logger.error("File file/group_users.txt is not exists!");
            return;
        }

        File outputFile = new File(OUTPUT_FILE);
        WritableWorkbook workbook = Workbook.createWorkbook(outputFile);
        WritableSheet writableSheet = workbook.createSheet("First Sheet", 0);
        int row = 0;

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                long userId = Long.parseLong(line);

                LbsHistoryBean bean = getLatlonFromDB(userId);
                if (null == bean) {
                    continue;
                }

                HttpBaiduPOIsWorker httpBaiduPOIsWorker = createHttpWorker(bean.getLatitude(), bean.getLongitude());
                Future<List<BaiduPOI>> future = executorService.submit(httpBaiduPOIsWorker);
                List<BaiduPOI> result = null;
                try {
                    result = future.get(FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    logger.error(e);
                    continue;
                }

                if (CollectionUtils.isEmpty(result)) {
                    continue;
                }

                Label userIdLabel = new Label(0, row, String.valueOf(userId));
                writableSheet.addCell(userIdLabel);

                Label latlonLable = new Label(1, row, bean.getLongitude() + "," + bean.getLatitude());
                writableSheet.addCell(latlonLable);

                String chinaCityName = ChinaCitySearcher.getInstance().getCity(bean.getLongitude(), bean.getLatitude());
                if (null == chinaCityName) {
                    chinaCityName = "未知";
                }
                Label cityNameLable = new Label(2, row, chinaCityName);
                writableSheet.addCell(cityNameLable);

                long lastActivityTime = -1;
                try {
                    lastActivityTime = queryUserLastActivityTime(userId);
                } catch (SQLException e) {
                    logger.error(e);
                }
                if (lastActivityTime != -1 && lastActivityTime != 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(lastActivityTime);

                    String content = calendar.get(Calendar.YEAR) + " " + (calendar.get(Calendar.MONTH) + 1) + " " + calendar.get(Calendar.DAY_OF_MONTH);
                    Label lastActivityTimeLabel = new Label(3, row, content);
                    writableSheet.addCell(lastActivityTimeLabel);
                }

                Label headPOINameLabel = new Label(4, row, result.get(0).name);
                writableSheet.addCell(headPOINameLabel);

                Label headPOIIdLabel = new Label(5, row, result.get(0).id);
                writableSheet.addCell(headPOIIdLabel);

                row++;

                for (int i = 1; i < result.size() && i < 10; i++) {
                    Label tailPOINameLabel = new Label(4, row, result.get(i).name);
                    writableSheet.addCell(tailPOINameLabel);

                    Label tailPOIIdLabel = new Label(5, row, result.get(i).id);
                    writableSheet.addCell(tailPOIIdLabel);

                    row++;
                }
            }

            workbook.write();
            workbook.close();

            logger.info("Ending NearbyPOIWorker...");
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static HttpBaiduPOIsWorker createHttpWorker(double latitude, double longitude) {
        int radius = 2000;
        int page = 0;
        int count = 10;

        List<String> keywords = new ArrayList<String>();
        keywords.add("小区");
        keywords.add("商场");

        return new HttpBaiduPOIsWorker(latitude, longitude, radius, page, count, keywords);
    }

    private static LbsHistoryBean getLatlonFromDB(long userId) {
        QueryRunner queryRunner = new QueryRunner(dataSource);
        BeanListHandler<LbsHistoryBean> beanBeanListHandler = new BeanListHandler<LbsHistoryBean>(LbsHistoryBean.class);
        try {
            List<LbsHistoryBean> result = queryRunner.query(QUERY_LOCATION_STR, beanBeanListHandler, userId);
            if (CollectionUtils.isEmpty(result)) {
                logger.error("There was no record in db!");
                return null;
            }
            if (result.size() != 1) {
                logger.error("There shoule be only one record in db!");
                return null;
            }
            return result.get(0);
        } catch (SQLException e) {
            logger.error("Failed to retrive data from db.", e);
        }
        return null;
    }

    @DAO
    public static interface UserActivinessDAO {
        @SQL("select last_activity_time from user_activiness where user_id=:userId")
        @SQLControl(useSlave = true)
        long queryLastActivityTime(@SQLParam("userId") long userId) throws SQLException;
    }

    private static long queryUserLastActivityTime(long userId) throws SQLException {
        return DAOFacade.getDAO(UserActivinessDAO.class).queryLastActivityTime(userId);
    }

    public static class LbsHistoryBean {
        private double latitude;
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    private static class HttpBaiduPOIsWorker implements Callable<List<BaiduPOI>> {
        private double latitude;
        private double longitude;
        private int radius;
        private int page;
        private int count;
        private List<String> keywords;
        private int responseCount;

        HttpBaiduPOIsWorker(double latitude, double longitude, int radius, int page, int count, List<String> keywords) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
            this.page = page;
            this.count = count;
            this.keywords = keywords;
        }

        @Override
        public List<BaiduPOI> call() throws Exception {
            HttpURLConnection gaodeURLConnection = null;
            try {
                String urlStr = getURLStr();
                URL url = new URL(urlStr);

                gaodeURLConnection = (HttpURLConnection) url.openConnection();
                gaodeURLConnection.setConnectTimeout(BAIDU_HTTP_CONNECTION_TIMEOUT);
                gaodeURLConnection.connect();

                InputStream inputStream = gaodeURLConnection.getInputStream();

                // We can use the blew block to get the original http-response.
                /**
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                 StringBuilder httpResponse = new StringBuilder();
                 String line = null;
                 while(null != (line = bufferedReader.readLine())){
                 httpResponse.append(line);
                 }
                 **/

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(inputStream);

                return parseXMLDocument(document);
            } finally {
                if (null != gaodeURLConnection) {
                    gaodeURLConnection.disconnect();
                }
            }
        }

        private String getURLStr() {
            StringBuilder urlStr = new StringBuilder();
            urlStr.append(BAIDU_GET_POIS_URL).append("?");
            urlStr.append("radius=").append(radius).append("&");
            urlStr.append("page_num=").append(this.page).append("&page_size=").append(count).append("&");
            urlStr.append("location=").append(this.latitude).append(",").append(this.longitude).append("&");

            urlStr.append("query=").append(keywords.get(0));
            for (int i = 1; i < keywords.size(); i++) {
                urlStr.append("$").append(this.keywords.get(i));
            }
            urlStr.append("&");


            urlStr.append("scope=2").append("&");
            urlStr.append("filter=sort_name:distance|sort_rule:1").append("&");
            urlStr.append("output=xml&");
            urlStr.append("ak=").append(BAIDU_GET_POIS_SECURE_KEY);

            return urlStr.toString();
        }

        private List<BaiduPOI> parseXMLDocument(Document document) {
            List<BaiduPOI> poiList = new ArrayList<BaiduPOI>();

            Element root = document.getDocumentElement();
            for (Node childNode = root.getFirstChild(); null != childNode; childNode = childNode.getNextSibling()) {
                if (Node.ELEMENT_NODE == childNode.getNodeType() && "total".equals(childNode.getNodeName())) {
                    this.responseCount = Integer.parseInt(childNode.getTextContent());
                }

                if (Node.ELEMENT_NODE == childNode.getNodeType() && "results".equals(childNode.getNodeName())) {
                    for (Node poiNode = childNode.getFirstChild(); poiNode != null; poiNode = poiNode.getNextSibling()) {
                        if (Node.ELEMENT_NODE == poiNode.getNodeType() && "result".equals(poiNode.getNodeName())) {
                            BaiduPOI baiduPOI = new BaiduPOI();
                            NodeList itemNodeList = poiNode.getChildNodes();
                            for (int i = 0; i < itemNodeList.getLength(); i++) {
                                Node node = itemNodeList.item(i);

                                if (Node.ELEMENT_NODE == node.getNodeType() && "uid".equals(node.getNodeName())) {
                                    baiduPOI.id = node.getTextContent();
                                }
                                if (Node.ELEMENT_NODE == node.getNodeType() && "name".equals(node.getNodeName())) {
                                    baiduPOI.name = node.getTextContent();
                                }
                                if (Node.ELEMENT_NODE == node.getNodeType() && "address".equals(node.getNodeName())) {
                                    baiduPOI.address = node.getTextContent();
                                }

                                if (Node.ELEMENT_NODE == node.getNodeType() && "location".equals(node.getNodeName())) {
                                    StringBuilder locationBuilder = new StringBuilder();
                                    for (Node locNode = node.getFirstChild(); locNode != null; locNode = locNode.getNextSibling()) {
                                        if (Node.ELEMENT_NODE == locNode.getNodeType() && "lat".equals(locNode.getNodeName())) {
                                            locationBuilder.append(locNode.getTextContent()).append(",");
                                        }
                                        if (Node.ELEMENT_NODE == locNode.getNodeType() && "lng".equals(locNode.getNodeName())) {
                                            locationBuilder.append(locNode.getTextContent());
                                        }
                                    }
                                    baiduPOI.location = locationBuilder.toString();
                                }

                                if (Node.ELEMENT_NODE == node.getNodeType() && "detail_info".equals(node.getNodeName())) {
                                    for (Node detailNode = node.getFirstChild(); detailNode != null; detailNode = detailNode.getNextSibling()) {
                                        if (Node.ELEMENT_NODE == detailNode.getNodeType() && "type".equals(detailNode.getNodeName())) {
                                            baiduPOI.type = detailNode.getTextContent();
                                        }
                                        if (Node.ELEMENT_NODE == detailNode.getNodeType() && "distance".equals(detailNode.getNodeName())) {
                                            baiduPOI.distance = detailNode.getTextContent();
                                        }
                                    }
                                }
                            }
                            poiList.add(baiduPOI);
                        }
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(poiList)) {
                BaiduPOI provideCount = new BaiduPOI();
                provideCount.isJustSetCount = true;
                provideCount.count = this.responseCount;
                poiList.add(provideCount);
            }

            return poiList;
        }
    }

    private static class BaiduPOI {
        private String id;
        private String name;
        private String type;
        private String address;
        private String location;
        private String distance;
        private int count;
        private boolean isJustSetCount;

        @Override
        public String toString() {
            return "id:" + this.id + ",name:" + this.name + ",type:" + this.type + ",address:" + this.address + ",locatoin" + this.location + ",distance:" + this.distance + ",count:" + this.count;
        }
    }
}
