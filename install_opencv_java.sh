#!/bin/bash
set -e

# 1. Install dependencies (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install -y build-essential cmake git ant default-jdk pkg-config unzip wget

# 2. Download OpenCV source
OPENCV_VERSION=4.7.0
wget -O opencv.zip https://github.com/opencv/opencv/archive/${OPENCV_VERSION}.zip
unzip -q opencv.zip
rm opencv.zip

# 3. Build OpenCV with Java
cd opencv-${OPENCV_VERSION}
mkdir -p build
cd build
cmake -DBUILD_SHARED_LIBS=ON -DBUILD_opencv_java=ON -DCMAKE_BUILD_TYPE=Release ..
make -j$(nproc)

# 4. Prepare output folders
cd ../..
mkdir -p libs/native

# 5. Copy jar and .so to your project
cp opencv-${OPENCV_VERSION}/build/bin/opencv-${OPENCV_VERSION}.jar libs/
cp opencv-${OPENCV_VERSION}/build/lib/libopencv_java${OPENCV_VERSION//./}.so libs/native/

echo "OpenCV Java build complete!"
echo "Jar: libs/opencv-${OPENCV_VERSION}.jar"
echo "Native: libs/native/libopencv_java${OPENCV_VERSION//./}.so"
