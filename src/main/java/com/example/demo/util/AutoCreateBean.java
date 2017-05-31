package com.example.demo.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AutoCreateBean {

	private static final String DRIVER = "com.mysql.jdbc.Driver";

	private static final String USER = "root";

	private static final String PASSWORD = "root";

	private static final String URL = "jdbc:mysql://172.16.1.210:3306/middlepayapp?autoReconnect=true&autoReconnectForPools=true";

	private static String tablename; 

	private String[] colnames; // 列名数组

	private String[] colTypes; // 列名类型数组

	private int[] colSizes; // 列名大小数组

	private boolean f_util = false; // 是否需要导入包java.util.*

	private boolean f_sql = false; // 是否需要导入包java.sql.*

	
	
	/**
	 *    获取指定数据库中包含的表
	 * TBlist
	 * @time 2016年3月4日下午5:54:52
	 * @packageName com.util
	 * @return     返回所有表名(将表名放到一个集合中)
	 * @throws Exception
	 */
	public List<String> TBlist() throws Exception {
		//  访问数据库 采用 JDBC方式
		Class.forName(DRIVER);

		Connection con = DriverManager.getConnection(URL, USER, PASSWORD);

		DatabaseMetaData md = con.getMetaData();

		List<String> list = null;

		ResultSet rs = md.getTables(null, null, null, null);
		if (rs != null) {
			list = new ArrayList<String>();
		}
		while (rs.next()) {
			// System.out.println("|表"+(i++)+":"+rs.getString("TABLE_NAME"));
			String tableName = rs.getString("TABLE_NAME");
			list.add(tableName);
		}

		rs = null;
		md = null;
		con = null;

		return list;
	}

	public String GenEntity(List<String> TBlist) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSetMetaData rsmd = null;

//		StringBuffer content = new StringBuffer();
		conn = DriverManager.getConnection(URL, USER, PASSWORD);

		for (int k = 0; k < TBlist.size(); k++) {
			tablename = TBlist.get(k);
			String strsql = "select * from " + tablename;
			try {
				pstmt = conn.prepareStatement(strsql);
				rsmd = pstmt.getMetaData();
				int size = rsmd.getColumnCount(); // 共有多少列
				colnames = new String[size];
				colTypes = new String[size];
				colSizes = new int[size];
				for (int i = 0; i < rsmd.getColumnCount(); i++) {
					colnames[i] = rsmd.getColumnName(i + 1);
					colTypes[i] = rsmd.getColumnTypeName(i + 1);
					if (colTypes[i].equalsIgnoreCase("datetime")) {
						f_util = true;
					}
					if (colTypes[i].equalsIgnoreCase("image")
							|| colTypes[i].equalsIgnoreCase("text")) {
						f_sql = true;
					}
					colSizes[i] = rsmd.getColumnDisplaySize(i + 1);
				}
//				content.append(parse(colnames, colTypes, colSizes));

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				pstmt = null;
				rsmd = null;
				conn = null;
			}
		}
//		return content.toString();
		return parse();
	}

	/**
	 * 解析处理(生成实体类主体代码)
	 */
	private String parse() {
		StringBuffer sb = new StringBuffer();
		if (f_util) {
			sb.append("import java.util.Date;\r\n");
		}
		if (f_sql) {
			sb.append("import java.sql.*;\r\n\r\n\r\n");
		}
		sb.append("public class " + initcap(tablename) + " {\r\n");
		processAllAttrs(sb);
		processAllMethod(sb);
		sb.append("}\r\n");

//		markerBean(initcap(tablename), sb.toString(), "com/bean");

		return sb.toString();

	}

	/**
	 * 创建java 文件 将生成的属性 get/set 方法 保存到 文件中 markerBean
	 * 
	 * @time 2015年9月29日下午4:15:22
	 * @packageName fanshe
	 * @param className
	 *            类名称
	 * @param content
	 *            类内容 包括属性 getset 方法
	 */
	public void markerBean(String className, String content, String packageName) {
		
		System.out.println("创建");
		
		String folder = System.getProperty("user.dir") + "/src/main/java/" + packageName + "/";

		File file = new File(folder);
		if (!file.exists()) {
			file.mkdirs();
		}
		String fileName = folder + className + ".java";

		try {
			File newdao = new File(fileName);
			FileWriter fw = new FileWriter(newdao);
			fw.write("package\t" + packageName.replace("/", ".") + ";\r\n");
			fw.write(content);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		System.out.println("创建结束");
	}

	/**
	 * 生成所有的方法
	 * 
	 * @param sb
	 */
	private void processAllMethod(StringBuffer sb) {
		for (int i = 0; i < colnames.length; i++) {
			sb.append("\tpublic void set" + initcap(colnames[i]) + "("
					+ sqlType2JavaType(colTypes[i]) + " " + toLowerCaseFirstOne(colnames[i])
					+ "){\r\n");
			sb.append("\t\tthis." + toLowerCaseFirstOne(colnames[i]) + " = " + toLowerCaseFirstOne(colnames[i]) + ";\r\n");
			sb.append("\t}\r\n");

			sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get"
					+ initcap(colnames[i]) + "(){\r\n");
			sb.append("\t\treturn " + toLowerCaseFirstOne(colnames[i]) + ";\r\n");
			sb.append("\t}\r\n");
		}
	}

	/**
	 * 解析输出属性
	 * 
	 * @return
	 */
	private void processAllAttrs(StringBuffer sb) {
		for (int i = 0; i < colnames.length; i++) {
			sb.append("\tprivate " + sqlType2JavaType(colTypes[i]) + " "
					+ toLowerCaseFirstOne(colnames[i]) + ";\r\n");

		}
	}

	/**
	 * 把输入字符串的首字母改成大写
	 * 
	 * @param str
	 * @return
	 */
	private String initcap(String str) {
		char[] ch = str.toCharArray();
		if (ch[0] >= 'a' && ch[0] <= 'z') {
			ch[0] = (char) (ch[0] - 32);
		}
		return new String(ch);
	}
	
	
	private String toLowerCaseFirstOne(String str){
		if (Character.isLowerCase(str.charAt(0)))
			return str;
		else
			return (new StringBuilder()).append(Character.toLowerCase(str.charAt(0))).append(str.substring(1)).toString();

	}
	

	private String sqlType2JavaType(String sqlType) {
		if (sqlType.equalsIgnoreCase("bit")) {
			return "bool";
		} else if (sqlType.equalsIgnoreCase("tinyint")) {
			return "byte";
		} else if (sqlType.equalsIgnoreCase("smallint")) {
			return "short";
		} else if (sqlType.equalsIgnoreCase("int")) {
			return "int";
		} else if (sqlType.equalsIgnoreCase("bigint")) {
			return "long";
		} else if (sqlType.equalsIgnoreCase("float")) {
			return "float";
		} else if (sqlType.equalsIgnoreCase("decimal")
				|| sqlType.equalsIgnoreCase("numeric")
				|| sqlType.equalsIgnoreCase("real")) {
			return "double";
		} else if (sqlType.equalsIgnoreCase("money")
				|| sqlType.equalsIgnoreCase("smallmoney")) {
			return "double";
		} else if (sqlType.equalsIgnoreCase("varchar")
				|| sqlType.equalsIgnoreCase("char")
				|| sqlType.equalsIgnoreCase("nvarchar")
				|| sqlType.equalsIgnoreCase("nchar")) {
			return "String";
		} else if (sqlType.equalsIgnoreCase("datetime")) {
			return "Date";
		}

		else if (sqlType.equalsIgnoreCase("image")) {
			return "Blob";
		} else if (sqlType.equalsIgnoreCase("text")) {
			return "Clob";
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		AutoCreateBean auto = new AutoCreateBean();
//		List<String> list = auto.TBlist();
		
		List<String> list = new ArrayList<String>();
		
		list.add("tab_ymf_qrcode");
		
		String content = auto.GenEntity(list);
		String packageName = "com/rhjf/appserver/model";
		auto.markerBean(auto.initcap(tablename), content, packageName);
	}
}
