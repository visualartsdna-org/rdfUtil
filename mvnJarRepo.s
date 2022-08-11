#!/bin/sh
#
mvn install:install-file -Dfile=target/rdfUtil-1.0.0.jar -DgroupId=org.visualartsdna -DartifactId=rdfUtil -Dversion=1.0.0 -Dpackaging=jar -DgeneratePom=true
