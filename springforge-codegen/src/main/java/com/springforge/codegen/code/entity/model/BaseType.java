package com.springforge.codegen.code.entity.model;

import com.springforge.codegen.code.JavaType;

public interface BaseType<T> {

  String getName();

  String getPackageName();

  JavaType toJavaClass();

  default JavaType toRepoClass() {
    return null;
  }

  void merge(T other);
}
