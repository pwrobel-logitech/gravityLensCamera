#include "backend.h"
#include "math.h"

#include <pthread.h>


struct test_args{
  double in;
};

double test_result;

void *do_test(void *args) {
  test_args *myargs = (test_args*)args;
  double input = myargs->in;
  test_result = sin(input) + M_PI;
  return (void*) 0;
}



JNIEXPORT jdouble JNICALL Java_com_pwrobel_darkcam1_NativeCPUBackend_internal_1numerical_1test
  (JNIEnv *, jobject, jdouble in){
   pthread_t tid;
    void *status;
    test_args tst;
    tst.in = in;
    pthread_create(&tid, NULL, do_test, (void*)&tst);
    pthread_join(tid, &status);
    return test_result;
  };


JNIEXPORT void JNICALL Java_com_pwrobel_darkcam1_NativeCPUBackend_process_1buffer
  (JNIEnv *env, jobject, jintArray jinput, jintArray joutput, jint w, jint h, jdouble phys_ratio, jdouble fovX_deg, jint nThr)
  {
    const jint *in_buff = env->GetIntArrayElements(jinput , 0);
    jint *out_buff = env->GetIntArrayElements(joutput , 0);

    for (int j = 0; j < h; j++)
      for (int i = 0; i < w; i++){
        out_buff[i + j * w] = in_buff[i + j * w] & 0xff00ffff;
      }

    env->ReleaseIntArrayElements(jinput, (jint*)in_buff, 0);
    env->ReleaseIntArrayElements(joutput, out_buff, 0);
  };