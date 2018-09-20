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
- module_db.jsp: operate DB, it can remove itself by using a timer  
- module_uploadbin.jsp: upload binary file
- reverse_shell.jsp: from msf  
- ...(#TODO, such as jfolder.jsp, do it by yourself)  

##### HOWTO
`java -jar jweevely.jar http://127.0.0.1:8080/jweevely.jsp passwd_in_jsp`

##### SERVER AND CLIENT
- SERVER: `jweevely.jsp`  
  - can only be deployed in java 8 or lower  
  - `key_important` is the md5 value of `passwd_in_jsp`

- CLIENT: `jweevely.jar`  
  - default: java 8  

##### MISC
2018年 09月 17日 星期一 04:20:20 CST  
tested in tomcat6, 8  
I code it before learned git, so the old changelog is a mess.  
now I am a pythoner and nearly forget how to code java(OMG)...  
I think it would be easier and better that the client use python(#TODO?)...  

my e-mail:  
needlewang2011@gmail.com

