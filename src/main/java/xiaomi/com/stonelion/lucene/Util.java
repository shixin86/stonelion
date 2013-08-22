
package xiaomi.com.stonelion.lucene;

import org.apache.commons.lang.StringUtils;

/**
 * @author shixin
 * @date 2012-7-2
 */
public class Util {
    protected static interface UserBoost {
        float FIELD_USERID_BOOST = 2.0f;
        float FIELD_NICKNAME_BOOST = 1.8f;
        float FIELD_CORPORATIONS_BOOST = 1.5f;
        float FIELD_SCHOOLS_BOOST = 1.5f;
        float FIELD_CITY_BOOST = 1.0f;
    }

    public static interface AllInOneUserDocKey {
        String USERID = "userId";
        String NICKNAME = "nickname";
        String SEX = "sex";
        String SCHOOL = "school";
        String CORPRATION = "corporation";
        String CITY = "city";
        String ACTIVINESS = "activeness";

        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String TOWERADDRESS = "towerAddress";
        String REG_TIME = "regTime";
        String PPL_CITY = "pplCity";
        String E = "e";
    }

    public static String getUniformedSex(String sex) {
        if (!StringUtils.isEmpty(sex)) {
            if ("M".equalsIgnoreCase(sex) || "男".equals(sex)) {
                return "M";
            }
            if ("F".equalsIgnoreCase(sex) || "女".equals(sex)) {
                return "F";
            }
        }
        return null;
    }
}
