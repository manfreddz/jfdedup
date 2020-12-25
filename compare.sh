#!/bin/bash
set -e

dir="/usr/lib"

function title {
  echo -e "\n\n"
  echo "################################"
  echo "# $@"
  echo "# $dir"
  echo "################################"
}

title "Building jfdedup..."
mvn package > /dev/null 2> /dev/null

title "Benchmarking fdupes..."
sync; echo 3 |sudo tee /proc/sys/vm/drop_caches ;\
time fdupes -rm "$dir" 2> /dev/null > /dev/null

title "Benchmarking jfdedup..."
sync; echo 3 |sudo tee /proc/sys/vm/drop_caches ;\
time java -jar target/jfdedup-1.0-SNAPSHOT-jar-with-dependencies.jar "$dir" > /dev/null

title "Benchmarking rdfind..."
sync; echo 3 |sudo tee /proc/sys/vm/drop_caches ;\
time rdfind "$dir" > /dev/null
