package xiaomi.com.stonelion.lucene;

public class User {
    public int id;
    public String name;
    public long birthday;
    
    public User(int id, String name, long birthday) {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
    }
    
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_BIRTHDAY = "birthday";
}
