package com.xiaomi.stonelion.common;

public class SQL {

	public static void main(String[] args) {
		getPushHistory();
	}

	private static void getPushHistory() {
		for (int i = 0; i < 100; i++) {
			System.out.println(String.format(
					"DROP TABLE IF EXISTS `push_history_%d`;", i));
			System.out.println(String
					.format("CREATE TABLE push_history_%d(", i));
			System.out.println("user_id bigint unsigned not null primary key,");
			System.out.println("type tinyint unsigned not null default 0,");
			System.out.println("history text not null default ''");
			System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		}
	}

	private static void like() {
		for (int i = 0; i < 100; i++) {
			System.out.println(String.format(
					"select * from user_like_%d where user_id2=196946;", i));
		}
	}

	private static void getWanderCity() {
		System.out.println("DROP TABLE IF EXISTS `wander_city`;");
		System.out.println("CREATE TABLE wander_city(");
		System.out
				.println("id int(10) unsigned not null primary key auto_increment,");
		System.out.println("name varchar(128) not null default '',");
		System.out.println("modify_time timestamp not null");
		System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
	}

	private static void getWanderDistrict() {
		System.out.println("DROP TABLE IF EXISTS `wander_district`;");
		System.out.println("CREATE TABLE wander_district(");
		System.out
				.println("id int(10) unsigned not null primary key auto_increment,");
		System.out.println("city_id int(10) not null default -1,");
		System.out.println("name varchar(256) not null default '',");
		System.out.println("latlon varchar(512) not null default '',");
		System.out.println("icon varchar(1024) not null default '',");
		System.out.println("modify_time timestamp not null,");
		System.out.println("index city_id_index(city_id)");
		System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
	}

	private static void getPlUser() {
		for (int i = 0; i < 10; i++) {
			System.out.println(String.format(
					"DROP TABLE IF EXISTS `ppl_user_%d`;", i));
			System.out.println(String.format("CREATE TABLE ppl_user_%d(", i));
			System.out
					.println("user_id int(10) unsigned not null primary key,");
			System.out.println("voice_url varchar(512) not null,");
			System.out.println("modify_time timestamp not null");
			System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			System.out.println();
		}
	}

	private static void getGroupTower() {
		for (int i = 0; i < 100; i++) {
			System.out.println(String.format(
					"DROP TABLE IF EXISTS `group_tower_%d`;", i));
			System.out
					.println(String.format("CREATE TABLE group_tower_%d(", i));
			System.out
					.println("group_id int(10) unsigned not null primary key,");
			System.out.println("address varchar(64) not null,");
			System.out.println("create_time timestamp not null,");
			System.out.println("enable tinyint(3) not null default 1,");
			System.out.println("index address_index(address, enable)");
			System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			System.out.println();

		}
	}

	private static void getGroupLatLon() {
		for (int i = 0; i < 100; i++) {
			System.out.println(String.format(
					"DROP TABLE IF EXISTS `group_latlon_%d`;", i));
			System.out.println(String
					.format("CREATE TABLE group_latlon_%d(", i));
			System.out
					.println("group_id int(10) unsigned not null primary key,");
			System.out.println("latitude double(10,6) not null,");
			System.out.println("longitude double(10,6) not null,");
			System.out.println("create_time timestamp not null,");
			System.out.println("enable tinyint(3) not null default 1,");
			System.out.println("index lat_lon(latitude, longitude, enable)");
			System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			System.out.println();

		}
	}

	private static void getppl() {
		for (int i = 0; i < 100; i++) {
			System.out.println(String.format("DROP TABLE IF EXISTS `ppl_%d`;",
					i));
			System.out.println(String.format("CREATE TABLE ppl_%d(", i));
			System.out.println("user_id1 int(10) unsigned not null,");
			System.out.println("user_id2 int(10) unsigned not null,");
			System.out.println("relation tinyint unsigned not null default 0,");
			System.out.println("modify_time timestamp not null,");
			System.out.println("index modify_time_index(modify_time),");
			System.out.println("primary key(user_id1, user_id2)");
			System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			System.out.println();
		}
	}

	private static void genUserActivity() {
		for (int i = 0; i < 10; i++) {
			System.out.println(String.format(
					"DROP TABLE IF EXISTS `user_activity_%d`;", i));
			System.out.println(String.format("CREATE TABLE user_activity_%d(",
					i));
			System.out.println("user_id int(10) unsigned not null,");
			System.out
					.println("last_activity_time bigint(20) unsigned not null,");
			System.out.println("activity double(6,3) unsigned not null,");
			System.out.println("modify_time timestamp not null,");
			System.out.println("primary key(user_id)");
			System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			System.out.println();
		}
	}

	private static void genLbsHistory() {
		System.out
				.println(String.format("DROP TABLE IF EXISTS `lbs_history`;"));
		System.out.println(String.format("CREATE TABLE lbs_history("));
		System.out.println("user_id int(10) unsigned not null,");
		System.out.println("app_id tinyint(10) unsigned not null,");
		System.out.println("nickname varchar(64) not null,");
		System.out.println("sex char(1) not null,");
		System.out.println("image varchar(512) not null,");
		System.out.println("signature varchar(512) not null,");
		System.out.println("lbs_lable varchar(128) not null,");
		System.out.println("verified varchar(10) not null,");
		System.out.println("latitude double(10,7) not null,");
		System.out.println("longitude double(10,7) not null,");
		System.out.println("towers varchar(2048) not null,");
		System.out.println("create_time timestamp not null,");
		System.out.println("primary key(user_id, app_id)");
		System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		System.out.println();
	}

