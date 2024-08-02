
#ifndef _INCLUDE_MBEDTLS_CONFIG_H
#define _INCLUDE_MBEDTLS_CONFIG_H

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

#endif

