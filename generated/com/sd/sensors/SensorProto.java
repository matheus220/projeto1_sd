// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sensor.proto

package com.sd.sensors;

public final class SensorProto {
  private SensorProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_com_sd_sensors_Sensor_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_com_sd_sensors_Sensor_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_com_sd_sensors_Command_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_com_sd_sensors_Command_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_com_sd_sensors_Message_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_com_sd_sensors_Message_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\014sensor.proto\022\016com.sd.sensors\"\301\001\n\006Senso" +
      "r\022\n\n\002id\030\001 \001(\t\022\014\n\004addr\030\002 \001(\t\022\014\n\004port\030\003 \001(" +
      "\005\022\014\n\004data\030\005 \001(\t\022\025\n\rlast_msg_date\030\006 \001(\t\022/" +
      "\n\004type\030\004 \001(\0162!.com.sd.sensors.Sensor.Sen" +
      "sorType\"9\n\nSensorType\022\t\n\005LIGHT\020\000\022\014\n\010MAGN" +
      "ETIC\020\001\022\t\n\005SOUND\020\002\022\007\n\003LED\020\003\"&\n\007Command\022\n\n" +
      "\002id\030\001 \001(\t\022\017\n\007command\030\002 \001(\t\"2\n\007Message\022\'\n" +
      "\007sensors\030\001 \003(\0132\026.com.sd.sensors.Sensor2O" +
      "\n\021SensorServiceGRPC\022:\n\004Send\022\027.com.sd.sen" +
      "sors.Command\032\027.com.sd.sensors.Message\"\000B" +
      "\"\n\016com.sd.sensorsB\013SensorProtoP\001\210\001\001b\006pro" +
      "to3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_com_sd_sensors_Sensor_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_com_sd_sensors_Sensor_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_com_sd_sensors_Sensor_descriptor,
        new java.lang.String[] { "Id", "Addr", "Port", "Data", "LastMsgDate", "Type", });
    internal_static_com_sd_sensors_Command_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_com_sd_sensors_Command_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_com_sd_sensors_Command_descriptor,
        new java.lang.String[] { "Id", "Command", });
    internal_static_com_sd_sensors_Message_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_com_sd_sensors_Message_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_com_sd_sensors_Message_descriptor,
        new java.lang.String[] { "Sensors", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}