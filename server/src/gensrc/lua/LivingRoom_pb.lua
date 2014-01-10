-- Generated By protoc-gen-lua Do not Edit
local protobuf = require "protobuf"
module('LivingRoom_pb', package.seeall)


local LIVINGROOM = protobuf.Descriptor();
local LIVINGROOM_CAN_PICK_GOLD_FIELD = protobuf.FieldDescriptor();
local LIVINGROOM_MESSAGES_FIELD = protobuf.FieldDescriptor();
local LIVINGROOM_NOTICE_FIELD = protobuf.FieldDescriptor();
local LIVINGROOM_EQUIPS_FIELD = protobuf.FieldDescriptor();
local LIVINGROOM_GENDER_FIELD = protobuf.FieldDescriptor();
local LIVINGROOM_DATA_FIELD = protobuf.FieldDescriptor();
local LIVINGROOM_EXDATA_FIELD = protobuf.FieldDescriptor();

LIVINGROOM_CAN_PICK_GOLD_FIELD.name = "can_pick_gold"
LIVINGROOM_CAN_PICK_GOLD_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.LivingRoom.can_pick_gold"
LIVINGROOM_CAN_PICK_GOLD_FIELD.number = 1
LIVINGROOM_CAN_PICK_GOLD_FIELD.index = 0
LIVINGROOM_CAN_PICK_GOLD_FIELD.label = 1
LIVINGROOM_CAN_PICK_GOLD_FIELD.has_default_value = true
LIVINGROOM_CAN_PICK_GOLD_FIELD.default_value = false
LIVINGROOM_CAN_PICK_GOLD_FIELD.type = 8
LIVINGROOM_CAN_PICK_GOLD_FIELD.cpp_type = 7

LIVINGROOM_MESSAGES_FIELD.name = "messages"
LIVINGROOM_MESSAGES_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.LivingRoom.messages"
LIVINGROOM_MESSAGES_FIELD.number = 2
LIVINGROOM_MESSAGES_FIELD.index = 1
LIVINGROOM_MESSAGES_FIELD.label = 3
LIVINGROOM_MESSAGES_FIELD.has_default_value = false
LIVINGROOM_MESSAGES_FIELD.default_value = {}
LIVINGROOM_MESSAGES_FIELD.message_type = LEAVEMESSAGE_PB_LEAVEMESSAGE
LIVINGROOM_MESSAGES_FIELD.type = 11
LIVINGROOM_MESSAGES_FIELD.cpp_type = 10

LIVINGROOM_NOTICE_FIELD.name = "notice"
LIVINGROOM_NOTICE_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.LivingRoom.notice"
LIVINGROOM_NOTICE_FIELD.number = 3
LIVINGROOM_NOTICE_FIELD.index = 2
LIVINGROOM_NOTICE_FIELD.label = 1
LIVINGROOM_NOTICE_FIELD.has_default_value = true
LIVINGROOM_NOTICE_FIELD.default_value = ""
LIVINGROOM_NOTICE_FIELD.type = 9
LIVINGROOM_NOTICE_FIELD.cpp_type = 9

LIVINGROOM_EQUIPS_FIELD.name = "equips"
LIVINGROOM_EQUIPS_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.LivingRoom.equips"
LIVINGROOM_EQUIPS_FIELD.number = 4
LIVINGROOM_EQUIPS_FIELD.index = 3
LIVINGROOM_EQUIPS_FIELD.label = 3
LIVINGROOM_EQUIPS_FIELD.has_default_value = false
LIVINGROOM_EQUIPS_FIELD.default_value = {}
LIVINGROOM_EQUIPS_FIELD.message_type = PROPDATA_PB_PROPDATA
LIVINGROOM_EQUIPS_FIELD.type = 11
LIVINGROOM_EQUIPS_FIELD.cpp_type = 10

LIVINGROOM_GENDER_FIELD.name = "gender"
LIVINGROOM_GENDER_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.LivingRoom.gender"
LIVINGROOM_GENDER_FIELD.number = 5
LIVINGROOM_GENDER_FIELD.index = 4
LIVINGROOM_GENDER_FIELD.label = 1
LIVINGROOM_GENDER_FIELD.has_default_value = true
LIVINGROOM_GENDER_FIELD.default_value = 1
LIVINGROOM_GENDER_FIELD.type = 5
LIVINGROOM_GENDER_FIELD.cpp_type = 1

LIVINGROOM_DATA_FIELD.name = "data"
LIVINGROOM_DATA_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.LivingRoom.data"
LIVINGROOM_DATA_FIELD.number = 6
LIVINGROOM_DATA_FIELD.index = 5
LIVINGROOM_DATA_FIELD.label = 1
LIVINGROOM_DATA_FIELD.has_default_value = false
LIVINGROOM_DATA_FIELD.default_value = nil
LIVINGROOM_DATA_FIELD.message_type = USERDATA_PB_USERDATA
LIVINGROOM_DATA_FIELD.type = 11
LIVINGROOM_DATA_FIELD.cpp_type = 10

LIVINGROOM_EXDATA_FIELD.name = "exdata"
LIVINGROOM_EXDATA_FIELD.full_name = ".com.xinqihd.sns.gameserver.proto.LivingRoom.exdata"
LIVINGROOM_EXDATA_FIELD.number = 7
LIVINGROOM_EXDATA_FIELD.index = 6
LIVINGROOM_EXDATA_FIELD.label = 1
LIVINGROOM_EXDATA_FIELD.has_default_value = false
LIVINGROOM_EXDATA_FIELD.default_value = nil
LIVINGROOM_EXDATA_FIELD.message_type = USEREXDATA_PB_USEREXDATA
LIVINGROOM_EXDATA_FIELD.type = 11
LIVINGROOM_EXDATA_FIELD.cpp_type = 10

LIVINGROOM.name = "LivingRoom"
LIVINGROOM.full_name = ".com.xinqihd.sns.gameserver.proto.LivingRoom"
LIVINGROOM.nested_types = {}
LIVINGROOM.enum_types = {}
LIVINGROOM.fields = {LIVINGROOM_CAN_PICK_GOLD_FIELD, LIVINGROOM_MESSAGES_FIELD, LIVINGROOM_NOTICE_FIELD, LIVINGROOM_EQUIPS_FIELD, LIVINGROOM_GENDER_FIELD, LIVINGROOM_DATA_FIELD, LIVINGROOM_EXDATA_FIELD}
LIVINGROOM.is_extendable = false
LIVINGROOM.extensions = {}

LivingRoom = protobuf.Message(LIVINGROOM)
_G.LIVINGROOM_PB_LIVINGROOM = LIVINGROOM
