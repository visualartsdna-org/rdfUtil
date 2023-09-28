
rem https://stackoverflow.com/questions/4955635/how-to-add-local-jar-files-to-a-maven-project
mvn install:install-file -Dfile=target\rdfUtil-1.0.0.jar -DgroupId=org.visualartsdna -DartifactId=rdfUtil -Dversion=1.0.0 -Dpackaging=jar -DgeneratePom=true

pause
