#!/bin/sh
cd `dirname $0`
java -classpath hsqldb-2.4.1.jar org.hsqldb.server.Server -port 11002 -database.0 file:./data/ewaconfhelp -dbname.0 ewaconfhelp
