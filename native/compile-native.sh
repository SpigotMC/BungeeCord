#!/bin/sh

gcc -shared -fPIC -O3 -Werror -I$JAVA_HOME/include/ src/main/c/NativeCipherImpl.c -o src/main/resources/native-cipher.so -lcrypto
