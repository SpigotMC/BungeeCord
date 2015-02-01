#!/bin/sh

CXX="g++ -std=c++11 -shared -fPIC -O3 -Wall -Werror -I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"

$CXX src/main/c/NativeCipherImpl.cpp -o src/main/resources/native-cipher.so -lcrypto
$CXX src/main/c/NativeCompressImpl.cpp -o src/main/resources/native-compress.so -lz
