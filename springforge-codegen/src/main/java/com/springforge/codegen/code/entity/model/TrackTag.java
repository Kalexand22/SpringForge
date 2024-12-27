package com.springforge.codegen.code.entity.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum TrackTag {
  @XmlEnumValue("success")
  SUCCESS("success"),

  @XmlEnumValue("warning")
  WARNING("warning"),

  @XmlEnumValue("important")
  IMPORTANT("important"),

  @XmlEnumValue("info")
  INFO("info");

  private final String value;

  TrackTag(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static TrackTag fromValue(String v) {
    for (TrackTag c : TrackTag.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }
}