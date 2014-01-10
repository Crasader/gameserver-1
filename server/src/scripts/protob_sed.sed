#!/bin/sh
1, /message/{
#Insert the java package 
s/\/\/package.*;/package com.xinqihd.sns.gameserver.proto;/
#Output java class name to "Xinqi"
s/^message \([a-zA-Z0-9]*\).*{/option java_outer_classname = "Xinqi\1"; \
message \1 {/
#Output java class name to "Xinqi"
s/^message \([a-zA-Z0-9]*\)[^{]*$/option java_outer_classname = "Xinqi\1"; \
message \1 /
#Replace the following three proto since there are only embeded in other proto
s/BseUserData/UserData/
s/BseUserExData/UserExData/
s/BseUserInfo/UserInfo/
}
s/XinqiBuyInfo/XinqiBceBuyProp/
s/XinqiLengthenIndate/XinqiBceLengthenIndate/
s/XinqiTaskProtoInfo/XinqiBseTaskList/
#Replace the following three proto since there are only embeded in other proto
s/BseUserData/UserData/
s/BseUserExData/UserExData/
s/BseUserInfo/UserInfo/
