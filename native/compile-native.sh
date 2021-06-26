#!/bin/sh

set -eu

echo "Compiling mbedtls"
(cd mbedtls && make no_test)

echo "Compiling zlib"
(cd zlib && CFLAGS=-fPIC ./configure --static && make)

CXX="g++ -shared -fPIC -Wl,--wrap=memcpy -O3 -Wall -Werror -I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"

$CXX -Imbedtls/include src/main/c/NativeCipherImpl.cpp -o src/main/resources/native-cipher.so mbedtls/library/libmbedcrypto.a
$CXX -Izlib src/main/c/NativeCompressImpl.cpp -o src/main/resources/native-compress.so zlib/libz.a
