-- Generated By protoc-gen-lua Do not Edit
local protobuf = require "protobuf"
module('BceRegister_pb', package.seeall)


local BCEREGISTER = protobuf.Descriptor();
local BCEREGISTER_USERNAME_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_ROLENAME_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_PASSWORD_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_EMAIL_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_GENDER_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_CLIENT_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_CHANNEL_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_COUNTRY_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_LOCX_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_LOCY_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_MAJORVERSION_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_MINORVERSION_FIELD = protobuf.FieldDescriptor();
local BCEREGISTER_TINYVERSION_FIELD = protobuf.FieldDescriptor();

BCEREGISTER_USERNAME_FIELD.name = "username"
BCEREGISTER_USERNAME_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.username"
BCEREGISTER_USERNAME_FIELD.number = 1
BCEREGISTER_USERNAME_FIELD.index = 0
BCEREGISTER_USERNAME_FIELD.label = 1
BCEREGISTER_USERNAME_FIELD.has_default_value = false
BCEREGISTER_USERNAME_FIELD.default_value = ""
BCEREGISTER_USERNAME_FIELD.type = 9
BCEREGISTER_USERNAME_FIELD.cpp_type = 9

BCEREGISTER_ROLENAME_FIELD.name = "rolename"
BCEREGISTER_ROLENAME_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.rolename"
BCEREGISTER_ROLENAME_FIELD.number = 2
BCEREGISTER_ROLENAME_FIELD.index = 1
BCEREGISTER_ROLENAME_FIELD.label = 1
BCEREGISTER_ROLENAME_FIELD.has_default_value = false
BCEREGISTER_ROLENAME_FIELD.default_value = ""
BCEREGISTER_ROLENAME_FIELD.type = 9
BCEREGISTER_ROLENAME_FIELD.cpp_type = 9

BCEREGISTER_PASSWORD_FIELD.name = "password"
BCEREGISTER_PASSWORD_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.password"
BCEREGISTER_PASSWORD_FIELD.number = 3
BCEREGISTER_PASSWORD_FIELD.index = 2
BCEREGISTER_PASSWORD_FIELD.label = 1
BCEREGISTER_PASSWORD_FIELD.has_default_value = false
BCEREGISTER_PASSWORD_FIELD.default_value = ""
BCEREGISTER_PASSWORD_FIELD.type = 9
BCEREGISTER_PASSWORD_FIELD.cpp_type = 9

BCEREGISTER_EMAIL_FIELD.name = "email"
BCEREGISTER_EMAIL_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.email"
BCEREGISTER_EMAIL_FIELD.number = 4
BCEREGISTER_EMAIL_FIELD.index = 3
BCEREGISTER_EMAIL_FIELD.label = 1
BCEREGISTER_EMAIL_FIELD.has_default_value = false
BCEREGISTER_EMAIL_FIELD.default_value = ""
BCEREGISTER_EMAIL_FIELD.type = 9
BCEREGISTER_EMAIL_FIELD.cpp_type = 9

BCEREGISTER_GENDER_FIELD.name = "gender"
BCEREGISTER_GENDER_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.gender"
BCEREGISTER_GENDER_FIELD.number = 5
BCEREGISTER_GENDER_FIELD.index = 4
BCEREGISTER_GENDER_FIELD.label = 1
BCEREGISTER_GENDER_FIELD.has_default_value = false
BCEREGISTER_GENDER_FIELD.default_value = 0
BCEREGISTER_GENDER_FIELD.type = 5
BCEREGISTER_GENDER_FIELD.cpp_type = 1

BCEREGISTER_CLIENT_FIELD.name = "client"
BCEREGISTER_CLIENT_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.client"
BCEREGISTER_CLIENT_FIELD.number = 6
BCEREGISTER_CLIENT_FIELD.index = 5
BCEREGISTER_CLIENT_FIELD.label = 1
BCEREGISTER_CLIENT_FIELD.has_default_value = false
BCEREGISTER_CLIENT_FIELD.default_value = ""
BCEREGISTER_CLIENT_FIELD.type = 9
BCEREGISTER_CLIENT_FIELD.cpp_type = 9

BCEREGISTER_CHANNEL_FIELD.name = "channel"
BCEREGISTER_CHANNEL_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.channel"
BCEREGISTER_CHANNEL_FIELD.number = 7
BCEREGISTER_CHANNEL_FIELD.index = 6
BCEREGISTER_CHANNEL_FIELD.label = 1
BCEREGISTER_CHANNEL_FIELD.has_default_value = false
BCEREGISTER_CHANNEL_FIELD.default_value = ""
BCEREGISTER_CHANNEL_FIELD.type = 9
BCEREGISTER_CHANNEL_FIELD.cpp_type = 9

