// This header contains functions to be shared between both native libraries

#include <jni.h>

#ifndef _INCLUDE_SHARED_H
#define _INCLUDE_SHARED_H

void throwOutOfMemoryError(JNIEnv* env, const char* msg);

#endif