	private static void genDeprecatedMayknows() {
		for (int i = 0; i < 10; i++) {
			System.out.println(String.format(
					"DROP TABLE IF EXISTS `deprecated_mayknows_%d`;", i));
			System.out.println(String.format(
					"CREATE TABLE deprecated_mayknows_%d(", i));
			System.out.println("user_id int(10) unsigned not null,");
			System.out.println("mayknows mediumtext not null,");
			System.out.println("modify_time timestamp not null,");
			System.out.println("primary key(user_id)");
			System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			System.out.println();
		}
	}

	public static void ggg() {
		for (int i = 0; i < 100; i++) {
			System.out
					.println(String
							.format("select * from location_%d where object_id = 29532493;",
									i));
		}
	}

	public static void genMayknowsInfo() {
		for (int i = 0; i < 10; i++) {
			System.out.println(String.format(
					"DROP TABLE IF EXISTS `mayknows_info_%d`;", i));
			System.out.println(String.format("CREATE TABLE mayknows_info_%d(",
					i));
			System.out.println("user_id int(10) unsigned not null,");
			System.out.println("info mediumtext not null,");
			System.out.println("modify_time timestamp not null,");
			System.out.println("primary key(user_id)");
			System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			System.out.println();
		}
	}

	public static void genRecommendMayknows() {
		for (int i = 0; i < 10; i++) {
			System.out.println(String.format(
					"DROP TABLE IF EXISTS `recommend_mayknows_%d`;", i));
			System.out.println(String.format(
					"CREATE TABLE recommend_mayknows_%d(", i));
			System.out.println("user_id int(10) unsigned not null,");
			System.out.println("mayknows mediumtext not null,");
			System.out.println("modify_time timestamp not null,");
			System.out.println("primary key(user_id)");
			System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			System.out.println();
		}
	}

	public static void genApp() {
		System.out.println("DROP TABLE IF EXISTS `app`;");
		System.out.println("CREATE TABLE app(");
		System.out.println("app_id smallint(5) unsigned not null,");
		System.out.println("app_name varchar(128),");
		System.out.println("creat_time timestamp not null,");
		System.out.println("primary key(app_id),");
		System.out.println("key(app_name)");
		System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
	}

	public static void genShakeHand() {
		System.out.println("DROP TABLE IF EXISTS `shakehand`;");
		System.out.println("CREATE TABLE shakehand(");
		System.out.println("user_id int(10) unsigned not null,");
		System.out.println("modify_time timestamp not null,");
		System.out.println("longitude double(10,7) not null,");
		System.out.println("latitude double(10,7) not null,");
		System.out.println("towers_address varchar(2048) not null,");
		System.out.println("primary key(user_id),");
		System.out.println("key createTime(modify_time)");
		System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
	}

	private static void clear() {
		// for (int i = 0; i < 100; i++) {
		// System.out.println(String.format("delete from site_%d;", i));
		// }

		for (int i = 0; i < 100; i++) {
			System.out.println(String.format("delete from topic_location_%d;",
					i));
		}
	}

	private static void genEncounter() {
		for (int i = 0; i < 10; i++) {
			System.out.println(String.format(
					"DROP TABLE IF EXISTS `encounter_%d`;", i));
			System.out.println(String.format("CREATE TABLE encounter_%d(", i));
			System.out
					.println("user_id int(10) unsigned primary key not null,");
			System.out.println("encounter_times mediumblob not null,");
			System.out.println("modify_time timestamp");
			System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		}
	}

	private static void genTopic() {
		System.out.println("DROP TABLE IF EXISTS `topic`;");
		System.out.println("CREATE TABLE topic(");
		System.out.println("topic_id int(10) unsigned primary key not null,");
		System.out.println("longitude double(10,7) unsigned not null,");
		System.out.println("latitude double(10,7) unsigned not null,");
		System.out.println("towers varchar(512) not null,");
		System.out.println("world varchar(15) not null,");
		System.out.println("nation varchar(256) not null,");
		System.out.println("province varchar(256) not null,");
		System.out.println("city varchar(256) not null,");
		System.out.println("status varchar(15) not null,");
		System.out.println("hot int(10) unsigned not null,");
		System.out.println("scope tinyint(3) unsigned not null,");
		System.out.println("modify_time timestamp not null,");
		System.out.println("create_time timestamp not null");
		System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
	}

	private static void genTuba_position() {
		System.out.println("DROP TABLE IF EXISTS `tuba_position`;");
		System.out.println("CREATE TABLE tuba_position(");
		System.out
				.println("id bigint unsigned primary key auto_increment not null,");
		System.out.println("radio_type varchar(10) not null,");
		System.out.println("carrier varchar(10) not null,");
		System.out.println("language varchar(10) not null,");
		System.out.println("cell_towers varchar(512) not null,");
		System.out.println("wifi_towers varchar(512) not null,");
		System.out.println("position varchar(1024) not null");
		System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
	}

	private static void genGoogleAddress() {
		System.out.println("DROP TABLE IF EXISTS `google_address`;");
		System.out.println("CREATE TABLE google_address(");
		System.out
				.println("id bigint unsigned primary key auto_increment not null,");
		System.out.println("longitude double(10,7) not null,");
		System.out.println("latitude double(10,7) not null,");
		System.out.println("address varchar(50) not null");
		System.out.println(")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
	}
}
