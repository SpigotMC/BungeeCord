// Header to check for SSE 2, SSSE 3, and SSE 4.2 support in compression natives
// GCC only!

#ifndef _INCLUDE_CPUID_HELPER_H
#define _INCLUDE_CPUID_HELPER_H

#include <stdbool.h>
#include <cpuid.h>

static inline bool checkCompressionNativesSupport() {
    unsigned int eax, ebx, ecx, edx;
    if(__get_cpuid(1, &eax, &ebx, &ecx, &edx)) {
        return (edx & bit_SSE2) != 0 && (ecx & bit_SSSE3) != 0 && (ecx & bit_SSE4_2) != 0;
    }else {
        return false;
    }
}

#endif // _INCLUDE_CPUID_HELPER_H
