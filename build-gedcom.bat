echo off

if ""%1"" == ""prod"" goto prod
if ""%2"" == ""web"" goto dev-web

echo --- dev-spa ... ... ...
cd c:\Scala\gedcom\gedcom-spa
mvn -Pspa_dev -Dmaven.test.skip=true clean install
pause  "Development spa jar is built"

if ""%2"" == ""spa"" goto end
:dev-web
echo --- dev-web ... ... ...
cd c:\Scala\gedcom\gedcom-web
mvn -Pdev -Dmaven.test.skip=true clean install
pause  "Development war build is complete"
goto end

:prod
if ""%2"" == ""web"" goto prod-web
echo --- prod-spa ... ... ...
cd c:\Scala\gedcom\gedcom-spa
mvn -Pspa_prod -Dmaven.test.skip=true clean install
pause  "Production spa spa is built"

if ""%2"" == ""spa"" goto end
:prod-web
echo --- prod-web ... ... ...
cd c:\Scala\gedcom\gedcom-web
mvn -Pprod -Dmaven.test.skip=true clean install
pause  "Production build is complete"

:end
