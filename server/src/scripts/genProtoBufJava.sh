#!/bin/bash
genjavacode() {
	mkdir -p ../gensrc/java
	for file in $PROTO_PATH/*.proto
	do
		echo "generate java code for $file"
		protoc --java_out=../gensrc/java --proto_path=$PROTO_PATH $file
	done
}

genluacode() {
	mkdir -p ../gensrc/lua
	for file in $PROTO_PATH/*.proto
	do
		echo "generate lua code for $file"
		protoc --lua_out=../gensrc/lua --proto_path=$PROTO_PATH $file
	done
}

prepare() {
	mkdir -p ../../target/proto
	cp ../main/protoc/xinqihd/*.proto $PROTO_PATH
	cp ../main/protoc/extend/*.proto $PROTO_PATH
}

export PROTO_PATH=../../target/proto/
prepare
genjavacode
genluacode
