#include <openssl/evp.h>
#include "net_md_5_bungee_NativeCipherImpl.h"

typedef unsigned char byte;

jlong JNICALL Java_net_md_15_bungee_NativeCipherImpl_init(JNIEnv* env, jobject obj, jboolean forEncryption, jbyteArray key) {
    jbyte *keyBytes = env->GetByteArrayElements(key, NULL);

    EVP_CIPHER_CTX *cipherCtx = EVP_CIPHER_CTX_new();
    EVP_CipherInit(cipherCtx, EVP_aes_128_cfb8(), (byte*) keyBytes, (byte*) keyBytes, forEncryption);

    env->ReleaseByteArrayElements(key, keyBytes, JNI_ABORT);
    return (jlong) cipherCtx;
}

void Java_net_md_15_bungee_NativeCipherImpl_free(JNIEnv* env, jobject obj, jlong ctx) {
    EVP_CIPHER_CTX_free((EVP_CIPHER_CTX*) ctx);
}

void Java_net_md_15_bungee_NativeCipherImpl_cipher(JNIEnv* env, jobject obj, jlong ctx, jlong in, jlong out, jint length) {
    EVP_CipherUpdate((EVP_CIPHER_CTX*) ctx, (byte*) out, &length, (byte*) in, length);
}
