#include "texture_frame_uploader.h"

#define LOG_TAG "TextureFrameUploader"

TextureFrameUploader::TextureFrameUploader() :
		_msg(MSG_NONE), copyTexSurface(0) {
	LOGI("TextureFrameUploader instance created");
	eglCore = NULL;
	textureFrame = NULL;
	outputTexId = -1;
	isInitial = false;
	vertexCoords = new GLfloat[OPENGL_VERTEX_COORDNATE_CNT];
	textureCoords = new GLfloat[OPENGL_VERTEX_COORDNATE_CNT];
	pthread_mutex_init(&mLock, NULL);
	pthread_cond_init(&mCondition, NULL);
}

TextureFrameUploader::~TextureFrameUploader() {
	LOGI("TextureFrameUploader instance destroyed");
	pthread_mutex_destroy(&mLock);
	pthread_cond_destroy(&mCondition);
	delete[] vertexCoords;
	delete[] textureCoords;
}

/**
 * 开始渲染
 * @param videoWidth
 * @param videoHeight
 * @param degress
 * @return
 */
bool TextureFrameUploader::start(int videoWidth, int videoHeight, int degress) {
	LOGI("Creating TextureFrameUploader thread");
	this->videoWidth = videoWidth;
	this->videoHeight = videoHeight;
	memcpy(vertexCoords, DECODER_COPIER_GL_VERTEX_COORDS, sizeof(GLfloat) * OPENGL_VERTEX_COORDNATE_CNT);
	switch(degress){
	case 90:
		memcpy(textureCoords, DECODER_COPIER_GL_TEXTURE_COORDS_ROTATED_90, sizeof(GLfloat) * OPENGL_VERTEX_COORDNATE_CNT);
		break;
	case 180:
		memcpy(textureCoords, DECODER_COPIER_GL_TEXTURE_COORDS_ROTATED_180, sizeof(GLfloat) * OPENGL_VERTEX_COORDNATE_CNT);
		break;
	case 270:
		memcpy(textureCoords, DECODER_COPIER_GL_TEXTURE_COORDS_ROTATED_270, sizeof(GLfloat) * OPENGL_VERTEX_COORDNATE_CNT);
		break;
	default:
		memcpy(textureCoords, DECODER_COPIER_GL_TEXTURE_COORDS_NO_ROTATION, sizeof(GLfloat) * OPENGL_VERTEX_COORDNATE_CNT);
		break;
	}

	//在线程中进行初始化上下文工作
	_msg = MSG_WINDOW_SET;
	pthread_create(&_threadId, 0, threadStartCallback, this);
	return true;
}

/**
 * 有新的数据
 */
void TextureFrameUploader::signalFrameAvailable() {
//	LOGI("enter TextureFrameUploader::signalFrameAvailable");
	while(!isInitial || _msg == MSG_WINDOW_SET || NULL == eglCore){
		usleep(100 * 1000);
	}
	pthread_mutex_lock(&mLock);
	pthread_cond_signal(&mCondition);
	pthread_mutex_unlock(&mLock);
}

/**
 * 停止渲染
 */
void TextureFrameUploader::stop() {
	LOGI("Stopping TextureFrameUploader Render thread");
	/*send message to render thread to stop rendering*/
	pthread_mutex_lock(&mLock);
	_msg = MSG_RENDER_LOOP_EXIT;
	pthread_cond_signal(&mCondition);
	pthread_mutex_unlock(&mLock);

	pthread_join(_threadId, 0); // 等待_threadId线程退出
	LOGI("TextureFrameUploader Render thread stopped");
}

void* TextureFrameUploader::threadStartCallback(void *myself) {
	TextureFrameUploader *processor = (TextureFrameUploader*) myself;
	processor->renderLoop(); // 开启循环渲染
	pthread_exit(0);
	return 0;
}

/**
 * 循环开始处理渲染帧
 */
