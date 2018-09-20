### JWEEVELY
a exec jsp shell, simply like weevely php C/S shell

##### FEATURE
1. exec cmd mainly  
2. upload text file, not binary  
3. cd(code it by myself...)  
4. gbk and utf8 presents well  
5. support some simple command line completion(supported by jline)  
6. use cookie to send encrypted data, the same as weevely  

##### MODULE(not exactly module, just another jsp):  
- module_uploadbin.jsp  : upload binary file
- module_db.jsp         : operate DB, it can remove itself by using a timer  
- reverse_shell.jsp     : from msf  
- ...(#TODO, such as jfolder.jsp, do it by yourself)  

##### HOWTO
`java -jar jweevely0.4.jar http://127.0.0.1:8080/jweevely.jsp passwd_in_jsp`

##### THE SERVER: jweevely.jsp  
can only be deployed in jdk1.8 or lower  
`key_important` is the md5 value of `passwd_in_jsp`

##### THE CLIENT: jweevely.jar  
the client contains:  
`commons-codec-1.8.jar` `commons-lang3-3.1.jar` `commons-logging-1.1.3.jar`  
`httpclient-4.3.1.jar` `httpcore-4.3.jar`  
`jline-2.10.jar` `mysql-connector-java-5.1.7-bin.jar`

##### MISC
2018年 09月 17日 星期一 04:20:20 CST  
tested in tomcat6, 8  
I code it before learned git, so the changelog is bad~  
now I am a pythoner and nearly forget how to code java(OMG)...  
I think it would be easier and better that the client use python...  

my e-mail:  
needlewang2011@gmail.com

