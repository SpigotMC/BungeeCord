#include "shared.h"
#include <stdlib.h>
#include <stdio.h>

void throwOutOfMemoryError(JNIEnv* env, const char* msg) {
    jclass exceptionClass = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
    if (!exceptionClass) {
        // If the proxy ran out of memory, loading this class may fail
        fprintf(stderr, "OUT OF MEMORY: %s\n", msg);
        fprintf(stderr, "Could not load class java.lang.OutOfMemoryError!\n");
        exit(-1);
        return;
    }
    (*env)->ThrowNew(env, exceptionClass, msg);
}
