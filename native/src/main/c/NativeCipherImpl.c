#include "net_md_5_bungee_NativeCipherImpl.h"
#include <openssl/aes.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define BYTE unsigned char

jlong Java_net_md_15_bungee_NativeCipherImpl_initKey
(JNIEnv* env, jobject obj, jbyteArray key)
{
    AES_KEY *aes_key = malloc(sizeof(AES_KEY));

    jboolean isFieldCopy;
    BYTE *key_bytes = (BYTE*)(*env)->GetByteArrayElements(env, key, &isFieldCopy);
    int key_length = (*env)->GetArrayLength(env, key) * 8; // in bits

    AES_set_encrypt_key(key_bytes, key_length, aes_key);

    if (isFieldCopy) {
        (*env)->ReleaseByteArrayElements(env, key, (jbyte*)key_bytes, JNI_ABORT);
    }

    return (long) aes_key;
}
jlong Java_net_md_15_bungee_NativeCipherImpl_initIV
(JNIEnv* env, jobject obj, jbyteArray iv)
{
    jboolean isFieldCopy;
    BYTE *iv_bytes = (BYTE*)(*env)->GetByteArrayElements(env, iv, &isFieldCopy);
    int iv_length = (*env)->GetArrayLength(env, iv);

    BYTE* jni_iv = malloc(iv_length);

    memcpy(jni_iv, iv_bytes, iv_length);

    if (isFieldCopy) {
        (*env)->ReleaseByteArrayElements(env, iv, (jbyte*)iv_bytes, JNI_ABORT);
    }

    return (long) jni_iv;
}
void Java_net_md_15_bungee_NativeCipherImpl_free
(JNIEnv* env, jobject obj, jlong key, jlong iv)
{
    free((AES_KEY*)key);
    free((BYTE*)iv);
}
void Java_net_md_15_bungee_NativeCipherImpl_cipher
(JNIEnv* env, jobject obj, jboolean forEncryption, jlong key, jlong iv, jlong in, jlong out, jint length)
{
    AES_KEY *aes_key = (AES_KEY*)key;
    size_t buffer_length = (size_t) length;

    BYTE *input = (BYTE*) in;
    BYTE *output = (BYTE*) out;
    BYTE* jni_iv = (BYTE*) iv;

    AES_cfb8_encrypt(
      input,                                     // input buffer
      output,                                    // output buffer
      buffer_length,                             // readable bytes
      aes_key,                                   // encryption key
      jni_iv,                                    // IV
      NULL,                                      // not needed
      forEncryption ? AES_ENCRYPT : AES_DECRYPT  // encryption mode
    );
}
