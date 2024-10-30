#!/bin/sh

set -eu

CWD=$(pwd)

echo "Compiling mbedtls"
(cd mbedtls && CFLAGS="-fPIC -I$CWD/src/main/c -DMBEDTLS_USER_CONFIG_FILE='<mbedtls_custom_config.h>'" make no_test)

echo "Compiling zlib"
(cd zlib && CFLAGS="-fPIC -DNO_GZIP" ./configure --static && make)

CC="gcc"
CFLAGS="-c -fPIC -O3 -Wall -Werror -I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"
LDFLAGS="-shared"

echo "Compiling bungee"
$CC $CFLAGS -o shared.o src/main/c/shared.c 
$CC $CFLAGS -Imbedtls/include -o NativeCipherImpl.o src/main/c/NativeCipherImpl.c
$CC $CFLAGS -Izlib -o NativeCompressImpl.o src/main/c/NativeCompressImpl.c

echo "Linking native-cipher.so"
$CC $LDFLAGS -o src/main/resources/native-cipher.so shared.o NativeCipherImpl.o mbedtls/library/libmbedcrypto.a

echo "Linking native-compress.so"
$CC $LDFLAGS -o src/main/resources/native-compress.so shared.o NativeCompressImpl.o zlib/libz.a

echo "Cleaning up"
rm shared.o NativeCipherImpl.o NativeCompressImpl.o
