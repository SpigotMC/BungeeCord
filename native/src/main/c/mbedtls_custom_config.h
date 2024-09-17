
// This is a hack to deal with a glitch that happens when mbedtls is compiled against glibc
// but then run on a linux distro that uses musl libc. This implementation of the zeroize
// is compatible with both glibc and musl without requiring the library to be recompiled.

// I checked with a disassembler and for BungeeCord's usage of the library, implementing
// this function as a static function only resulted in 2 different subroutines referencing
// different versions of memset_func, so we might as well keep things simple and use a
// static function here instead of requiring the mbedtls makefile to be modified to add
// additional source files.

#ifndef _INCLUDE_MBEDTLS_CUSTOM_CONFIG_H
#define _INCLUDE_MBEDTLS_CUSTOM_CONFIG_H

#include <string.h>

#define MBEDTLS_PLATFORM_ZEROIZE_ALT

#define mbedtls_platform_zeroize mbedtls_platform_zeroize_impl

// hack to prevent compilers from optimizing the memset away
static void *(*const volatile memset_func)(void *, int, size_t) = memset;

static void mbedtls_platform_zeroize_impl(void *buf, size_t len) {
    if (len > 0) {
        memset_func(buf, 0, len);
    }
}

#endif // _INCLUDE_MBEDTLS_CUSTOM_CONFIG_H

