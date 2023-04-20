FROM alpine:3.17.3

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apk/repositories

RUN apk update && apk add curl

COPY ./init.sh ./data/init.sh
