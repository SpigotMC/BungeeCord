#include <stdlib.h>
#include <string.h>

#include <zlib.h>
#include "net_md_5_bungee_jni_zlib_NativeCompressImpl.h"

// Support for CentOS 6
__asm__(".symver memcpy,memcpy@GLIBC_2.2.5");
extern "C" void *__wrap_memcpy(void *dest, const void *src, size_t n) {
    return memcpy(dest, src, n);
}

typedef unsigned char byte;

static jfieldID consumedID;
static jfieldID finishedID;

void JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_initFields(JNIEnv* env, jclass clazz) {
    // We trust that these fields will be there
    consumedID = env->GetFieldID(clazz, "consumed", "I");
    finishedID = env->GetFieldID(clazz, "finished", "Z");
}

jint throwException(JNIEnv *env, const char* message, int err) {
    // These can't be static for some unknown reason
    jclass exceptionClass = env->FindClass("net/md_5/bungee/jni/NativeCodeException");
    jmethodID exceptionInitID = env->GetMethodID(exceptionClass, "<init>", "(Ljava/lang/String;I)V");

    jstring jMessage = env->NewStringUTF(message);

    jthrowable throwable = (jthrowable) env->NewObject(exceptionClass, exceptionInitID, jMessage, err);
    return env->Throw(throwable);
}

void JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_reset(JNIEnv* env, jobject obj, jlong ctx, jboolean compress) {
    z_stream* stream = (z_stream*) ctx;
    int ret = (compress) ? deflateReset(stream) : inflateReset(stream);

    if (ret != Z_OK) {
        throwException(env, "Could not reset z_stream", ret);
    }
}

void JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_end(JNIEnv* env, jobject obj, jlong ctx, jboolean compress) {
    z_stream* stream = (z_stream*) ctx;
    int ret = (compress) ? deflateEnd(stream) : inflateEnd(stream);

    free(stream);

    if (ret != Z_OK) {
        throwException(env, "Could not free z_stream: ", ret);
    }
}

jlong JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_init(JNIEnv* env, jobject obj, jboolean compress, jint level) {
    z_stream* stream = (z_stream*) calloc(1, sizeof (z_stream));
    int ret = (compress) ? deflateInit(stream, level) : inflateInit(stream);

    if (ret != Z_OK) {
        throwException(env, "Could not init z_stream", ret);
    }

    return (jlong) stream;
}

jint JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_process(JNIEnv* env, jobject obj, jlong ctx, jlong in, jint inLength, jlong out, jint outLength, jboolean compress) {
    z_stream* stream = (z_stream*) ctx;

    stream->avail_in = inLength;
    stream->next_in = (byte*) in;

    stream->avail_out = outLength;
    stream->next_out = (byte*) out;

    int ret = (compress) ? deflate(stream, Z_FINISH) : inflate(stream, Z_PARTIAL_FLUSH);

    switch (ret) {
        case Z_STREAM_END:
            env->SetBooleanField(obj, finishedID, true);
            break;
        case Z_OK:
            break;
        default:
            throwException(env, "Unknown z_stream return code", ret);
    }

    env->SetIntField(obj, consumedID, inLength - stream->avail_in);

    return outLength - stream->avail_out;
}
