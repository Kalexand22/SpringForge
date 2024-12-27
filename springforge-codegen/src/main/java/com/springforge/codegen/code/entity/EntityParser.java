
package com.springforge.codegen.code.entity;

import com.springforge.codegen.code.entity.model.BaseType;
import com.springforge.codegen.code.entity.model.DomainModels;
import com.springforge.codegen.code.entity.model.Entity;
import com.springforge.codegen.code.entity.model.EnumType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

public class EntityParser {

  private static final AtomicReference<JAXBContext> CONTEXT = new AtomicReference<>();

  private static JAXBContext getContext() throws JAXBException {
    if (CONTEXT.get() == null) {
      synchronized (CONTEXT) {
        CONTEXT.set(JAXBContext.newInstance(DomainModels.class));
      }
    }
    return CONTEXT.get();
  }

  public static List<BaseType<?>> parse(File file) throws JAXBException {
    JAXBContext context = getContext();
    Unmarshaller unmarshaller = context.createUnmarshaller();
    DomainModels domain = (DomainModels) unmarshaller.unmarshal(file);

    List<Entity> entities = domain.getEntities();
    List<EnumType> enums = domain.getEnums();

    List<BaseType<?>> types = new ArrayList<>();

    types.addAll(entities);
    types.addAll(enums);

    return types;
  }
}
