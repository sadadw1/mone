// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: bo.proto

package run.mone.local.docean.protobuf;

/**
 * Protobuf type {@code run.mone.local.docean.protobuf.InstancesProto}
 */
public final class InstancesProto extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:run.mone.local.docean.protobuf.InstancesProto)
    InstancesProtoOrBuilder {
private static final long serialVersionUID = 0L;
  // Use InstancesProto.newBuilder() to construct.
  private InstancesProto(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private InstancesProto() {
    instances_ = java.util.Collections.emptyList();
    key_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new InstancesProto();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private InstancesProto(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
              instances_ = new java.util.ArrayList<run.mone.local.docean.protobuf.InstanceProto>();
              mutable_bitField0_ |= 0x00000001;
            }
            instances_.add(
                input.readMessage(run.mone.local.docean.protobuf.InstanceProto.parser(), extensionRegistry));
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            key_ = s;
            break;
          }
          case 24: {

            timestamp_ = input.readInt64();
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000001) != 0)) {
        instances_ = java.util.Collections.unmodifiableList(instances_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return run.mone.local.docean.protobuf.Bo.internal_static_run_mone_local_docean_protobuf_InstancesProto_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return run.mone.local.docean.protobuf.Bo.internal_static_run_mone_local_docean_protobuf_InstancesProto_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            run.mone.local.docean.protobuf.InstancesProto.class, run.mone.local.docean.protobuf.InstancesProto.Builder.class);
  }

  public static final int INSTANCES_FIELD_NUMBER = 1;
  private java.util.List<run.mone.local.docean.protobuf.InstanceProto> instances_;
  /**
   * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
   */
  @java.lang.Override
  public java.util.List<run.mone.local.docean.protobuf.InstanceProto> getInstancesList() {
    return instances_;
  }
  /**
   * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
   */
  @java.lang.Override
  public java.util.List<? extends run.mone.local.docean.protobuf.InstanceProtoOrBuilder> 
      getInstancesOrBuilderList() {
    return instances_;
  }
  /**
   * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
   */
  @java.lang.Override
  public int getInstancesCount() {
    return instances_.size();
  }
  /**
   * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
   */
  @java.lang.Override
  public run.mone.local.docean.protobuf.InstanceProto getInstances(int index) {
    return instances_.get(index);
  }
  /**
   * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
   */
  @java.lang.Override
  public run.mone.local.docean.protobuf.InstanceProtoOrBuilder getInstancesOrBuilder(
      int index) {
    return instances_.get(index);
  }

  public static final int KEY_FIELD_NUMBER = 2;
  private volatile java.lang.Object key_;
  /**
   * <code>string key = 2;</code>
   * @return The key.
   */
  @java.lang.Override
  public java.lang.String getKey() {
    java.lang.Object ref = key_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      key_ = s;
      return s;
    }
  }
  /**
   * <code>string key = 2;</code>
   * @return The bytes for key.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getKeyBytes() {
    java.lang.Object ref = key_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      key_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int TIMESTAMP_FIELD_NUMBER = 3;
  private long timestamp_;
  /**
   * <code>int64 timestamp = 3;</code>
   * @return The timestamp.
   */
  @java.lang.Override
  public long getTimestamp() {
    return timestamp_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    for (int i = 0; i < instances_.size(); i++) {
      output.writeMessage(1, instances_.get(i));
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(key_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, key_);
    }
    if (timestamp_ != 0L) {
      output.writeInt64(3, timestamp_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < instances_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, instances_.get(i));
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(key_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, key_);
    }
    if (timestamp_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(3, timestamp_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof run.mone.local.docean.protobuf.InstancesProto)) {
      return super.equals(obj);
    }
    run.mone.local.docean.protobuf.InstancesProto other = (run.mone.local.docean.protobuf.InstancesProto) obj;

    if (!getInstancesList()
        .equals(other.getInstancesList())) return false;
    if (!getKey()
        .equals(other.getKey())) return false;
    if (getTimestamp()
        != other.getTimestamp()) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (getInstancesCount() > 0) {
      hash = (37 * hash) + INSTANCES_FIELD_NUMBER;
      hash = (53 * hash) + getInstancesList().hashCode();
    }
    hash = (37 * hash) + KEY_FIELD_NUMBER;
    hash = (53 * hash) + getKey().hashCode();
    hash = (37 * hash) + TIMESTAMP_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getTimestamp());
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static run.mone.local.docean.protobuf.InstancesProto parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static run.mone.local.docean.protobuf.InstancesProto parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static run.mone.local.docean.protobuf.InstancesProto parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static run.mone.local.docean.protobuf.InstancesProto parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static run.mone.local.docean.protobuf.InstancesProto parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static run.mone.local.docean.protobuf.InstancesProto parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static run.mone.local.docean.protobuf.InstancesProto parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static run.mone.local.docean.protobuf.InstancesProto parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static run.mone.local.docean.protobuf.InstancesProto parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static run.mone.local.docean.protobuf.InstancesProto parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static run.mone.local.docean.protobuf.InstancesProto parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static run.mone.local.docean.protobuf.InstancesProto parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(run.mone.local.docean.protobuf.InstancesProto prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code run.mone.local.docean.protobuf.InstancesProto}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:run.mone.local.docean.protobuf.InstancesProto)
      run.mone.local.docean.protobuf.InstancesProtoOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return run.mone.local.docean.protobuf.Bo.internal_static_run_mone_local_docean_protobuf_InstancesProto_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return run.mone.local.docean.protobuf.Bo.internal_static_run_mone_local_docean_protobuf_InstancesProto_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              run.mone.local.docean.protobuf.InstancesProto.class, run.mone.local.docean.protobuf.InstancesProto.Builder.class);
    }

    // Construct using run.mone.local.docean.protobuf.InstancesProto.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
        getInstancesFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      if (instancesBuilder_ == null) {
        instances_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
      } else {
        instancesBuilder_.clear();
      }
      key_ = "";

      timestamp_ = 0L;

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return run.mone.local.docean.protobuf.Bo.internal_static_run_mone_local_docean_protobuf_InstancesProto_descriptor;
    }

    @java.lang.Override
    public run.mone.local.docean.protobuf.InstancesProto getDefaultInstanceForType() {
      return run.mone.local.docean.protobuf.InstancesProto.getDefaultInstance();
    }

    @java.lang.Override
    public run.mone.local.docean.protobuf.InstancesProto build() {
      run.mone.local.docean.protobuf.InstancesProto result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public run.mone.local.docean.protobuf.InstancesProto buildPartial() {
      run.mone.local.docean.protobuf.InstancesProto result = new run.mone.local.docean.protobuf.InstancesProto(this);
      int from_bitField0_ = bitField0_;
      if (instancesBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          instances_ = java.util.Collections.unmodifiableList(instances_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.instances_ = instances_;
      } else {
        result.instances_ = instancesBuilder_.build();
      }
      result.key_ = key_;
      result.timestamp_ = timestamp_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof run.mone.local.docean.protobuf.InstancesProto) {
        return mergeFrom((run.mone.local.docean.protobuf.InstancesProto)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(run.mone.local.docean.protobuf.InstancesProto other) {
      if (other == run.mone.local.docean.protobuf.InstancesProto.getDefaultInstance()) return this;
      if (instancesBuilder_ == null) {
        if (!other.instances_.isEmpty()) {
          if (instances_.isEmpty()) {
            instances_ = other.instances_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureInstancesIsMutable();
            instances_.addAll(other.instances_);
          }
          onChanged();
        }
      } else {
        if (!other.instances_.isEmpty()) {
          if (instancesBuilder_.isEmpty()) {
            instancesBuilder_.dispose();
            instancesBuilder_ = null;
            instances_ = other.instances_;
            bitField0_ = (bitField0_ & ~0x00000001);
            instancesBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getInstancesFieldBuilder() : null;
          } else {
            instancesBuilder_.addAllMessages(other.instances_);
          }
        }
      }
      if (!other.getKey().isEmpty()) {
        key_ = other.key_;
        onChanged();
      }
      if (other.getTimestamp() != 0L) {
        setTimestamp(other.getTimestamp());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      run.mone.local.docean.protobuf.InstancesProto parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (run.mone.local.docean.protobuf.InstancesProto) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.util.List<run.mone.local.docean.protobuf.InstanceProto> instances_ =
      java.util.Collections.emptyList();
    private void ensureInstancesIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        instances_ = new java.util.ArrayList<run.mone.local.docean.protobuf.InstanceProto>(instances_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        run.mone.local.docean.protobuf.InstanceProto, run.mone.local.docean.protobuf.InstanceProto.Builder, run.mone.local.docean.protobuf.InstanceProtoOrBuilder> instancesBuilder_;

    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public java.util.List<run.mone.local.docean.protobuf.InstanceProto> getInstancesList() {
      if (instancesBuilder_ == null) {
        return java.util.Collections.unmodifiableList(instances_);
      } else {
        return instancesBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public int getInstancesCount() {
      if (instancesBuilder_ == null) {
        return instances_.size();
      } else {
        return instancesBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public run.mone.local.docean.protobuf.InstanceProto getInstances(int index) {
      if (instancesBuilder_ == null) {
        return instances_.get(index);
      } else {
        return instancesBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public Builder setInstances(
        int index, run.mone.local.docean.protobuf.InstanceProto value) {
      if (instancesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureInstancesIsMutable();
        instances_.set(index, value);
        onChanged();
      } else {
        instancesBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public Builder setInstances(
        int index, run.mone.local.docean.protobuf.InstanceProto.Builder builderForValue) {
      if (instancesBuilder_ == null) {
        ensureInstancesIsMutable();
        instances_.set(index, builderForValue.build());
        onChanged();
      } else {
        instancesBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public Builder addInstances(run.mone.local.docean.protobuf.InstanceProto value) {
      if (instancesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureInstancesIsMutable();
        instances_.add(value);
        onChanged();
      } else {
        instancesBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public Builder addInstances(
        int index, run.mone.local.docean.protobuf.InstanceProto value) {
      if (instancesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureInstancesIsMutable();
        instances_.add(index, value);
        onChanged();
      } else {
        instancesBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public Builder addInstances(
        run.mone.local.docean.protobuf.InstanceProto.Builder builderForValue) {
      if (instancesBuilder_ == null) {
        ensureInstancesIsMutable();
        instances_.add(builderForValue.build());
        onChanged();
      } else {
        instancesBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public Builder addInstances(
        int index, run.mone.local.docean.protobuf.InstanceProto.Builder builderForValue) {
      if (instancesBuilder_ == null) {
        ensureInstancesIsMutable();
        instances_.add(index, builderForValue.build());
        onChanged();
      } else {
        instancesBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public Builder addAllInstances(
        java.lang.Iterable<? extends run.mone.local.docean.protobuf.InstanceProto> values) {
      if (instancesBuilder_ == null) {
        ensureInstancesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, instances_);
        onChanged();
      } else {
        instancesBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public Builder clearInstances() {
      if (instancesBuilder_ == null) {
        instances_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        instancesBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public Builder removeInstances(int index) {
      if (instancesBuilder_ == null) {
        ensureInstancesIsMutable();
        instances_.remove(index);
        onChanged();
      } else {
        instancesBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public run.mone.local.docean.protobuf.InstanceProto.Builder getInstancesBuilder(
        int index) {
      return getInstancesFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public run.mone.local.docean.protobuf.InstanceProtoOrBuilder getInstancesOrBuilder(
        int index) {
      if (instancesBuilder_ == null) {
        return instances_.get(index);  } else {
        return instancesBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public java.util.List<? extends run.mone.local.docean.protobuf.InstanceProtoOrBuilder> 
         getInstancesOrBuilderList() {
      if (instancesBuilder_ != null) {
        return instancesBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(instances_);
      }
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public run.mone.local.docean.protobuf.InstanceProto.Builder addInstancesBuilder() {
      return getInstancesFieldBuilder().addBuilder(
          run.mone.local.docean.protobuf.InstanceProto.getDefaultInstance());
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public run.mone.local.docean.protobuf.InstanceProto.Builder addInstancesBuilder(
        int index) {
      return getInstancesFieldBuilder().addBuilder(
          index, run.mone.local.docean.protobuf.InstanceProto.getDefaultInstance());
    }
    /**
     * <code>repeated .run.mone.local.docean.protobuf.InstanceProto instances = 1;</code>
     */
    public java.util.List<run.mone.local.docean.protobuf.InstanceProto.Builder> 
         getInstancesBuilderList() {
      return getInstancesFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        run.mone.local.docean.protobuf.InstanceProto, run.mone.local.docean.protobuf.InstanceProto.Builder, run.mone.local.docean.protobuf.InstanceProtoOrBuilder> 
        getInstancesFieldBuilder() {
      if (instancesBuilder_ == null) {
        instancesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            run.mone.local.docean.protobuf.InstanceProto, run.mone.local.docean.protobuf.InstanceProto.Builder, run.mone.local.docean.protobuf.InstanceProtoOrBuilder>(
                instances_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        instances_ = null;
      }
      return instancesBuilder_;
    }

    private java.lang.Object key_ = "";
    /**
     * <code>string key = 2;</code>
     * @return The key.
     */
    public java.lang.String getKey() {
      java.lang.Object ref = key_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        key_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string key = 2;</code>
     * @return The bytes for key.
     */
    public com.google.protobuf.ByteString
        getKeyBytes() {
      java.lang.Object ref = key_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        key_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string key = 2;</code>
     * @param value The key to set.
     * @return This builder for chaining.
     */
    public Builder setKey(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      key_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string key = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearKey() {
      
      key_ = getDefaultInstance().getKey();
      onChanged();
      return this;
    }
    /**
     * <code>string key = 2;</code>
     * @param value The bytes for key to set.
     * @return This builder for chaining.
     */
    public Builder setKeyBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      key_ = value;
      onChanged();
      return this;
    }

    private long timestamp_ ;
    /**
     * <code>int64 timestamp = 3;</code>
     * @return The timestamp.
     */
    @java.lang.Override
    public long getTimestamp() {
      return timestamp_;
    }
    /**
     * <code>int64 timestamp = 3;</code>
     * @param value The timestamp to set.
     * @return This builder for chaining.
     */
    public Builder setTimestamp(long value) {
      
      timestamp_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int64 timestamp = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearTimestamp() {
      
      timestamp_ = 0L;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:run.mone.local.docean.protobuf.InstancesProto)
  }

  // @@protoc_insertion_point(class_scope:run.mone.local.docean.protobuf.InstancesProto)
  private static final run.mone.local.docean.protobuf.InstancesProto DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new run.mone.local.docean.protobuf.InstancesProto();
  }

  public static run.mone.local.docean.protobuf.InstancesProto getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<InstancesProto>
      PARSER = new com.google.protobuf.AbstractParser<InstancesProto>() {
    @java.lang.Override
    public InstancesProto parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new InstancesProto(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<InstancesProto> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<InstancesProto> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public run.mone.local.docean.protobuf.InstancesProto getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

