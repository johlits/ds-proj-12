#!/bin/sh

CLASSPATH="$CLASSPATH":./Editor:./Sim:./images:.:.
export CLASSPATH

/usr/lib/jdk1.3/bin/javac *.java ./Editor/*.java ./Sim/*.java

