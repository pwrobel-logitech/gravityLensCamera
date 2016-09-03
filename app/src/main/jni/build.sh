
APP_BUILD_SCRIPT=Android.mk
NDK_DIR=/home/pwrobel/ndk/android-ndk-r12b
SDK_DIR=/home/pwrobel/Android/Sdk

rm -rf ../obj
rm -rf ../libs
mkdir -p ../jniLibs

ant -Dsdk.dir=$SDK_DIR -Dndk.dir=$NDK_DIR release

cp -r ../libs/* ../jniLibs