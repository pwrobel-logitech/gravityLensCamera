#include "backend.h"
#include "math.h"

#include <pthread.h>
#include <android/log.h>

#define APPNAME "darkcam native:"

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

void lowest_priority(){
    int policy;
    struct sched_param param;

    pthread_getschedparam(pthread_self(), &policy, &param);
    param.sched_priority = sched_get_priority_min(policy);
    pthread_setschedparam(pthread_self(), policy, &param);
}

inline float clamp(float a, float low, float high){
    if(a < low)
        return low;
    if(a > high)
        return high;
    return a;
}

//imitate pixel shader in software
void process_pixel(int x, int y, int w, int h, float phys_ratio, float fovX_deg,
                   const jint* in, jint* out){
    float fov_yx_ratio = ((float)h)/((float)w);
    float fovX = fovX_deg * (M_PI/180.0) ;
    float fovY = fov_yx_ratio * fovX;

    float xnorm = ((float)x)/((float)w);
    float ynorm = ((float)y)/((float)h);

    float tx = tan((xnorm - 0.5) * fovX);
    float ty = tan((ynorm - 0.5) * fovY);
    float fi = atan(sqrt(tx*tx+ty*ty));

    int final_col;

    if(fi < phys_ratio){
        final_col = 0x00000000;
    }else{
        float coeff = 1.0 - phys_ratio * (1.0 / (fi * fi));
        float xn = clamp(0.5 + coeff*(xnorm-0.5),0.0,1.0);
        float yn = clamp(0.5 + coeff*(ynorm-0.5),0.0,1.0);
        int readcoord = ((int)(xn*w)) + w * ((int)(yn*h));
        if(readcoord < 0)
            readcoord = 0;
        if(readcoord >= w*h)
            readcoord = w*h - 1;
        final_col = in[readcoord];
    }
    out[x + y * w] = final_col;// & 0xffff00ff;
}

struct subimage_info{
    int div_x;
    int div_y;
    int num_x;
    int num_y;
    float phys_ratio;
    float fovX_deg;
    int w;
    int h;
    jint *in_buf;
    jint *out_buf;
};

