echo on

cd c:\Scala\gedcom\gedcom-web
set warVer=1.0
set war=gedcom
rem mvn -Pdev -Dmaven.test.skip=true clean install
rem rem pause " --- before copy execution -- "
copy /b target\gedcom-web-%warVer%.war c:\Tomcat\webapps\%war%.war /b /v /y
pause  "Copying is complete"
