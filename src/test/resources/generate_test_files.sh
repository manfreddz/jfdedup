#!/bin/bash
set -e

cd "`dirname "$0"`"

touch empty.data
dd if=/dev/urandom of=original.data bs=1M count=10
cp original.data copy.data
ln original.data hardlink.data
dd if=/dev/urandom of=bigger.data bs=1M count=11
(dd if=original.data bs=1M count=1; dd if=/dev/urandom bs=1M count=9) > samestart.data
(dd if=/dev/urandom bs=1M count=9; dd if=original.data bs=1M count=1 skip=9) > sameend.data
(dd if=original.data bs=1M count=1; dd if=/dev/urandom bs=1M count=8; dd if=original.data bs=1M count=1 skip=1) > samestartandend.data
