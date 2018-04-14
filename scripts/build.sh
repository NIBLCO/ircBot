#!/bin/bash

#svn update
#kill -9 `ps aux | egrep 'ooinuza.*java' | grep -v egrep | awk '{ print $2 }'`
kill -9 `ps aux | egrep 'ircbot' | grep -v grep | awk '{ print $2 }'`

#export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre

ant clean

ant compile

#ant test

#nohup java -cp lib/mysql-connector-java-5.1.17-bin.jar:bin/ ircbot.Bot config/config.txt > log.txt 2>&1 &
nohup java -cp lib/*:bin/ ircbot.Bot config/config.txt > log.txt 2>&1 &

echo "Bot Started."
tail -f log.txt

