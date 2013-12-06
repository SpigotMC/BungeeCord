#!/bin/sh

OS=`uname -s`

if [ "${OS}" = "Linux" ] ; then
    gcc -shared -fPIC -O3 -Werror -I/usr/lib/jvm/default-java/include/ src/main/c/NativeCipherImpl.c -o src/main/resources/native-cipher.so -lcrypto
elif [ "${OS}" = "FreeBSD" ] ; then
    gcc -shared -fPIC -O3 -Werror -I/usr/local/openjdk7/include/ -I/usr/local/openjdk7/include/freebsd/ src/main/c/NativeCipherImpl.c -o src/main/resources/native-cipher.so -lcrypto
fi