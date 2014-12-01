echo on

set TomcatRoot=c:\Tomcat
cd %TomcatRoot%
bin\Tomcat8.exe stop
copy /a conf\server.xml_8443 conf\server.xml /v /y
pause  "conf\server.xml_8443 --> conf\server.xml   is complete"
