package com.xiaomi.stonelion.miliao;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Created by ishikin on 14-1-23.
 */
public class ChinaCitySearcher {
    private static final Logger logger = Logger.getLogger(ChinaCitySearcher.class);
    private static ChinaCitySearcher instance = new ChinaCitySearcher();

    private static final String CITY_FILE_NAME = "/opt/soft/stonelion/city_location.txt";
    private static final int CITY_MAX_DISTANCE_KILOMETER = 50;
    private static double EARTH_RADIUS = 6378.137;

    private Map<Integer, City> cityMap = new HashMap<Integer, City>();
    private Map<LocationMapKey, List<Integer>> locationMap = new HashMap<LocationMapKey, List<Integer>>();
    private Map<String, String> nameMap = new HashMap<String, String>();
    private Map<String, String> provMap = new HashMap<String, String>();

    private ChinaCitySearcher() {
        initializeLocationMap();
    }

    public static ChinaCitySearcher getInstance() {
        return instance;
    }

    public String getCity(double longitude, double latitude) {
        List<Integer> districtIds = locationMap.get(new LocationMapKey(longitude, latitude));

        if (!CollectionUtils.isEmpty(districtIds)) {
            List<City> cityList = new ArrayList<City>();
            for (Integer districtId : districtIds) {
                if (cityMap.containsKey(districtId)) {
                    City city = cityMap.get(districtId);
                    city.setDistance(calcDistance(longitude, latitude, city.getLongitude(), city.getLatitude()));
                    cityList.add(city);
                }
            }
            Collections.sort(cityList);
            if (!CollectionUtils.isEmpty(cityList)) {
                return cityList.get(0).getDistance() <= CITY_MAX_DISTANCE_KILOMETER ? cityList.get(0).getName() : null;
            }
        }
        return null;
    }

    public String getCity(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        for (String location : nameMap.keySet()) {
            if (text.contains(location)) {
                return nameMap.get(location);
            }
        }
        return null;
    }

    public String getProvince(String city) {
        return provMap.get(city);
    }

    private void initializeLocationMap() {
        BufferedReader bufferedReader = null;

        try {
            File file = new File(CITY_FILE_NAME);
            bufferedReader = new BufferedReader(new FileReader(file));

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split("\\|");
                int districtId = Integer.parseInt(fields[0]);
                String province = fields[1];
                String cityName = fields[2];
                String distName = fields[3];
                double districtLongitude = Double.parseDouble(fields[7]);
                double districtLatitude = Double.parseDouble(fields[8]);

                cityMap.put(districtId, new City(cityName, districtLongitude, districtLatitude));

                LocationMapKey locationMapKey = new LocationMapKey(districtLongitude, districtLatitude);

                if (locationMap.containsKey(locationMapKey)) {
                    locationMap.get(locationMapKey).add(districtId);
                } else {
                    List<Integer> uniqueCityIdList = new ArrayList<Integer>();
                    uniqueCityIdList.add(districtId);
                    locationMap.put(locationMapKey, uniqueCityIdList);
                }

                if (!nameMap.containsKey(cityName)) {
                    nameMap.put(cityName, cityName);
                }
                if (!nameMap.containsKey(distName)) {
                    nameMap.put(distName, cityName);
                }
                if (!provMap.containsKey(cityName)) {
                    provMap.put(cityName, province);
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } finally {
            try {
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    private class City implements Comparable<City> {
        private String name;
        private double longitude;
        private double latitude;
        private double distance;

        public City(String name, double longitude, double latitude) {
            this.name = name;
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public double getDistance() {
            return distance;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public String getName() {
            return name;
        }

        @Override
        public int compareTo(City city) {
            if (distance < city.getDistance()) {
                return -1;
            } else if (distance == city.getDistance()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    private class LocationMapKey {
        private int longitude;
        private int latitude;

        public LocationMapKey(double longitude, double latitude) {
            this.longitude = (int) longitude;
            this.latitude = (int) latitude;
        }

        @Override
        public int hashCode() {
            return (17 * 31 + longitude) * 31 + latitude;
        }

        @Override
        public boolean equals(Object key) {
            return key instanceof LocationMapKey && ((LocationMapKey) key).longitude == longitude
                    && ((LocationMapKey) key).latitude == latitude;
        }
    }

    protected double calcDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double radLatitude1 = rad(latitude1);
        double radLatitude2 = rad(latitude2);
        double a = radLatitude1 - radLatitude2;
        double b = rad(longitude1) - rad(longitude2);

        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLatitude1) * Math.cos(radLatitude2)
                * Math.pow(Math.sin(b / 2), 2)));
        distance = distance * EARTH_RADIUS;
        return distance;
    }

    private double rad(double value) {
        return value * Math.PI / 180.0;
    }
}
