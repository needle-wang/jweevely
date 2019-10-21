package jweevely;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * just a demo, not be used.
 * jdbc标准迭代例子
 * 我记得以前好像写过，不知道放哪去了~
 *
 * @author needle 2014年 05月 15日 星期四 22:38:41 CST
 */
public class DBOperator {

  // 1.获取数据库连接对象
  public static Connection getConn(String driverName, String url,
                                   String user, String pass) throws SQLException,
      ClassNotFoundException {
    // 加载数据库驱动
    Class.forName(driverName);
    return DriverManager.getConnection(url, user, pass);
  }

  // 2.1获取语句执行对象
  public static Statement getStatement(Connection conn) throws SQLException {
    Statement stmt;
    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
        ResultSet.CONCUR_READ_ONLY);
    return stmt;
  }

  // 2.2获取预处理语句执行对象
  public static PreparedStatement getPreparedStatement(Connection conn,
                                                       String sql) throws SQLException {
    PreparedStatement pstmt;
    pstmt = conn.prepareStatement(sql);
    return pstmt;
  }

  // 3.获取结果集对象
  public static ResultSet getResultSet(Statement stmt, String sql)
      throws SQLException {
    ResultSet res;
    res = stmt.executeQuery(sql);
    return res;
  }

  // 4.关闭资源方法
  public static void close(Connection conn, Statement stmt, ResultSet res) {
    close(res);
    close(stmt);
    close(conn);
  }

  // 关闭语句对象
  private static void close(Statement stmt) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException e) {
      }
      stmt = null;
    }
  }

  // 关闭结果集对象
  private static void close(ResultSet res) {
    if (res != null) {
      try {
        res.close();
      } catch (SQLException e) {
      }
      res = null;
    }
  }

  // 关闭数据库连接对象
  private static void close(Connection conn) {
    try {
      if (conn != null && !conn.isClosed()) {
        conn.close();
      }
    } catch (SQLException e) {
    }
    conn = null;
  }

  public static void main(String[] args) {
    String driverName = "com.mysql.jdbc.Driver";
    String conn_url = "jdbc:mysql://127.0.0.1/db_name";
    String conn_userName = "root";
    String conn_userPass = "123456";

    String sql = "show databases";

    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd;

    try {
      conn = DBOperator.getConn(driverName, conn_url, conn_userName,
          conn_userPass);

      statement = DBOperator.getStatement(conn);

      rs = DBOperator.getResultSet(statement, sql);

      rsmd = rs.getMetaData();
      int colCount = rsmd.getColumnCount();

      for (int i = 0; i < colCount; i++) {
        System.out.print(rsmd.getColumnName(i + 1) + "|");
      }
      System.out.println();

      while (rs.next()) {
        for (int i = 0; i < colCount; i++) {
          System.out.print(rs.getString(i + 1) + "|");
        }
        System.out.println();
      }
    } catch (ClassNotFoundException e) {
      System.out.println("class not found: '" + e.getMessage() + "'");
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    DBOperator.close(conn, statement, rs);
  }
}
