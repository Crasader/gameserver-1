-- Generated By protoc-gen-lua Do not Edit
local protobuf = require "protobuf"
module('BseFindRoom_pb', package.seeall)


local BSEFINDROOM = protobuf.Descriptor();
local BSEFINDROOM_ROOMID_FIELD = protobuf.FieldDescriptor();
local BSEFINDROOM_KEY_FIELD = protobuf.FieldDescriptor();
local BSEFINDROOM_ERROR_FIELD = protobuf.FieldDescriptor();

BSEFINDROOM_ROOMID_FIELD.name = "roomID"
BSEFINDROOM_ROOMID_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BseFindRoom.roomID"
BSEFINDROOM_ROOMID_FIELD.number = 1
BSEFINDROOM_ROOMID_FIELD.index = 0
BSEFINDROOM_ROOMID_FIELD.label = 2
BSEFINDROOM_ROOMID_FIELD.has_default_value = true
BSEFINDROOM_ROOMID_FIELD.default_value = 0
BSEFINDROOM_ROOMID_FIELD.type = 5
BSEFINDROOM_ROOMID_FIELD.cpp_type = 1

BSEFINDROOM_KEY_FIELD.name = "key"
BSEFINDROOM_KEY_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BseFindRoom.key"
BSEFINDROOM_KEY_FIELD.number = 2
BSEFINDROOM_KEY_FIELD.index = 1
BSEFINDROOM_KEY_FIELD.label = 2
BSEFINDROOM_KEY_FIELD.has_default_value = false
BSEFINDROOM_KEY_FIELD.default_value = ""
BSEFINDROOM_KEY_FIELD.type = 9
BSEFINDROOM_KEY_FIELD.cpp_type = 9

BSEFINDROOM_ERROR_FIELD.name = "error"
BSEFINDROOM_ERROR_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.BseFindRoom.error"
BSEFINDROOM_ERROR_FIELD.number = 3
BSEFINDROOM_ERROR_FIELD.index = 2
BSEFINDROOM_ERROR_FIELD.label = 2
BSEFINDROOM_ERROR_FIELD.has_default_value = false
BSEFINDROOM_ERROR_FIELD.default_value = 0
BSEFINDROOM_ERROR_FIELD.type = 5
BSEFINDROOM_ERROR_FIELD.cpp_type = 1

BSEFINDROOM.name = "BseFindRoom"
BSEFINDROOM.full_name = ".com.xinqihd.sns.gameserver.proto.BseFindRoom"
BSEFINDROOM.nested_types = {}
BSEFINDROOM.enum_types = {}
BSEFINDROOM.fields = {BSEFINDROOM_ROOMID_FIELD, BSEFINDROOM_KEY_FIELD, BSEFINDROOM_ERROR_FIELD}
BSEFINDROOM.is_extendable = false
BSEFINDROOM.extensions = {}

BseFindRoom = protobuf.Message(BSEFINDROOM)
_G.BSEFINDROOM_PB_BSEFINDROOM = BSEFINDROOM

