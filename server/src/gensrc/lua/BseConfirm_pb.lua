-- Generated By protoc-gen-lua Do not Edit
local protobuf = require "protobuf"
module('BseConfirm_pb', package.seeall)


local BSECONFIRM = protobuf.Descriptor();
local BSECONFIRM_TYPE_FIELD = protobuf.FieldDescriptor();
local BSECONFIRM_MESSAGE_FIELD = protobuf.FieldDescriptor();
local BSECONFIRM_USERSESSION_FIELD = protobuf.FieldDescriptor();

BSECONFIRM_TYPE_FIELD.name = "type"
BSECONFIRM_TYPE_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BseConfirm.type"
BSECONFIRM_TYPE_FIELD.number = 1
BSECONFIRM_TYPE_FIELD.index = 0
BSECONFIRM_TYPE_FIELD.label = 2
BSECONFIRM_TYPE_FIELD.has_default_value = false
BSECONFIRM_TYPE_FIELD.default_value = ""
BSECONFIRM_TYPE_FIELD.type = 9
BSECONFIRM_TYPE_FIELD.cpp_type = 9

BSECONFIRM_MESSAGE_FIELD.name = "message"
BSECONFIRM_MESSAGE_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BseConfirm.message"
BSECONFIRM_MESSAGE_FIELD.number = 2
BSECONFIRM_MESSAGE_FIELD.index = 1
BSECONFIRM_MESSAGE_FIELD.label = 2
BSECONFIRM_MESSAGE_FIELD.has_default_value = false
BSECONFIRM_MESSAGE_FIELD.default_value = ""
BSECONFIRM_MESSAGE_FIELD.type = 9
BSECONFIRM_MESSAGE_FIELD.cpp_type = 9

BSECONFIRM_USERSESSION_FIELD.name = "usersession"
BSECONFIRM_USERSESSION_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BseConfirm.usersession"
BSECONFIRM_USERSESSION_FIELD.number = 3
BSECONFIRM_USERSESSION_FIELD.index = 2
BSECONFIRM_USERSESSION_FIELD.label = 1
BSECONFIRM_USERSESSION_FIELD.has_default_value = false
BSECONFIRM_USERSESSION_FIELD.default_value = ""
BSECONFIRM_USERSESSION_FIELD.type = 9
BSECONFIRM_USERSESSION_FIELD.cpp_type = 9

BSECONFIRM.name = "BseConfirm"
BSECONFIRM.full_name = ".com.xinqihd.sns.gameserver.proto.BseConfirm"
BSECONFIRM.nested_types = {}
BSECONFIRM.enum_types = {}
BSECONFIRM.fields = {BSECONFIRM_TYPE_FIELD, BSECONFIRM_MESSAGE_FIELD, BSECONFIRM_USERSESSION_FIELD}
BSECONFIRM.is_extendable = false
BSECONFIRM.extensions = {}

BseConfirm = protobuf.Message(BSECONFIRM)
_G.BSECONFIRM_PB_BSECONFIRM = BSECONFIRM

