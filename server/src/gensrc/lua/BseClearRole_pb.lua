-- Generated By protoc-gen-lua Do not Edit
local protobuf = require "protobuf"
module('BseClearRole_pb', package.seeall)


local BSECLEARROLE = protobuf.Descriptor();
local BSECLEARROLE_SESSIONID_FIELD = protobuf.FieldDescriptor();

BSECLEARROLE_SESSIONID_FIELD.name = "sessionId"
BSECLEARROLE_SESSIONID_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BseClearRole.sessionId"
BSECLEARROLE_SESSIONID_FIELD.number = 1
BSECLEARROLE_SESSIONID_FIELD.index = 0
BSECLEARROLE_SESSIONID_FIELD.label = 2
BSECLEARROLE_SESSIONID_FIELD.has_default_value = false
BSECLEARROLE_SESSIONID_FIELD.default_value = ""
BSECLEARROLE_SESSIONID_FIELD.type = 9
BSECLEARROLE_SESSIONID_FIELD.cpp_type = 9

BSECLEARROLE.name = "BseClearRole"
BSECLEARROLE.full_name = ".com.xinqihd.sns.gameserver.proto.BseClearRole"
BSECLEARROLE.nested_types = {}
BSECLEARROLE.enum_types = {}
BSECLEARROLE.fields = {BSECLEARROLE_SESSIONID_FIELD}
BSECLEARROLE.is_extendable = false
BSECLEARROLE.extensions = {}

BseClearRole = protobuf.Message(BSECLEARROLE)
_G.BSECLEARROLE_PB_BSECLEARROLE = BSECLEARROLE

