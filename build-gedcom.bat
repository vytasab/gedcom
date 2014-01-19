echo on

rem {dev prod} {spa web} when prod {nexus badger stax oasvu}

if ""%1"" == ""prod"" goto prod
if ""%2"" == ""web"" goto dev-web

echo --- dev-spa ... ... ...
cd c:\Scala\gedcom\gedcom-spa
mvn -Pspa_dev -Dmaven.test.skip=true clean install
pause  "Development spa jar is built"
goto end

if ""%2"" == ""spa"" goto end
:dev-web
echo --- dev-web ... ... ...
cd c:\Scala\gedcom\gedcom-web
set warVer=1.0
set war=gedcom
mvn -Pdev -Dmaven.test.skip=true clean install
rem pause " --- before copy execution -- "
copy /b target\gedcom-web-%warVer%.war c:\Tomcat\webapps\%war%.war /b /v /y
pause  "Development war build is complete"
goto end

:prod
if ""%2"" == ""web"" goto prod-web
echo --- prod-spa ... ... ...
cd c:\Scala\gedcom\gedcom-spa
mvn -Pspa_prod -Dmaven.test.skip=true clean install
pause  "Production spa jar is built"
goto end

if ""%2"" == ""spa"" goto end

:prod-web
if ""%3"" == ""default"" goto prodDefault
if ""%3"" == ""nexus"" goto prodNexus
if ""%3"" == ""badger"" goto prodBadger
if ""%3"" == ""stax"" goto prodStax
if ""%3"" == ""aosvu"" goto prodAosvu

:prodDefault
echo --- prod-web default ... ... ...
cd c:\Scala\gedcom\gedcom-web
set warVer=1.0
set war=gedcom
mvn     -Pprod -Dmaven.test.skip=true -Drun.mode=production clean install
pause " --- before copy execution -- "
copy /b target\gedcom-web-%warVer%.war c:\Tomcat\webapps\%war%.war /b /v /y
pause  "Production build is complete for d_e_f_a_u_l_t"
goto end

:prodNexus
echo --- prod-web Nexus ... ... ...
cd c:\Scala\gedcom\gedcom-web
set warVer=1.0
set war=gedcom
mvn     -PprodNexus -Dmaven.test.skip=true -Drun.mode=production clean install
pause " --- before copy execution -- "
copy /b target\gedcom-web-%warVer%.war c:\Tomcat\webapps\%war%.war /b /v /y
pause  "Production build is complete for Nexus"
goto end

:prodBadger
echo --- prod-web Badger ... ... ...
cd c:\Scala\gedcom\gedcom-web
mvn     -PprodBadger -Dmaven.test.skip=true clean install
pause  "Production build is complete for Badger"
goto end

:prodStax
echo --- prod-web Stax ... ... ...
cd c:\Scala\gedcom\gedcom-web
mvn     -PprodStax -Dmaven.test.skip=true clean install
pause  "Production build is complete for Stax"
goto end

:prodAosvu
echo --- prod-web Aosvu ... ... ...
cd c:\Scala\gedcom\gedcom-web
mvn     -PprodAosvu -Dmaven.test.skip=true clean install
pause  "Production build is complete for Aosvu"
goto end

:end
