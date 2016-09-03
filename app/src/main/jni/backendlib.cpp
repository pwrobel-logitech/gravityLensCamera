#include "backend.h"


JNIEXPORT jdouble JNICALL Java_com_pwrobel_darkcam1_NativeCPUBackend_internal_1numerical_1test
  (JNIEnv *, jobject, jdouble){
    return 6.0 * 4.5;
  };


JNIEXPORT void JNICALL Java_com_pwrobel_darkcam1_NativeCPUBackend_process_1buffer
  (JNIEnv *g_vm, jobject, jintArray, jintArray, jint, jint, jdouble, jdouble, jint)
  {

  };