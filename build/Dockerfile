FROM openjdk:17

COPY *.jar /weChatMp.jar

# set environment
ENV JVM_XMS="1g" \
    JVM_XMX="1g" \
    JVM_XMN="512m" \
    JVM_MS="128m" \
    JVM_MMS="320m" \
    TIME_ZONE="Asia/Shanghai" \
    JAR_NAME="weChatMp-1.0" \
    SERVER_NAME="weChatMp"

ENTRYPOINT ["java","-jar","/weChatMp.jar"]
