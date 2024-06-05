#!/bin/sh

set -eu

echo "Compiling mbedtls"
(cd mbedtls && make no_test)

echo "Compiling zlib"
(cd zlib && CFLAGS=-fPIC ./configure --static && make)

CC="gcc -shared -fPIC -O3 -Wall -Werror -I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"

$CC -Imbedtls/include src/main/c/NativeCipherImpl.c -o src/main/resources/native-cipher.so mbedtls/library/libmbedcrypto.a
$CC -Izlib src/main/c/NativeCompressImpl.c -o src/main/resources/native-compress.so zlib/libz.a
