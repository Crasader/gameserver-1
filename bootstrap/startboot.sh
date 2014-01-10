#!/bin/sh

export DEPLIB=target/dependency
export JAVA_HOME=/Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home
export JVMFLAGS="-server -Dgameserver -XX:+HeapDumpOnOutOfMemoryError -XX:MaxPermSize=256m -mx1024m -Dusefakesender=false"
export JVMFLAGS="-server"
export GSBINDIR=/Users/wangqi/disk/projects/snsgames/babywar/server/deploy
export GSMAIN="com.xinqihd.sns.gameserver.bootstrap.Bootstrap"
export PORT=3443
export HTTPPORT=8080
export ZOO=192.168.0.77
#$JAVA_HOME/bin/java $JVMFLAGS -Dfile.encoding=utf8 -cp target/bootstrap.jar com.xinqihd.sns.gameserver.bootstrap.Bootstrap -t zoo -h 192.168.0.77 -p 3443 -http 8080 -httphost 192.168.0.77 -d ../deploy/data -s ../deploy/script -u ../babywarserver/target/classes 
$JAVA_HOME/bin/java $JVMFLAGS -Duserdir=../deploy -Dconfigdir=. -Dfile.encoding=utf8 -cp target/bootstrap.jar com.xinqihd.sns.gameserver.bootstrap.Bootstrap -t zoo -h 192.168.0.77 -p 3443 -http 8080 -httphost 192.168.0.77 -d ../deploy/data -s ../deploy/script -u ../deploy/babywarserver.jar
