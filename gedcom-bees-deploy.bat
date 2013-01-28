echo on
cd c:\Scala\gedcom\gedcom-web
mvn bees:deploy -Dmaven.test.skip=true -Dbees.environment=prod
pause
