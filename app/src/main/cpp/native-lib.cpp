#include <jni.h>
#include <string>

extern "C"
JNIEXPORT void JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_onSurfaceCreated(JNIEnv *env, jobject thiz,
                                                                    jobject surface) {
    // TODO: implement onSurfaceCreated()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_onSurfaceDestroyed(JNIEnv *env, jobject thiz,
                                                                      jobject surface) {
    // TODO: implement onSurfaceDestroyed()
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_prepare(JNIEnv *env, jobject thiz,
                                                           jstring src_filename_param,
                                                           jintArray max_analyze_durations,
                                                           jint size, jint probesize,
                                                           jboolean fps_probesize_configured,
                                                           jfloat min_buffered_duration,
                                                           jfloat max_buffered_duration, jint width,
                                                           jint height, jobject surface) {
    // TODO: implement prepare()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_pause(JNIEnv *env, jobject thiz) {
    // TODO: implement pause()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_play(JNIEnv *env, jobject thiz) {
    // TODO: implement play()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_stop(JNIEnv *env, jobject thiz) {
    // TODO: implement stop()
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_getBufferedProgress(JNIEnv *env, jobject thiz) {
    // TODO: implement getBufferedProgress()
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_getPlayProgress(JNIEnv *env, jobject thiz) {
    // TODO: implement getPlayProgress()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_seekToPosition(JNIEnv *env, jobject thiz,
                                                                  jfloat position) {
    // TODO: implement seekToPosition()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_seekCurrent(JNIEnv *env, jobject thiz,
                                                               jfloat position) {
    // TODO: implement seekCurrent()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_beforeSeekCurrent(JNIEnv *env, jobject thiz) {
    // TODO: implement beforeSeekCurrent()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_afterSeekCurrent(JNIEnv *env, jobject thiz) {
    // TODO: implement afterSeekCurrent()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ryan_playerffmpeg_player_PlayerController_resetRenderSize(JNIEnv *env, jobject thiz,
                                                                   jint left, jint top, jint width,
                                                                   jint height) {
    // TODO: implement resetRenderSize()
}