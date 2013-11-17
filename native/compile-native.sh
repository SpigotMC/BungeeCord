#!/bin/sh

gcc -shared -fPIC -O3 -Werror -I/usr/lib/jvm/default-java/include/ src/main/c/NativeCipherImpl.c -o src/main/resources/native-cipher.so -lcrypto
