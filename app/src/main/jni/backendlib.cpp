#include "backend.h"
#include "math.h"

#include <pthread.h>

JNIEXPORT jdouble JNICALL Java_com_pwrobel_darkcam1_NativeCPUBackend_internal_1numerical_1test
  (JNIEnv *, jobject, jdouble in){
    return M_PI + sin(in);
  };


JNIEXPORT void JNICALL Java_com_pwrobel_darkcam1_NativeCPUBackend_process_1buffer
  (JNIEnv *env, jobject, jintArray jinput, jintArray joutput, jint w, jint h, jdouble phys_ratio, jdouble fovX_deg, jint nThr)
  {
    const jint *in_buff = env->GetIntArrayElements(jinput , 0);
    jint *out_buff = env->GetIntArrayElements(joutput , 0);



    env->ReleaseIntArrayElements(jinput, (jint*)in_buff, 0);
    env->ReleaseIntArrayElements(joutput, out_buff, 0);
  };