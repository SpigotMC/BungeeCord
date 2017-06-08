#!/bin/sh

CXX="g++ -shared -fPIC -O3 -Wall -Werror -I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"

#x64
$CXX src/main/c/NativeCipherImpl.cpp -o src/main/resources/native-cipher_x64.so -lcrypto
$CXX src/main/c/NativeCompressImpl.cpp -o src/main/resources/native-compress_x64.so -lz

#x32
$CXX -m32 src/main/c/NativeCipherImpl.cpp -o src/main/resources/native-cipher_x32.so -lcrypto
$CXX -m32 src/main/c/NativeCompressImpl.cpp -o src/main/resources/native-compress_x32.so -lz