void *perform_subimage_transform(void *args) {
    lowest_priority();
    subimage_info* info = (subimage_info*)args;
    int loop_x_start = (int)(((float)info->num_x/(float)info->div_x)*info->w);
    int loop_x_end = (int)(((float)(info->num_x+1)/(float)info->div_x)*info->w);
    int loop_y_start = (int)(((float)info->num_y/(float)info->div_y)*info->h);
    int loop_y_end = (int)(((float)(info->num_y+1)/(float)info->div_y)*info->h);
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "raaaatio %f, rx %d, ry %d, loopingx: q%d,%dq, loopingy: q%d,%dq", info->phys_ratio,
    info->num_x, info->num_y, loop_x_start,loop_x_end,
                        loop_y_start,loop_y_end);
    for (int j = loop_y_start; j < loop_y_end; j++) //y
        for (int i = loop_x_start; i < loop_x_end; i++){ //x
            process_pixel(i, j, info->w, info->h,
                          info->phys_ratio, info->fovX_deg,
                          (const jint*)info->in_buf, info->out_buf);
        }
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

    lowest_priority();

    if(nThr == 1){
        pthread_t tid;
        void *status;
        subimage_info nfo;
        nfo.div_x = 1;//how many divisions on x direction
        nfo.div_y = 1;
        nfo.num_x = 0;//which xy subrectange does this thread process
        nfo.num_y = 0;
        nfo.w = w;
        nfo.h = h;
        nfo.phys_ratio = (float)phys_ratio;
        nfo.fovX_deg = (float)fovX_deg;
        nfo.in_buf = (jint*)in_buff;
        nfo.out_buf = out_buff;

        pthread_create(&tid, NULL, perform_subimage_transform, (void*)&nfo);
        pthread_join(tid, &status);
    } else if(nThr == 2){
        pthread_t tid1;
        void *status1;
        subimage_info nfo1;
        nfo1.div_x = 1;//how many divisions on x direction
        nfo1.div_y = 2;
        nfo1.num_x = 0;//which xy subrectange does this thread process
        nfo1.num_y = 0;
        nfo1.w = w;
        nfo1.h = h;
        nfo1.phys_ratio = phys_ratio;
        nfo1.fovX_deg = fovX_deg;
        nfo1.in_buf = (jint*)in_buff;
        nfo1.out_buf = out_buff;

        pthread_t tid2;
        void *status2;
        subimage_info nfo2;
        nfo2.div_x = 1;//how many divisions on x direction
        nfo2.div_y = 2;
        nfo2.num_x = 0;//which xy subrectange does this thread process
        nfo2.num_y = 1;
        nfo2.w = w;
        nfo2.h = h;
        nfo2.phys_ratio = phys_ratio;
        nfo2.fovX_deg = fovX_deg;
        nfo2.in_buf = (jint*)in_buff;
        nfo2.out_buf = out_buff;

        pthread_create(&tid1, NULL, perform_subimage_transform, (void*)&nfo1);
        pthread_create(&tid2, NULL, perform_subimage_transform, (void*)&nfo2);
        pthread_join(tid1, &status1);
        pthread_join(tid2, &status2);
    }else if(nThr >= 4){
        pthread_t tid1;
        void *status1;
        subimage_info nfo1;
        nfo1.div_x = 1;//how many divisions on x direction
        nfo1.div_y = 4;
        nfo1.num_x = 0;//which xy subrectange does this thread process
        nfo1.num_y = 0;
        nfo1.w = w;
        nfo1.h = h;
        nfo1.phys_ratio = phys_ratio;
        nfo1.fovX_deg = fovX_deg;
        nfo1.in_buf = (jint*)in_buff;
        nfo1.out_buf = out_buff;

        pthread_t tid2;
        void *status2;
        subimage_info nfo2;
        nfo2.div_x = 1;//how many divisions on x direction
        nfo2.div_y = 4;
        nfo2.num_x = 0;//which xy subrectange does this thread process
        nfo2.num_y = 1;
        nfo2.w = w;
        nfo2.h = h;
        nfo2.phys_ratio = phys_ratio;
        nfo2.fovX_deg = fovX_deg;
        nfo2.in_buf = (jint*)in_buff;
        nfo2.out_buf = out_buff;

        pthread_t tid3;
        void *status3;
        subimage_info nfo3;
        nfo3.div_x = 1;//how many divisions on x direction
        nfo3.div_y = 4;
        nfo3.num_x = 0;//which xy subrectange does this thread process
        nfo3.num_y = 2;
        nfo3.w = w;
        nfo3.h = h;
        nfo3.phys_ratio = phys_ratio;
        nfo3.fovX_deg = fovX_deg;
        nfo3.in_buf = (jint*)in_buff;
        nfo3.out_buf = out_buff;

        pthread_t tid4;
        void *status4;
        subimage_info nfo4;
        nfo4.div_x = 1;//how many divisions on x direction
        nfo4.div_y = 4;
        nfo4.num_x = 0;//which xy subrectange does this thread process
        nfo4.num_y = 3;
        nfo4.w = w;
        nfo4.h = h;
        nfo4.phys_ratio = phys_ratio;
        nfo4.fovX_deg = fovX_deg;
        nfo4.in_buf = (jint*)in_buff;
        nfo4.out_buf = out_buff;

        pthread_create(&tid1, NULL, perform_subimage_transform, (void*)&nfo1);
        pthread_create(&tid2, NULL, perform_subimage_transform, (void*)&nfo2);
        pthread_create(&tid3, NULL, perform_subimage_transform, (void*)&nfo3);
        pthread_create(&tid4, NULL, perform_subimage_transform, (void*)&nfo4);
        pthread_join(tid1, &status1);
        pthread_join(tid2, &status2);
        pthread_join(tid3, &status3);
        pthread_join(tid4, &status4);
    }

    env->ReleaseIntArrayElements(jinput, (jint*)in_buff, 0);
    env->ReleaseIntArrayElements(joutput, out_buff, 0);
};