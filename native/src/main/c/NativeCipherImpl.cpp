#include <stdlib.h>
#include <string.h>

#include <mbedtls/aes.h>
#include "net_md_5_bungee_jni_cipher_NativeCipherImpl.h"

// Support for CentOS 6
__asm__(".symver memcpy,memcpy@GLIBC_2.2.5");
extern "C" void *__wrap_memcpy(void *dest, const void *src, size_t n) {
    return memcpy(dest, src, n);
}

typedef unsigned char byte;

struct crypto_context {
    int mode;
    mbedtls_aes_context cipher;
    byte *key;
};

jlong JNICALL Java_net_md_15_bungee_jni_cipher_NativeCipherImpl_init(JNIEnv* env, jobject obj, jboolean forEncryption, jbyteArray key) {
    jsize keyLen = env->GetArrayLength(key);
    jbyte *keyBytes = env->GetByteArrayElements(key, NULL);

    crypto_context *crypto = (crypto_context*) malloc(sizeof (crypto_context));
    mbedtls_aes_init(&crypto->cipher);

    mbedtls_aes_setkey_enc(&crypto->cipher, (byte*) keyBytes, keyLen * 8);

    crypto->key = (byte*) malloc(keyLen);
    memcpy(crypto->key, keyBytes, keyLen);

    crypto->mode = (forEncryption) ? MBEDTLS_AES_ENCRYPT : MBEDTLS_AES_DECRYPT;

    env->ReleaseByteArrayElements(key, keyBytes, JNI_ABORT);
    return (jlong) crypto;
}

void Java_net_md_15_bungee_jni_cipher_NativeCipherImpl_free(JNIEnv* env, jobject obj, jlong ctx) {
    crypto_context *crypto = (crypto_context*) ctx;

    mbedtls_aes_free(&crypto->cipher);
    free(crypto->key);
    free(crypto);
}

void Java_net_md_15_bungee_jni_cipher_NativeCipherImpl_cipher(JNIEnv* env, jobject obj, jlong ctx, jlong in, jlong out, jint length) {
    crypto_context *crypto = (crypto_context*) ctx;

    mbedtls_aes_crypt_cfb8(&crypto->cipher, crypto->mode, length, crypto->key, (byte*) in, (byte*) out);
}
