#include <stdlib.h>
#include <string.h>

#include <mbedtls/aes.h>
#include "shared.h"
#include "net_md_5_bungee_jni_cipher_NativeCipherImpl.h"

typedef unsigned char byte;

typedef struct crypto_context {
    int mode;
    mbedtls_aes_context cipher;
    byte key[];
} crypto_context;

jlong JNICALL Java_net_md_15_bungee_jni_cipher_NativeCipherImpl_init(JNIEnv* env, jobject obj, jboolean forEncryption, jbyteArray key) {
    jsize keyLen = (*env)->GetArrayLength(env, key);

    crypto_context *crypto = (crypto_context*) malloc(sizeof (crypto_context) + (size_t) keyLen);
    if (!crypto) {
        throwOutOfMemoryError(env, "Failed to malloc new crypto_context");
        return 0;
    }

    (*env)->GetByteArrayRegion(env, key, 0, keyLen, (jbyte*) &crypto->key);

    mbedtls_aes_init(&crypto->cipher);
    mbedtls_aes_setkey_enc(&crypto->cipher, (byte*) &crypto->key, keyLen * 8);

    crypto->mode = (forEncryption) ? MBEDTLS_AES_ENCRYPT : MBEDTLS_AES_DECRYPT;

    return (jlong) crypto;
}

void Java_net_md_15_bungee_jni_cipher_NativeCipherImpl_free(JNIEnv* env, jobject obj, jlong ctx) {
    crypto_context *crypto = (crypto_context*) ctx;

    mbedtls_aes_free(&crypto->cipher);
    free(crypto);
}

void Java_net_md_15_bungee_jni_cipher_NativeCipherImpl_cipher(JNIEnv* env, jobject obj, jlong ctx, jlong in, jlong out, jint length) {
    crypto_context *crypto = (crypto_context*) ctx;

    mbedtls_aes_crypt_cfb8(&crypto->cipher, crypto->mode, length, crypto->key, (byte*) in, (byte*) out);
}
