#include <stdlib.h>
#include <string.h>

#include <zlib.h>
#include "shared.h"
#if !defined(__aarch64__)
#include "cpuid_helper.h"
#endif
#include "net_md_5_bungee_jni_zlib_NativeCompressImpl.h"

typedef unsigned char byte;

static jclass classID;
static jfieldID consumedID;
static jfieldID finishedID;
static jmethodID makeExceptionID;

void JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_initFields(JNIEnv* env, jclass clazz) {
    classID = clazz;
    // We trust that these will be there
    consumedID = (*env)->GetFieldID(env, clazz, "consumed", "I");
    finishedID = (*env)->GetFieldID(env, clazz, "finished", "Z");
    makeExceptionID = (*env)->GetMethodID(env, clazz, "makeException", "(Ljava/lang/String;I)Lnet/md_5/bungee/jni/NativeCodeException;");
}

jint throwException(JNIEnv *env, const char* message, int err) {
    jstring jMessage = (*env)->NewStringUTF(env, message);
    jthrowable throwable = (jthrowable) (*env)->CallStaticObjectMethod(env, classID, makeExceptionID, jMessage, err);
    return (*env)->Throw(env, throwable);
}

JNIEXPORT jboolean JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_checkSupported(JNIEnv* env, jobject obj) {
	#if !defined(__aarch64__)
	return (jboolean) checkCompressionNativesSupport();
	#else
	return JNI_TRUE;
	#endif
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
    if (!stream) {
        throwOutOfMemoryError(env, "Failed to calloc new z_stream");
        return 0;
    }

    int ret = (compress) ? deflateInit(stream, level) : inflateInit(stream);

    if (ret != Z_OK) {
        free(stream);
        throwException(env, "Could not init z_stream", ret);
        return 0;
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
            (*env)->SetBooleanField(env, obj, finishedID, JNI_TRUE);
            break;
        case Z_OK:
            break;
        default:
            throwException(env, "Unknown z_stream return code", ret);
            return -1;
    }

    (*env)->SetIntField(env, obj, consumedID, inLength - stream->avail_in);

    return outLength - stream->avail_out;
}
