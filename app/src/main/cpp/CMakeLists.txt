cmake_minimum_required(VERSION 3.4.1)

set(PATH_TO_NATIVE ${CMAKE_SOURCE_DIR})
set(PATH_TO_THIRDPARTY ${PATH_TO_NATIVE}/thirdparty)
set(PATH_TO_PRE_BUILT ${PATH_TO_THIRDPARTY}/prebuild/${ANDROID_ABI})
message(${PATH_TO_PRE_BUILT})  # app/src/main/cpp

add_definitions("
-DGL_GLEXT_PROTOTYPES
-DEGL_EGLEXT_PROTOTYPES
")

include_directories(BEFORE ${PATH_TO_THIRDPARTY}/ffmpeg/include/)
include_directories(${PATH_TO_NATIVE}/)
include_directories(${PATH_TO_NATIVE}/common/)
include_directories(${PATH_TO_NATIVE}/video_player/)


# GLOB 会产生一个由所有匹配globbing表达式的文件组成的列表，并将其保存到变量中
file(GLOB FILES_NATIVE_ROOT "${PATH_TO_NATIVE}/PlayerControllerNative.cpp")

file(GLOB FILES_LIB_COMMON "${PATH_TO_NATIVE}/common/*.cpp")
file(GLOB FILES_LIB_COMMON_EGL_CORE "${PATH_TO_NATIVE}/common/egl_core/*.cpp")
file(GLOB FILES_LIB_COMMON_EGL_CORE "${PATH_TO_NATIVE}/common/egl_core/*.cpp")
file(GLOB FILES_LIB_COMMON_RESAMPLER "${PATH_TO_NATIVE}/common/libresampler/*.cpp")
file(GLOB FILES_LIB_COMMON_MSG_Q "${PATH_TO_NATIVE}/common/message_queue/*.cpp")
file(GLOB FILES_LIB_COMMON_OPENGL_MEDIA "${PATH_TO_NATIVE}/common/opengl_media/*.cpp")
file(GLOB FILES_LIB_COMMON_OPENGL_MEDIA_RENDER "${PATH_TO_NATIVE}/common/opengl_media/render/*.cpp")
file(GLOB FILES_LIB_COMMON_OPENGL_MEDIA_TEXTURE "${PATH_TO_NATIVE}/common/opengl_media/texture/*.cpp")
file(GLOB FILES_LIB_COMMON_OPENGL_MEDIA_TEX_COPIER "${PATH_TO_NATIVE}/common/opengl_media/texture_copier/*.cpp")
file(GLOB FILES_LIB_COMMON_OPENSL "${PATH_TO_NATIVE}/common/opensl_media/*.cpp")

file(GLOB FILES_LIB_VIDEO_PLAYER "${PATH_TO_NATIVE}/video_player/*.cpp")
file(GLOB FILES_LIB_VIDEO_PLAYER_COMMON "${PATH_TO_NATIVE}/video_player/common/*.cpp")
file(GLOB FILES_LIB_VIDEO_PLAYER_DECODER "${PATH_TO_NATIVE}/video_player/decoder/*.cpp")
file(GLOB FILES_LIB_VIDEO_PLAYER_SYNC "${PATH_TO_NATIVE}/video_player/sync/*.cpp")
file(GLOB FILES_LIB_VIDEO_PLAYER_TEX_UPLOADER "${PATH_TO_NATIVE}/video_player/texture_uploader/*.cpp")

add_library(native-lib
        SHARED
        ${FILES_NATIVE_ROOT}
        ${FILES_LIB_COMMON}
        ${FILES_LIB_COMMON_EGL_CORE}
        ${FILES_LIB_COMMON_MSG_Q}
        ${FILES_LIB_COMMON_RESAMPLER}
        ${FILES_LIB_COMMON_OPENSL}
        ${FILES_LIB_COMMON_OPENGL_MEDIA}
        ${FILES_LIB_COMMON_OPENGL_MEDIA_RENDER}
        ${FILES_LIB_COMMON_OPENGL_MEDIA_TEXTURE}
        ${FILES_LIB_COMMON_OPENGL_MEDIA_TEX_COPIER}
        ${FILES_LIB_VIDEO_PLAYER}
        ${FILES_LIB_VIDEO_PLAYER_COMMON}
        ${FILES_LIB_VIDEO_PLAYER_DECODER}
        ${FILES_LIB_VIDEO_PLAYER_SYNC}
        ${FILES_LIB_VIDEO_PLAYER_TEX_UPLOADER}
        )

target_link_libraries(
        native-lib
        # 引入系统的动态库
        log
        android
        GLESv2
        EGL
        z
        OpenSLES
        # 引入ffmpeg相关静态库
        ${PATH_TO_PRE_BUILT}/libavfilter.a
        ${PATH_TO_PRE_BUILT}/libavformat.a
        ${PATH_TO_PRE_BUILT}/libavcodec.a
        ${PATH_TO_PRE_BUILT}/libpostproc.a
        ${PATH_TO_PRE_BUILT}/libswresample.a
        ${PATH_TO_PRE_BUILT}/libswscale.a
        ${PATH_TO_PRE_BUILT}/libavutil.a
        ${PATH_TO_PRE_BUILT}/libpostproc.a
        ${PATH_TO_PRE_BUILT}/libfdk-aac.a
        ${PATH_TO_PRE_BUILT}/libvo-aacenc.a
        ${PATH_TO_PRE_BUILT}/libx264.a
)