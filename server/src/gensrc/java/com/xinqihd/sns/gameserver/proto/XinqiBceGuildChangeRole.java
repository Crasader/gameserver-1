// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: BceGuildChangeRole.proto

package com.xinqihd.sns.gameserver.proto;

public final class XinqiBceGuildChangeRole {
  private XinqiBceGuildChangeRole() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface BceGuildChangeRoleOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // optional string userid = 1;
    boolean hasUserid();
    String getUserid();
    
    // optional string targetrole = 2;
    boolean hasTargetrole();
    String getTargetrole();
  }
  public static final class BceGuildChangeRole extends
      com.google.protobuf.GeneratedMessage
      implements BceGuildChangeRoleOrBuilder {
    // Use BceGuildChangeRole.newBuilder() to construct.
    private BceGuildChangeRole(Builder builder) {
      super(builder);
    }
    private BceGuildChangeRole(boolean noInit) {}
    
    private static final BceGuildChangeRole defaultInstance;
    public static BceGuildChangeRole getDefaultInstance() {
      return defaultInstance;
    }
    
    public BceGuildChangeRole getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.internal_static_com_xinqihd_sns_gameserver_proto_BceGuildChangeRole_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.internal_static_com_xinqihd_sns_gameserver_proto_BceGuildChangeRole_fieldAccessorTable;
    }
    
    private int bitField0_;
    // optional string userid = 1;
    public static final int USERID_FIELD_NUMBER = 1;
    private java.lang.Object userid_;
    public boolean hasUserid() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public String getUserid() {
      java.lang.Object ref = userid_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          userid_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getUseridBytes() {
      java.lang.Object ref = userid_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        userid_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    // optional string targetrole = 2;
    public static final int TARGETROLE_FIELD_NUMBER = 2;
    private java.lang.Object targetrole_;
    public boolean hasTargetrole() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public String getTargetrole() {
      java.lang.Object ref = targetrole_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          targetrole_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getTargetroleBytes() {
      java.lang.Object ref = targetrole_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        targetrole_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    private void initFields() {
      userid_ = "";
      targetrole_ = "";
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, getUseridBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getTargetroleBytes());
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
          .computeBytesSize(1, getUseridBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, getTargetroleBytes());
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
    
    public static com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole parseDelimitedFrom(
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
    public static com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole prototype) {
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
       implements com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRoleOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.internal_static_com_xinqihd_sns_gameserver_proto_BceGuildChangeRole_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.internal_static_com_xinqihd_sns_gameserver_proto_BceGuildChangeRole_fieldAccessorTable;
      }
      
      // Construct using com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        userid_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        targetrole_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole.getDescriptor();
      }
      
      public com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole getDefaultInstanceForType() {
        return com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole.getDefaultInstance();
      }
      
      public com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole build() {
        com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole buildPartial() {
        com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole result = new com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.userid_ = userid_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.targetrole_ = targetrole_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole) {
          return mergeFrom((com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole other) {
        if (other == com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole.getDefaultInstance()) return this;
        if (other.hasUserid()) {
          setUserid(other.getUserid());
        }
        if (other.hasTargetrole()) {
          setTargetrole(other.getTargetrole());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
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
              userid_ = input.readBytes();
              break;
            }
            case 18: {
              bitField0_ |= 0x00000002;
              targetrole_ = input.readBytes();
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // optional string userid = 1;
      private java.lang.Object userid_ = "";
      public boolean hasUserid() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public String getUserid() {
        java.lang.Object ref = userid_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          userid_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setUserid(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        userid_ = value;
        onChanged();
        return this;
      }
      public Builder clearUserid() {
        bitField0_ = (bitField0_ & ~0x00000001);
        userid_ = getDefaultInstance().getUserid();
        onChanged();
        return this;
      }
      void setUserid(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000001;
        userid_ = value;
        onChanged();
      }
      
      // optional string targetrole = 2;
      private java.lang.Object targetrole_ = "";
      public boolean hasTargetrole() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public String getTargetrole() {
        java.lang.Object ref = targetrole_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          targetrole_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setTargetrole(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        targetrole_ = value;
        onChanged();
        return this;
      }
      public Builder clearTargetrole() {
        bitField0_ = (bitField0_ & ~0x00000002);
        targetrole_ = getDefaultInstance().getTargetrole();
        onChanged();
        return this;
      }
      void setTargetrole(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000002;
        targetrole_ = value;
        onChanged();
      }
      
      // @@protoc_insertion_point(builder_scope:com.xinqihd.sns.gameserver.proto.BceGuildChangeRole)
    }
    
    static {
      defaultInstance = new BceGuildChangeRole(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:com.xinqihd.sns.gameserver.proto.BceGuildChangeRole)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_xinqihd_sns_gameserver_proto_BceGuildChangeRole_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_xinqihd_sns_gameserver_proto_BceGuildChangeRole_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\030BceGuildChangeRole.proto\022 com.xinqihd." +
      "sns.gameserver.proto\"8\n\022BceGuildChangeRo" +
      "le\022\016\n\006userid\030\001 \001(\t\022\022\n\ntargetrole\030\002 \001(\tB\031" +
      "B\027XinqiBceGuildChangeRole"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_com_xinqihd_sns_gameserver_proto_BceGuildChangeRole_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_com_xinqihd_sns_gameserver_proto_BceGuildChangeRole_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_xinqihd_sns_gameserver_proto_BceGuildChangeRole_descriptor,
              new java.lang.String[] { "Userid", "Targetrole", },
              com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole.class,
              com.xinqihd.sns.gameserver.proto.XinqiBceGuildChangeRole.BceGuildChangeRole.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
