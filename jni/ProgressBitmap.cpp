#include <jni.h>
#include <android/bitmap.h>
const char *className = "com/example/progressbitmap/MainActivity";
#include<android/log.h>
#define LOG_TAG "AndroidJni"
#define LOG(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__);


void progress(JNIEnv *env,jobject *thiz,jintArray colors,jfloat p,jobject jbitmap)
{
	AndroidBitmapInfo info;
	AndroidBitmap_getInfo(env,jbitmap,&info);
	int w = info.width;
	int h = info.height;
	int start = (h - h * p) * w;
	int *pixels ;
	jboolean flag = JNI_FALSE;
	jint *cl = (*env).GetIntArrayElements(colors,&flag);
	if(AndroidBitmap_lockPixels(env,jbitmap,(void**)&pixels) < 0)
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"error");
	else
		LOG("----------------------------------------------------");
	int size = h * w;
	LOG("VALUE:%d,total:%d", start,w*h);
	for(int i=start;i<size;i++)
	{
		cl[i] = (pixels[i] & 0xFF000000) | ((pixels[i]&0xFF) << 16) | ((pixels[i] >> 16) & 0xFF) | (pixels[i] & 0xFF00);
	}
	AndroidBitmap_unlockPixels(env,jbitmap);
	env->ReleaseIntArrayElements(colors,cl,0);
}

void gray(JNIEnv *env,jobject *thiz,jintArray colors,jint len)
{
	jboolean FLAG = JNI_FALSE;
	jint *cl  =env->GetIntArrayElements(colors, &FLAG);
	for(int i=0;i<len;i++)
	{
		jint average = (((cl[i] >> 16) & 0xFF) + ((cl[i] >> 8) & 0xFF) + (cl[i] & 0xFF))/3;

		cl[i] = average | (average << 8) | (average << 16) | (cl[i] & 0xFF000000);
	}
	env->ReleaseIntArrayElements(colors,cl,0);
}
JNINativeMethod methods[] = { "progress", "([IFLandroid/graphics/Bitmap;)V", (void*)progress,
							 "gray",      "([II)V", (void*)gray};
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv *env;
	vm->GetEnv((void**) &env, JNI_VERSION_1_4);
	jclass jclazz = env->FindClass(className);
	env->RegisterNatives(jclazz,methods,2);
	return JNI_VERSION_1_4;
}
