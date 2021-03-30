#!/bin/sh
cd `dirname $0`
java -classpath hsqldb-2.5.1.jar org.hsqldb.server.Server -port 11002\
	-database.0 file:./data/ewaconfhelp/ewaconfhelp -dbname.0 ewaconfhelp\
	-database.1 file:./data/ewademo/ewademo -dbname.1 ewademo\
	-database.2 file:./data/ewa/ewa -dbname.2 ewa
