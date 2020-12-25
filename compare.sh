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

title "Benchmarking fdupes..."
sync; echo 3 |sudo tee /proc/sys/vm/drop_caches ;\
time fdupes -rm "$dir"

title "Benchmarking jfdedup (mvn exec)..."
sync; echo 3 |sudo tee /proc/sys/vm/drop_caches ;\
mvn compile ;\
time mvn exec:java -Dexec.args="$dir"

title "Benchmarking rdfind..."
sync; echo 3 |sudo tee /proc/sys/vm/drop_caches ;\
time rdfind "$dir"
