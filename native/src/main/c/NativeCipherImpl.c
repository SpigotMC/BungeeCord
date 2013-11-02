#include "net_md_5_bungee_NativeCipherImpl.h"
#include <openssl/aes.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#define BYTE unsigned char

jlong Java_net_md_15_bungee_NativeCipherImpl_init
(JNIEnv* env, jobject obj, jbyteArray key)
{
    AES_KEY *aes_key = malloc(sizeof(AES_KEY));

    jboolean isKeyCopy;
    BYTE *key_bytes = (*env)->GetByteArrayElements(env, key, &isKeyCopy);
    int key_length = (*env)->GetArrayLength(env, key) * 8; // in bits

    AES_set_encrypt_key(key_bytes, key_length, aes_key);

    if (isKeyCopy) {
        (*env)->ReleaseByteArrayElements(env, key, key_bytes, JNI_ABORT);
    }
    return aes_key;
}
void Java_net_md_15_bungee_NativeCipherImpl_free
(JNIEnv* env, jobject obj, jlong key)
{
    free((AES_KEY*)key);
}
void Java_net_md_15_bungee_NativeCipherImpl_cipher
(JNIEnv* env, jobject obj, jboolean forEncryption, jlong key, jbyteArray iv, jlong in, jlong out, jint length)
{
    AES_KEY *aes_key = (AES_KEY*)key;
    jboolean isKeyCopy;

    size_t buffer_length = (size_t) length;

    BYTE *input =  (BYTE*) in;
    BYTE *output =  (BYTE*) out;

    jboolean isCopy;
    BYTE *iv_bytes = (*env)->GetByteArrayElements(env, iv, &isCopy);

    AES_cfb8_encrypt(
      input,                                     // input buffer
      output,                                    // output buffer
      buffer_length,                             // readable bytes
      aes_key,                                   // encryption key
      iv_bytes,                                  // IV
      NULL,                                      // not needed
      forEncryption ? AES_ENCRYPT : AES_DECRYPT  // encryption mode
    );

    // IV has changed, let's copy it back
    if (isCopy) {
      (*env)->ReleaseByteArrayElements(env, iv, (jbyte*)iv_bytes, 0);
    }
}
