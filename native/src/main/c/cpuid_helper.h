// Header to check for SSE 4.2 support in compression natives
// GCC only!

#ifndef _INCLUDE_CPUID_HELPER_H
#define _INCLUDE_CPUID_HELPER_H

#include <stdbool.h>
#include <cpuid.h>

static inline bool checkCompressionNativesSupport() {
    unsigned int eax;
    unsigned int ebx;
    unsigned int ecx; 
    unsigned int edx;
    if(__get_cpuid(1, &eax, &ebx, &ecx, &edx)) {
        return (ecx & bit_PCLMUL) != 0 && (ecx & bit_SSE4_2) != 0;
    }else {
        return false;
    }
}

#endif // _INCLUDE_CPUID_HELPER_H
