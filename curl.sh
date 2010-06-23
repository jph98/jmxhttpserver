#!/bin/bash

zabbixhostname=$1
zabbixport=$2
zabbixpath=$3

curl -XGET $zabbixhostname:$zabbixport/$zabbixpath -m 10
