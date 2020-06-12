#!/bin/bash

docker run --rm -v $(pwd):/working oracle/graalvm-ce:20.1.0-java11 \
    /bin/bash -c "
                    gu install native-image; \
                    native-image --enable-url-protocols=http,https \
                      -H:ReflectionConfigurationFiles=/working/src/main/resources/reflect.json \
                      -H:+ReportUnsupportedElementsAtRuntime --no-server -jar \"/working/build/libs/S3Graalvm-all.jar\" \
                    ; \
                    cp S3Graalvm-all /working/build/graalvm/server"

mkdir -p build/graalvm
if [ ! -f "build/graalvm/server" ]; then
    echo "there was an error building graalvm image"
    exit 1
fi