BCEREGISTER_COUNTRY_FIELD.name = "country"
BCEREGISTER_COUNTRY_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.country"
BCEREGISTER_COUNTRY_FIELD.number = 8
BCEREGISTER_COUNTRY_FIELD.index = 7
BCEREGISTER_COUNTRY_FIELD.label = 1
BCEREGISTER_COUNTRY_FIELD.has_default_value = false
BCEREGISTER_COUNTRY_FIELD.default_value = ""
BCEREGISTER_COUNTRY_FIELD.type = 9
BCEREGISTER_COUNTRY_FIELD.cpp_type = 9

BCEREGISTER_LOCX_FIELD.name = "locx"
BCEREGISTER_LOCX_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.locx"
BCEREGISTER_LOCX_FIELD.number = 9
BCEREGISTER_LOCX_FIELD.index = 8
BCEREGISTER_LOCX_FIELD.label = 1
BCEREGISTER_LOCX_FIELD.has_default_value = false
BCEREGISTER_LOCX_FIELD.default_value = 0
BCEREGISTER_LOCX_FIELD.type = 5
BCEREGISTER_LOCX_FIELD.cpp_type = 1

BCEREGISTER_LOCY_FIELD.name = "locy"
BCEREGISTER_LOCY_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.locy"
BCEREGISTER_LOCY_FIELD.number = 10
BCEREGISTER_LOCY_FIELD.index = 9
BCEREGISTER_LOCY_FIELD.label = 1
BCEREGISTER_LOCY_FIELD.has_default_value = false
BCEREGISTER_LOCY_FIELD.default_value = 0
BCEREGISTER_LOCY_FIELD.type = 5
BCEREGISTER_LOCY_FIELD.cpp_type = 1

BCEREGISTER_MAJORVERSION_FIELD.name = "majorversion"
BCEREGISTER_MAJORVERSION_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.majorversion"
BCEREGISTER_MAJORVERSION_FIELD.number = 15
BCEREGISTER_MAJORVERSION_FIELD.index = 10
BCEREGISTER_MAJORVERSION_FIELD.label = 1
BCEREGISTER_MAJORVERSION_FIELD.has_default_value = true
BCEREGISTER_MAJORVERSION_FIELD.default_value = 0
BCEREGISTER_MAJORVERSION_FIELD.type = 5
BCEREGISTER_MAJORVERSION_FIELD.cpp_type = 1

BCEREGISTER_MINORVERSION_FIELD.name = "minorversion"
BCEREGISTER_MINORVERSION_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.minorversion"
BCEREGISTER_MINORVERSION_FIELD.number = 16
BCEREGISTER_MINORVERSION_FIELD.index = 11
BCEREGISTER_MINORVERSION_FIELD.label = 1
BCEREGISTER_MINORVERSION_FIELD.has_default_value = true
BCEREGISTER_MINORVERSION_FIELD.default_value = 0
BCEREGISTER_MINORVERSION_FIELD.type = 5
BCEREGISTER_MINORVERSION_FIELD.cpp_type = 1

BCEREGISTER_TINYVERSION_FIELD.name = "tinyversion"
BCEREGISTER_TINYVERSION_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister.tinyversion"
BCEREGISTER_TINYVERSION_FIELD.number = 17
BCEREGISTER_TINYVERSION_FIELD.index = 12
BCEREGISTER_TINYVERSION_FIELD.label = 1
BCEREGISTER_TINYVERSION_FIELD.has_default_value = true
BCEREGISTER_TINYVERSION_FIELD.default_value = 0
BCEREGISTER_TINYVERSION_FIELD.type = 5
BCEREGISTER_TINYVERSION_FIELD.cpp_type = 1

BCEREGISTER.name = "BceRegister"
BCEREGISTER.full_name = ".com.xinqihd.sns.gameserver.proto.BceRegister"
BCEREGISTER.nested_types = {}
BCEREGISTER.enum_types = {}
BCEREGISTER.fields = {BCEREGISTER_USERNAME_FIELD, BCEREGISTER_ROLENAME_FIELD, BCEREGISTER_PASSWORD_FIELD, BCEREGISTER_EMAIL_FIELD, BCEREGISTER_GENDER_FIELD, BCEREGISTER_CLIENT_FIELD, BCEREGISTER_CHANNEL_FIELD, BCEREGISTER_COUNTRY_FIELD, BCEREGISTER_LOCX_FIELD, BCEREGISTER_LOCY_FIELD, BCEREGISTER_MAJORVERSION_FIELD, BCEREGISTER_MINORVERSION_FIELD, BCEREGISTER_TINYVERSION_FIELD}
BCEREGISTER.is_extendable = false
BCEREGISTER.extensions = {}

BceRegister = protobuf.Message(BCEREGISTER)
_G.BCEREGISTER_PB_BCEREGISTER = BCEREGISTER

