// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: BseMessage.proto

package com.xinqihd.sns.gameserver.proto;

public final class XinqiBseMessage {
  private XinqiBseMessage() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface BseMessageOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // required string uid = 1;
    boolean hasUid();
    String getUid();
    
    // optional .com.xinqihd.sns.gameserver.proto.LeaveMessage message = 2;
    boolean hasMessage();
    com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage getMessage();
    com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessageOrBuilder getMessageOrBuilder();
  }
  public static final class BseMessage extends
      com.google.protobuf.GeneratedMessage
      implements BseMessageOrBuilder {
    // Use BseMessage.newBuilder() to construct.
    private BseMessage(Builder builder) {
      super(builder);
    }
    private BseMessage(boolean noInit) {}
    
    private static final BseMessage defaultInstance;
    public static BseMessage getDefaultInstance() {
      return defaultInstance;
    }
    
    public BseMessage getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.xinqihd.sns.gameserver.proto.XinqiBseMessage.internal_static_com_xinqihd_sns_gameserver_proto_BseMessage_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.xinqihd.sns.gameserver.proto.XinqiBseMessage.internal_static_com_xinqihd_sns_gameserver_proto_BseMessage_fieldAccessorTable;
    }
    
    private int bitField0_;
    // required string uid = 1;
    public static final int UID_FIELD_NUMBER = 1;
    private java.lang.Object uid_;
    public boolean hasUid() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public String getUid() {
      java.lang.Object ref = uid_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          uid_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getUidBytes() {
      java.lang.Object ref = uid_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        uid_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    // optional .com.xinqihd.sns.gameserver.proto.LeaveMessage message = 2;
    public static final int MESSAGE_FIELD_NUMBER = 2;
    private com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage message_;
    public boolean hasMessage() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage getMessage() {
      return message_;
    }
    public com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessageOrBuilder getMessageOrBuilder() {
      return message_;
    }
    
    private void initFields() {
      uid_ = "";
      message_ = com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.getDefaultInstance();
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      if (!hasUid()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (hasMessage()) {
        if (!getMessage().isInitialized()) {
          memoizedIsInitialized = 0;
          return false;
        }
      }
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, getUidBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeMessage(2, message_);
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(1, getUidBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(2, message_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessageOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.xinqihd.sns.gameserver.proto.XinqiBseMessage.internal_static_com_xinqihd_sns_gameserver_proto_BseMessage_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.xinqihd.sns.gameserver.proto.XinqiBseMessage.internal_static_com_xinqihd_sns_gameserver_proto_BseMessage_fieldAccessorTable;
      }
      
      // Construct using com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          getMessageFieldBuilder();
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        uid_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        if (messageBuilder_ == null) {
          message_ = com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.getDefaultInstance();
        } else {
          messageBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage.getDescriptor();
      }
      
      public com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage getDefaultInstanceForType() {
        return com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage.getDefaultInstance();
      }
      
      public com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage build() {
        com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage buildPartial() {
        com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage result = new com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.uid_ = uid_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        if (messageBuilder_ == null) {
          result.message_ = message_;
        } else {
          result.message_ = messageBuilder_.build();
        }
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage) {
          return mergeFrom((com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage other) {
        if (other == com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage.getDefaultInstance()) return this;
        if (other.hasUid()) {
          setUid(other.getUid());
        }
        if (other.hasMessage()) {
          mergeMessage(other.getMessage());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        if (!hasUid()) {
          
          return false;
        }
        if (hasMessage()) {
          if (!getMessage().isInitialized()) {
            
            return false;
          }
        }
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 10: {
              bitField0_ |= 0x00000001;
              uid_ = input.readBytes();
              break;
            }
            case 18: {
              com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.Builder subBuilder = com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.newBuilder();
              if (hasMessage()) {
                subBuilder.mergeFrom(getMessage());
              }
              input.readMessage(subBuilder, extensionRegistry);
              setMessage(subBuilder.buildPartial());
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // required string uid = 1;
      private java.lang.Object uid_ = "";
      public boolean hasUid() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public String getUid() {
        java.lang.Object ref = uid_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          uid_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setUid(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        uid_ = value;
        onChanged();
        return this;
      }
      public Builder clearUid() {
        bitField0_ = (bitField0_ & ~0x00000001);
        uid_ = getDefaultInstance().getUid();
        onChanged();
        return this;
      }
      void setUid(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000001;
        uid_ = value;
        onChanged();
      }
      
      // optional .com.xinqihd.sns.gameserver.proto.LeaveMessage message = 2;
      private com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage message_ = com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
          com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage, com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.Builder, com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessageOrBuilder> messageBuilder_;
      public boolean hasMessage() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage getMessage() {
        if (messageBuilder_ == null) {
          return message_;
        } else {
          return messageBuilder_.getMessage();
        }
      }
      public Builder setMessage(com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage value) {
        if (messageBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          message_ = value;
          onChanged();
        } else {
          messageBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      public Builder setMessage(
          com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.Builder builderForValue) {
        if (messageBuilder_ == null) {
          message_ = builderForValue.build();
          onChanged();
        } else {
          messageBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      public Builder mergeMessage(com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage value) {
        if (messageBuilder_ == null) {
          if (((bitField0_ & 0x00000002) == 0x00000002) &&
              message_ != com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.getDefaultInstance()) {
            message_ =
              com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.newBuilder(message_).mergeFrom(value).buildPartial();
          } else {
            message_ = value;
          }
          onChanged();
        } else {
          messageBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      public Builder clearMessage() {
        if (messageBuilder_ == null) {
          message_ = com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.getDefaultInstance();
          onChanged();
        } else {
          messageBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      public com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.Builder getMessageBuilder() {
        bitField0_ |= 0x00000002;
        onChanged();
        return getMessageFieldBuilder().getBuilder();
      }
      public com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessageOrBuilder getMessageOrBuilder() {
        if (messageBuilder_ != null) {
          return messageBuilder_.getMessageOrBuilder();
        } else {
          return message_;
        }
      }
      private com.google.protobuf.SingleFieldBuilder<
          com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage, com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.Builder, com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessageOrBuilder> 
          getMessageFieldBuilder() {
        if (messageBuilder_ == null) {
          messageBuilder_ = new com.google.protobuf.SingleFieldBuilder<
              com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage, com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessage.Builder, com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.LeaveMessageOrBuilder>(
                  message_,
                  getParentForChildren(),
                  isClean());
          message_ = null;
        }
        return messageBuilder_;
      }
      
      // @@protoc_insertion_point(builder_scope:com.xinqihd.sns.gameserver.proto.BseMessage)
    }
    
    static {
      defaultInstance = new BseMessage(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:com.xinqihd.sns.gameserver.proto.BseMessage)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_xinqihd_sns_gameserver_proto_BseMessage_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_xinqihd_sns_gameserver_proto_BseMessage_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\020BseMessage.proto\022 com.xinqihd.sns.game" +
      "server.proto\032\022LeaveMessage.proto\"Z\n\nBseM" +
      "essage\022\013\n\003uid\030\001 \002(\t\022?\n\007message\030\002 \001(\0132..c" +
      "om.xinqihd.sns.gameserver.proto.LeaveMes" +
      "sageB\021B\017XinqiBseMessage"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_com_xinqihd_sns_gameserver_proto_BseMessage_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_com_xinqihd_sns_gameserver_proto_BseMessage_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_xinqihd_sns_gameserver_proto_BseMessage_descriptor,
              new java.lang.String[] { "Uid", "Message", },
              com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage.class,
              com.xinqihd.sns.gameserver.proto.XinqiBseMessage.BseMessage.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.xinqihd.sns.gameserver.proto.XinqiLeaveMessage.getDescriptor(),
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}