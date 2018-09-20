<%@ page language="java" import="java.io.*" import="java.util.*"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>uploader</title>
</head>
<body>
bitch!!! that's a shit!!!
	<%!public static final long MAX_SIZE = 1024 * 1024 * 10;%>
	<%
		String contentType = request.getContentType();
		if ((contentType != null)
				&& (contentType.indexOf("multipart/form-data") >= 0)) {
			DataInputStream din = null;
			FileOutputStream fileOut = null;

			try {
				din = new DataInputStream(request.getInputStream());

				int formDataLength = request.getContentLength();
				if (formDataLength > MAX_SIZE) {
					out.println("<P>上传的文件字节数不可以超过" + MAX_SIZE + "</p>");
					return;
				}

				byte dataBytes[] = new byte[formDataLength];
				int byteReadCount = 0;
				int totalBytesRead = 0;
				//上传的数据保存在byte数组, 这循环写得真水……
				while (totalBytesRead < formDataLength) {
					byteReadCount = din.read(dataBytes, totalBytesRead,
							formDataLength);
					totalBytesRead += byteReadCount;
				}

				//典型的数据格式如下(实际数据外部会有两个\r\n包裹)：
				//------WebKitFormBoundaryKhozVpZHOKdiAyqm^M$
				//Content-Disposition: form-data; name="filename"; filename="le_sour.txt"^M$
				//Content-Type: text/plain^M$
				//^M$
				//this is a test.$
				//this is a t$
				//^M$
				//------WebKitFormBoundaryKhozVpZHOKdiAyqm--^M$
				String fileContent = new String(dataBytes);

				//for debug.
				FileOutputStream fos = new FileOutputStream(
						"/media/BACKUP/apache-tomcat-6.0.37/webapps/ROOT/origin_data");
				fos.write(dataBytes);
				fos.close();

				//获取从表单的数据流中取得文件名
				String saveFileName = fileContent.substring(fileContent
						.indexOf("filename=\"") + 10);
				saveFileName = saveFileName.substring(0,
						saveFileName.indexOf("\""));

				//获取父目录
				String currentFileStr = application.getRealPath(request
						.getRequestURI());
				String current_dir = new File(currentFileStr).getParent();

				String destFilePath = current_dir + File.separator
						+ saveFileName;

				out.println("contentType is: '" + contentType + "'<br/>");

				//取得post data的分隔字串
				int boundary_firstIndex = contentType.lastIndexOf("=");

				String boundary = contentType.substring(
						boundary_firstIndex + 1, contentType.length());

				//跳过多余的描述行。
				int data_start_pos;
				data_start_pos = fileContent.indexOf("filename=\"");
				data_start_pos = fileContent.indexOf("\n", data_start_pos) + 1;
				data_start_pos = fileContent.indexOf("\n", data_start_pos) + 1;
				data_start_pos = fileContent.indexOf("\n", data_start_pos) + 1;

				//post data中的分隔字串比http报头中的boundary值多两个"-"
				int last_boundary_firstIndex = fileContent
						.lastIndexOf(boundary) - 4;
				out.println("data_start_pos is '" + data_start_pos
						+ "', last_boundary_firstIndex is '"
						+ last_boundary_firstIndex + "'<br/>");

				//取得文件数据的开始的位置, 因中文占多个字节，防止可能有中文, 所以要重新按字节算一次
				int startPos = ((fileContent.substring(0, data_start_pos))
						.getBytes()).length;
				//取得文件数据的结束的位置
				int endPos = ((fileContent.substring(0,
						last_boundary_firstIndex)).getBytes()).length;
				out.println("startPos:" + startPos + ", endPos:" + endPos
						+ "<br/>");

				fileOut = new FileOutputStream(destFilePath);
				fileOut.write(dataBytes, startPos, (endPos - startPos));
				fileOut.flush();
				out.println(destFilePath);
			} catch (Exception e) {
				out.println(e.getMessage());
			} finally {
				if (fileOut != null) {
					try {
						fileOut.close();
					} catch (Exception e) {
					}
				}
			}

		}
	%>
	<hr>
	<center>
		upload text or binary file<br>
		<form action="" method="post" enctype="multipart/form-data">
			<table>
				<tr>
					<td>name: <input type="file" name="filename">
					</td>
					<td><input type="submit" value="upload"></td>
				</tr>
			</table>
		</form>
	</center>
</body>
</html>