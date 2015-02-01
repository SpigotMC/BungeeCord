#include <zlib.h>
#include <stdlib.h>
#include <string>
#include "net_md_5_bungee_jni_zlib_NativeCompressImpl.h"

typedef unsigned char byte;

jint throwException(JNIEnv *env, std::string message) {
    jclass exClass = env->FindClass("java/lang/RuntimeException");
    if (exClass == NULL) {
        return -1;
    }

    return env->ThrowNew(exClass, message.c_str());
}

static jfieldID consumedID;
static jfieldID finishedID;

void JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_initFields(JNIEnv* env, jclass clazz) {
    consumedID = env->GetFieldID(clazz, "consumed", "I");
    finishedID = env->GetFieldID(clazz, "finished", "Z");
}

void JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_reset(JNIEnv* env, jobject obj, jlong ctx, jboolean compress) {
    z_stream* stream = (z_stream*) ctx;
    int ret = (compress) ? deflateReset(stream) : inflateReset(stream);

    if (ret != Z_OK) {
        throwException(env, "test");
    }
}

void JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_end(JNIEnv* env, jobject obj, jlong ctx, jboolean compress) {
    z_stream* stream = (z_stream*) ctx;
    int ret = (compress) ? deflateEnd(stream) : inflateEnd(stream);

    free(stream);

    if (ret != Z_OK) {
        // TODO
    }
}

jlong JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_init(JNIEnv* env, jobject obj, jboolean compress, jint level) {
    z_stream* stream = (z_stream*) calloc(1, sizeof (z_stream));
    int ret = (compress) ? deflateInit(stream, level) : inflateInit(stream);

    if (ret != Z_OK) {
        // TODO
    }

    return (jlong) stream;
}

jint JNICALL Java_net_md_15_bungee_jni_zlib_NativeCompressImpl_process(JNIEnv* env, jobject obj, jlong ctx, jlong in, jint inLength, jlong out, jint outLength, jboolean compress) {
    z_stream* stream = (z_stream*) ctx;

    stream->avail_in = inLength;
    stream->next_in = (byte*) in;

    stream->avail_out = outLength;
    stream->next_out = (byte*) out;

    // TODO: Flush?
    int ret = (compress) ? deflate(stream, Z_FINISH) : inflate(stream, Z_PARTIAL_FLUSH);

    switch (ret) {
        case Z_STREAM_END:
            env->SetBooleanField(obj, finishedID, true);
            break;
        case Z_OK:
            break;
        default:
            throwException(env, "bar " + std::to_string(ret));
    }

    env->SetIntField(obj, consumedID, inLength - stream->avail_in);

    return outLength - stream->avail_out;
}
