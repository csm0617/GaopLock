#!/bin/bash
request_url='192.168.1.35:8088/lock/isBootable/'$_SERVICE_NAME
result=`curl -s -m 5 $request_url `
echo $_SERVICE_NAME
echo $result

if [ -z $result  ]; then
  result=9528
fi
x=9527
while [ $result -ne $x ]
do
  echo '未允许启动，等待下一次请求'
  sleep 5
  result=`curl  -s -m 5 $request_url `

  echo '再次请求后响应为:'$result
  if [ -z $result  ]; then
    result=9528
  fi
done

if [ $result == $x ]; then
  echo '可以启动，正常退出'
  exit 0
else
  echo '未允许启动，强制关闭'
  exit 1
fi


192.168.1.171:1080/system_containers/init:0.1
192.168.1.171:1080/system_containers/isboot:0.1