void TextureFrameUploader::renderLoop() {
	bool renderingEnabled = true;
	LOGI("renderLoop()");
	while (renderingEnabled) {
		pthread_mutex_lock(&mLock);
		switch (_msg) {
			case MSG_WINDOW_SET:
				LOGI("receive msg MSG_WINDOW_SET");
				isInitial = initialize();
				break;
			case MSG_RENDER_LOOP_EXIT:
				LOGI("receive msg MSG_RENDER_LOOP_EXIT");
				renderingEnabled = false;
				destroy();
				break;
			default:
				break;
		}
		_msg = MSG_NONE;
		if (NULL != eglCore) {
			this->signalDecodeThread();
			pthread_cond_wait(&mCondition, &mLock);
			LOGI("zhf-debug: drawFrame+++++");
			eglCore->makeCurrent(copyTexSurface);
			this->drawFrame(); // 有新的数据，就会通知condition，然后开始draw一帧
		}
		pthread_mutex_unlock(&mLock);
	}
	LOGI("Render loop exits");
	return;
}

bool TextureFrameUploader::initialize() {
	eglCore = new EGLCore();
	LOGI("TextureFrameUploader use sharecontext");
	eglCore->initWithSharedContext(); // 加载着色器
	LOGI("after TextureFrameUploader use sharecontext");

	// 这里创建的是离屏的surface??
	copyTexSurface = eglCore->createOffscreenSurface(videoWidth, videoHeight);
	eglCore->makeCurrent(copyTexSurface);
	glGenFramebuffers(1, &mFBO); // 生成一个FBO
	//初始化outputTexId
	glGenTextures(1, &outputTexId); // 生成一个普通的纹理
	glBindTexture(GL_TEXTURE_2D, outputTexId);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	// 这是一个普通的RGB纹理
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, videoWidth, videoHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
	glBindTexture(GL_TEXTURE_2D, 0);

	if (mUploaderCallback){
		mUploaderCallback->initFromUploaderGLContext(eglCore); // 回调通知
	}

	eglCore->makeCurrent(copyTexSurface);
	LOGI("leave TextureFrameUploader::initialize");

	return true;
}

void TextureFrameUploader::destroy() {
	LOGI("dealloc eglCore ...");
	if (mUploaderCallback)
		mUploaderCallback->destroyFromUploaderGLContext();

	eglCore->makeCurrent(copyTexSurface);
	if(-1 != outputTexId){
		glDeleteTextures(1, &outputTexId); // 释放textureID
	}
	if (mFBO) {
		glBindFramebuffer(GL_FRAMEBUFFER, 0); // 释放FBO
		glDeleteFramebuffers(1, &mFBO);
	}
	eglCore->releaseSurface(copyTexSurface);
	eglCore->release();
	delete eglCore;
	eglCore = NULL;
}

void TextureFrameUploader::registerUpdateTexImageCallback(float (*update_tex_image_callback)(TextureFrame* textureFrame, void *context), void (*signal_decode_thread_callback)(void *context), void* context) {
	this->updateTexImageCallback = update_tex_image_callback;
	this->signalDecodeThreadCallback = signal_decode_thread_callback;
	this->updateTexImageContext = context;
}

void TextureFrameUploader::signalDecodeThread() {
	signalDecodeThreadCallback(updateTexImageContext);
}

float TextureFrameUploader::updateTexImage() {
	//调用MediaCodec中的Surface中的SurfaceTexture更新纹理并且把时间传回来xx.xxx单位秒
	//或者调用自己写的软件解码的YUVFrame来更新纹理 传回时间来
	return updateTexImageCallback(textureFrame, updateTexImageContext);
}

/**
 * 绘制视频帧，每一帧都会触发
 */
void TextureFrameUploader::drawFrame() {
	float position = this->updateTexImage(); // 这里调用updateTexImage，会更新textureFrame，得到帧数据后通过glteximage2d来绘制到3个YUV texture上，最后在绘制到到FBO上

	glBindFramebuffer(GL_FRAMEBUFFER, mFBO);
	/** 将YUV数据(软件解码), samplerExternalOES格式的TexId(硬件解码) 拷贝到GL_RGBA格式的纹理ID上 **/
	textureFrameCopier->renderWithCoords(textureFrame, outputTexId, vertexCoords, textureCoords); // 最后在绘制到到FBO上
	if (mUploaderCallback) // 因为绘制到FBO，然后我们需要通过这个FBO绑定的textureID，也就是这个outputTexId进行传递
		mUploaderCallback->processVideoFrame(outputTexId, videoWidth, videoHeight, position);
	else
		LOGE("TextureFrameUploader::mUploaderCallback is NULL");

	glBindFramebuffer(GL_FRAMEBUFFER, 0);
}
