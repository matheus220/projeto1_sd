// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sensor.proto

package com.sd.sensors;

/**
 * Protobuf type {@code com.sd.sensors.Sensor}
 */
public  final class Sensor extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:com.sd.sensors.Sensor)
    SensorOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Sensor.newBuilder() to construct.
  private Sensor(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Sensor() {
    id_ = "";
    addr_ = "";
    data_ = "";
    lastMsgDate_ = "";
    type_ = 0;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Sensor();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Sensor(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
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
            java.lang.String s = input.readStringRequireUtf8();

            id_ = s;
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            addr_ = s;
            break;
          }
          case 24: {

            port_ = input.readInt32();
            break;
          }
          case 32: {
            int rawValue = input.readEnum();

            type_ = rawValue;
            break;
          }
          case 42: {
            java.lang.String s = input.readStringRequireUtf8();

            data_ = s;
            break;
          }
          case 50: {
            java.lang.String s = input.readStringRequireUtf8();

            lastMsgDate_ = s;
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
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.sd.sensors.SensorProto.internal_static_com_sd_sensors_Sensor_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.sd.sensors.SensorProto.internal_static_com_sd_sensors_Sensor_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.sd.sensors.Sensor.class, com.sd.sensors.Sensor.Builder.class);
  }

  /**
   * Protobuf enum {@code com.sd.sensors.Sensor.SensorType}
   */
  public enum SensorType
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>LIGHT = 0;</code>
     */
    LIGHT(0),
    /**
     * <code>MAGNETIC = 1;</code>
     */
    MAGNETIC(1),
    /**
     * <code>SOUND = 2;</code>
     */
    SOUND(2),
    /**
     * <code>LED = 3;</code>
     */
    LED(3),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>LIGHT = 0;</code>
     */
    public static final int LIGHT_VALUE = 0;
    /**
     * <code>MAGNETIC = 1;</code>
     */
    public static final int MAGNETIC_VALUE = 1;
    /**
     * <code>SOUND = 2;</code>
     */
    public static final int SOUND_VALUE = 2;
    /**
     * <code>LED = 3;</code>
     */
    public static final int LED_VALUE = 3;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static SensorType valueOf(int value) {
      return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static SensorType forNumber(int value) {
      switch (value) {
        case 0: return LIGHT;
        case 1: return MAGNETIC;
        case 2: return SOUND;
        case 3: return LED;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<SensorType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        SensorType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<SensorType>() {
            public SensorType findValueByNumber(int number) {
              return SensorType.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.sd.sensors.Sensor.getDescriptor().getEnumTypes().get(0);
    }

    private static final SensorType[] VALUES = values();

    public static SensorType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private SensorType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:com.sd.sensors.Sensor.SensorType)
  }

  public static final int ID_FIELD_NUMBER = 1;
  private volatile java.lang.Object id_;
  /**
   * <code>string id = 1;</code>
   * @return The id.
   */
  public java.lang.String getId() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      id_ = s;
      return s;
    }
  }
  /**
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  public com.google.protobuf.ByteString
      getIdBytes() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      id_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ADDR_FIELD_NUMBER = 2;
  private volatile java.lang.Object addr_;
  /**
   * <code>string addr = 2;</code>
   * @return The addr.
   */
  public java.lang.String getAddr() {
    java.lang.Object ref = addr_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      addr_ = s;
      return s;
    }
  }
  /**
   * <code>string addr = 2;</code>
   * @return The bytes for addr.
   */
  public com.google.protobuf.ByteString
      getAddrBytes() {
    java.lang.Object ref = addr_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      addr_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int PORT_FIELD_NUMBER = 3;
  private int port_;
  /**
   * <code>int32 port = 3;</code>
   * @return The port.
   */
  public int getPort() {
    return port_;
  }

  public static final int DATA_FIELD_NUMBER = 5;
  private volatile java.lang.Object data_;
  /**
   * <code>string data = 5;</code>
   * @return The data.
   */
  public java.lang.String getData() {
    java.lang.Object ref = data_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      data_ = s;
      return s;
    }
  }
  /**
   * <code>string data = 5;</code>
   * @return The bytes for data.
   */
  public com.google.protobuf.ByteString
      getDataBytes() {
    java.lang.Object ref = data_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      data_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int LAST_MSG_DATE_FIELD_NUMBER = 6;
  private volatile java.lang.Object lastMsgDate_;
  /**
   * <code>string last_msg_date = 6;</code>
   * @return The lastMsgDate.
   */
  public java.lang.String getLastMsgDate() {
    java.lang.Object ref = lastMsgDate_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      lastMsgDate_ = s;
      return s;
    }
  }
  /**
   * <code>string last_msg_date = 6;</code>
   * @return The bytes for lastMsgDate.
   */
  public com.google.protobuf.ByteString
      getLastMsgDateBytes() {
    java.lang.Object ref = lastMsgDate_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      lastMsgDate_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int TYPE_FIELD_NUMBER = 4;
  private int type_;
  /**
   * <code>.com.sd.sensors.Sensor.SensorType type = 4;</code>
   * @return The enum numeric value on the wire for type.
   */
  public int getTypeValue() {
    return type_;
  }
  /**
   * <code>.com.sd.sensors.Sensor.SensorType type = 4;</code>
   * @return The type.
   */
  public com.sd.sensors.Sensor.SensorType getType() {
    @SuppressWarnings("deprecation")
    com.sd.sensors.Sensor.SensorType result = com.sd.sensors.Sensor.SensorType.valueOf(type_);
    return result == null ? com.sd.sensors.Sensor.SensorType.UNRECOGNIZED : result;
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
    if (!getIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, id_);
    }
    if (!getAddrBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, addr_);
    }
    if (port_ != 0) {
      output.writeInt32(3, port_);
    }
    if (type_ != com.sd.sensors.Sensor.SensorType.LIGHT.getNumber()) {
      output.writeEnum(4, type_);
    }
    if (!getDataBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, data_);
    }
    if (!getLastMsgDateBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 6, lastMsgDate_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, id_);
    }
    if (!getAddrBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, addr_);
    }
    if (port_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(3, port_);
    }
    if (type_ != com.sd.sensors.Sensor.SensorType.LIGHT.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(4, type_);
    }
    if (!getDataBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, data_);
    }
    if (!getLastMsgDateBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(6, lastMsgDate_);
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
    if (!(obj instanceof com.sd.sensors.Sensor)) {
      return super.equals(obj);
    }
    com.sd.sensors.Sensor other = (com.sd.sensors.Sensor) obj;

    if (!getId()
        .equals(other.getId())) return false;
    if (!getAddr()
        .equals(other.getAddr())) return false;
    if (getPort()
        != other.getPort()) return false;
    if (!getData()
        .equals(other.getData())) return false;
    if (!getLastMsgDate()
        .equals(other.getLastMsgDate())) return false;
    if (type_ != other.type_) return false;
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
    hash = (37 * hash) + ID_FIELD_NUMBER;
    hash = (53 * hash) + getId().hashCode();
    hash = (37 * hash) + ADDR_FIELD_NUMBER;
    hash = (53 * hash) + getAddr().hashCode();
    hash = (37 * hash) + PORT_FIELD_NUMBER;
    hash = (53 * hash) + getPort();
    hash = (37 * hash) + DATA_FIELD_NUMBER;
    hash = (53 * hash) + getData().hashCode();
    hash = (37 * hash) + LAST_MSG_DATE_FIELD_NUMBER;
    hash = (53 * hash) + getLastMsgDate().hashCode();
    hash = (37 * hash) + TYPE_FIELD_NUMBER;
    hash = (53 * hash) + type_;
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.sd.sensors.Sensor parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.sd.sensors.Sensor parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.sd.sensors.Sensor parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.sd.sensors.Sensor parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.sd.sensors.Sensor parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.sd.sensors.Sensor parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.sd.sensors.Sensor parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.sd.sensors.Sensor parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.sd.sensors.Sensor parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.sd.sensors.Sensor parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.sd.sensors.Sensor parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.sd.sensors.Sensor parseFrom(
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
  public static Builder newBuilder(com.sd.sensors.Sensor prototype) {
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
   * Protobuf type {@code com.sd.sensors.Sensor}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:com.sd.sensors.Sensor)
      com.sd.sensors.SensorOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.sd.sensors.SensorProto.internal_static_com_sd_sensors_Sensor_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.sd.sensors.SensorProto.internal_static_com_sd_sensors_Sensor_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.sd.sensors.Sensor.class, com.sd.sensors.Sensor.Builder.class);
    }

    // Construct using com.sd.sensors.Sensor.newBuilder()
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
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      id_ = "";

      addr_ = "";

      port_ = 0;

      data_ = "";

      lastMsgDate_ = "";

      type_ = 0;

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.sd.sensors.SensorProto.internal_static_com_sd_sensors_Sensor_descriptor;
    }

    @java.lang.Override
    public com.sd.sensors.Sensor getDefaultInstanceForType() {
      return com.sd.sensors.Sensor.getDefaultInstance();
    }

    @java.lang.Override
    public com.sd.sensors.Sensor build() {
      com.sd.sensors.Sensor result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.sd.sensors.Sensor buildPartial() {
      com.sd.sensors.Sensor result = new com.sd.sensors.Sensor(this);
      result.id_ = id_;
      result.addr_ = addr_;
      result.port_ = port_;
      result.data_ = data_;
      result.lastMsgDate_ = lastMsgDate_;
      result.type_ = type_;
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
      if (other instanceof com.sd.sensors.Sensor) {
        return mergeFrom((com.sd.sensors.Sensor)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.sd.sensors.Sensor other) {
      if (other == com.sd.sensors.Sensor.getDefaultInstance()) return this;
      if (!other.getId().isEmpty()) {
        id_ = other.id_;
        onChanged();
      }
      if (!other.getAddr().isEmpty()) {
        addr_ = other.addr_;
        onChanged();
      }
      if (other.getPort() != 0) {
        setPort(other.getPort());
      }
      if (!other.getData().isEmpty()) {
        data_ = other.data_;
        onChanged();
      }
      if (!other.getLastMsgDate().isEmpty()) {
        lastMsgDate_ = other.lastMsgDate_;
        onChanged();
      }
      if (other.type_ != 0) {
        setTypeValue(other.getTypeValue());
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
      com.sd.sensors.Sensor parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.sd.sensors.Sensor) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object id_ = "";
    /**
     * <code>string id = 1;</code>
     * @return The id.
     */
    public java.lang.String getId() {
      java.lang.Object ref = id_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        id_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string id = 1;</code>
     * @return The bytes for id.
     */
    public com.google.protobuf.ByteString
        getIdBytes() {
      java.lang.Object ref = id_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        id_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string id = 1;</code>
     * @param value The id to set.
     * @return This builder for chaining.
     */
    public Builder setId(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      id_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearId() {
      
      id_ = getDefaultInstance().getId();
      onChanged();
      return this;
    }
    /**
     * <code>string id = 1;</code>
     * @param value The bytes for id to set.
     * @return This builder for chaining.
     */
    public Builder setIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      id_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object addr_ = "";
    /**
     * <code>string addr = 2;</code>
     * @return The addr.
     */
    public java.lang.String getAddr() {
      java.lang.Object ref = addr_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        addr_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string addr = 2;</code>
     * @return The bytes for addr.
     */
    public com.google.protobuf.ByteString
        getAddrBytes() {
      java.lang.Object ref = addr_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        addr_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string addr = 2;</code>
     * @param value The addr to set.
     * @return This builder for chaining.
     */
    public Builder setAddr(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      addr_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string addr = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearAddr() {
      
      addr_ = getDefaultInstance().getAddr();
      onChanged();
      return this;
    }
    /**
     * <code>string addr = 2;</code>
     * @param value The bytes for addr to set.
     * @return This builder for chaining.
     */
    public Builder setAddrBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      addr_ = value;
      onChanged();
      return this;
    }

    private int port_ ;
    /**
     * <code>int32 port = 3;</code>
     * @return The port.
     */
    public int getPort() {
      return port_;
    }
    /**
     * <code>int32 port = 3;</code>
     * @param value The port to set.
     * @return This builder for chaining.
     */
    public Builder setPort(int value) {
      
      port_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 port = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearPort() {
      
      port_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object data_ = "";
    /**
     * <code>string data = 5;</code>
     * @return The data.
     */
    public java.lang.String getData() {
      java.lang.Object ref = data_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        data_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string data = 5;</code>
     * @return The bytes for data.
     */
    public com.google.protobuf.ByteString
        getDataBytes() {
      java.lang.Object ref = data_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        data_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string data = 5;</code>
     * @param value The data to set.
     * @return This builder for chaining.
     */
    public Builder setData(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      data_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string data = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearData() {
      
      data_ = getDefaultInstance().getData();
      onChanged();
      return this;
    }
    /**
     * <code>string data = 5;</code>
     * @param value The bytes for data to set.
     * @return This builder for chaining.
     */
    public Builder setDataBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      data_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object lastMsgDate_ = "";
    /**
     * <code>string last_msg_date = 6;</code>
     * @return The lastMsgDate.
     */
    public java.lang.String getLastMsgDate() {
      java.lang.Object ref = lastMsgDate_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        lastMsgDate_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string last_msg_date = 6;</code>
     * @return The bytes for lastMsgDate.
     */
    public com.google.protobuf.ByteString
        getLastMsgDateBytes() {
      java.lang.Object ref = lastMsgDate_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        lastMsgDate_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string last_msg_date = 6;</code>
     * @param value The lastMsgDate to set.
     * @return This builder for chaining.
     */
    public Builder setLastMsgDate(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      lastMsgDate_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string last_msg_date = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearLastMsgDate() {
      
      lastMsgDate_ = getDefaultInstance().getLastMsgDate();
      onChanged();
      return this;
    }
    /**
     * <code>string last_msg_date = 6;</code>
     * @param value The bytes for lastMsgDate to set.
     * @return This builder for chaining.
     */
    public Builder setLastMsgDateBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      lastMsgDate_ = value;
      onChanged();
      return this;
    }

    private int type_ = 0;
    /**
     * <code>.com.sd.sensors.Sensor.SensorType type = 4;</code>
     * @return The enum numeric value on the wire for type.
     */
    public int getTypeValue() {
      return type_;
    }
    /**
     * <code>.com.sd.sensors.Sensor.SensorType type = 4;</code>
     * @param value The enum numeric value on the wire for type to set.
     * @return This builder for chaining.
     */
    public Builder setTypeValue(int value) {
      type_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>.com.sd.sensors.Sensor.SensorType type = 4;</code>
     * @return The type.
     */
    public com.sd.sensors.Sensor.SensorType getType() {
      @SuppressWarnings("deprecation")
      com.sd.sensors.Sensor.SensorType result = com.sd.sensors.Sensor.SensorType.valueOf(type_);
      return result == null ? com.sd.sensors.Sensor.SensorType.UNRECOGNIZED : result;
    }
    /**
     * <code>.com.sd.sensors.Sensor.SensorType type = 4;</code>
     * @param value The type to set.
     * @return This builder for chaining.
     */
    public Builder setType(com.sd.sensors.Sensor.SensorType value) {
      if (value == null) {
        throw new NullPointerException();
      }
      
      type_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.com.sd.sensors.Sensor.SensorType type = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearType() {
      
      type_ = 0;
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


    // @@protoc_insertion_point(builder_scope:com.sd.sensors.Sensor)
  }

  // @@protoc_insertion_point(class_scope:com.sd.sensors.Sensor)
  private static final com.sd.sensors.Sensor DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.sd.sensors.Sensor();
  }

  public static com.sd.sensors.Sensor getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Sensor>
      PARSER = new com.google.protobuf.AbstractParser<Sensor>() {
    @java.lang.Override
    public Sensor parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Sensor(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Sensor> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Sensor> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.sd.sensors.Sensor getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

