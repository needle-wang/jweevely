jweevely
========

a exec jsp shell, simply like weevely php C/S shell.

#2014年 11月 10日 星期一 22:26:33 CST
I code it for the OS of chinese environment(gbk), also can run in an en_OS.

use cookie to send encrypted data, the same as weevely.
it can show the right gbk and utf8 words.
support cd(I write it by myself...).
support some simple completion(supported by jline).
can exec cmd.
can upload text file, not binary~

module(not exactly module, just another jsp~):

module_db.jsp     : it using a timer can remove itself.
reverse_shell.jsp : from msf.
...(such as jfolder.jsp by yourself~)


I wrote it before learned git.
the changelog is so poor~

C/S in jdk1.6(not sure that if it can be in jdk1.5 or lower):
the client need some jar to run:

httpclient-4.3.1.jar
httpcore-4.3.jar
commons-logging-1.1.3.jar
jline-2.10.jar
commons-codec-1.8.jar
commons-lang3-3.1.jar
mysql-connector-java-5.1.7-bin.jar

