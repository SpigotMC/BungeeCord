#!/bin/sh
set -e

CXX="g++ -shared -fPIC -O3 -Wall -Werror -I${JAVA_HOME}/include/ -I${JAVA_HOME}/include/linux/"

# compile cipher (as native-cipher.so.LIB_SSL_VERSION)
CIPHER_OUT="src/main/resources/native-cipher.so"
$CXX src/main/c/NativeCipherImpl.cpp -o "$CIPHER_OUT" -lcrypto
LCRYPTO_VERSION=$(readelf -d "$CIPHER_OUT" | grep 'Shared library: \[libcrypto.so'| awk '{print $5}' | cut -d. -f3- | cut -d']' -f1)
[ -z "$LCRYPTO_VERSION" ] && { echo "Unable to determine libcrypto version"; exit 1; }
mv "$CIPHER_OUT" "${CIPHER_OUT}.${LCRYPTO_VERSION}"

# compile compress
$CXX src/main/c/NativeCompressImpl.cpp -o src/main/resources/native-compress.so -lz

