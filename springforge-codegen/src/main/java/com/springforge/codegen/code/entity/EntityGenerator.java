package com.springforge.codegen.code.entity;

import com.springforge.codegen.code.*;
import com.springforge.codegen.code.entity.model.BaseType;
import com.springforge.codegen.code.entity.model.Entity;
import com.springforge.codegen.code.entity.model.EnumType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.springforge.codegen.code.entity.model.PropertyType;
import com.springforge.codegen.code.entity.model.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

import jakarta.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
@ConfigurationProperties(prefix = "springforge.codegen")
public class EntityGenerator {

  private static final Logger log = LoggerFactory.getLogger(EntityGenerator.class);

  // Constants for model fields and auditing
  private static final Set<String> MODEL_FIELD_NAMES = ImmutableSet.of(
          "id", "version", "archived", "active");
  private static final Set<String> AUDITABLE_MODEL_FIELD_NAMES = ImmutableSet.of(
          "createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy");

  // Base model class paths
  private static final String SPRING_AUDITABLE_MODEL = "com.springforge.db.SpringAuditableEntity";
  private static final String SPRING_BASE_MODEL = "com.springforge.db.SpringBaseEntity";

  // Configuration properties
  private File domainPath;
  private File outputPath;
  private boolean generateRepositories = true;
  private boolean generateServices = true;
  private String basePackage;

  // Internal state
  private final Set<String> definedEntities = new HashSet<>();
  private final Set<String> definedEnums = new HashSet<>();
  private final List<EntityGenerator> lookup = new ArrayList<>();
  private final Function<String, String> formatter;
  private final Multimap<String, Entity> entities = LinkedHashMultimap.create();
  private final Multimap<String, EnumType> enums = LinkedHashMultimap.create();
  private static final Map<String, Entity> mergedEntities = new HashMap<>();

  public EntityGenerator() {
    this(null, null, String::toString);
  }

  public EntityGenerator(File domainPath, File outputPath) {
    this(domainPath, outputPath, String::toString);
  }

  public EntityGenerator(File domainPath, File outputPath, Function<String, String> formatter) {
    this.domainPath = domainPath;
    this.outputPath = outputPath;
    this.formatter = Objects.requireNonNull(formatter);
  }

  // Generate Spring Data JPA Repository
  private JavaType generateRepository(Entity entity) {
    String repoPackage = entity.getRepoPackage();
    String entityName = entity.getName();
    Property.PropertyType idType = entity.getIdType();

    // Conversion de PropertyType vers le type Java correspondant
    String idTypeStr = "Long"; // Type par défaut
    if (idType != null) {
      switch (idType) {
        case STRING:
          idTypeStr = "String";
          break;
        case INTEGER:
          idTypeStr = "Integer";
          break;
        case LONG:
          idTypeStr = "Long";
          break;
        case BOOLEAN:
          idTypeStr = "Boolean";
          break;
        case DECIMAL:
          idTypeStr = "java.math.BigDecimal";
          break;
        // Ajoutez les autres cas selon vos besoins
      }
    }

    JavaType repoType = JavaType.newClass(entityName + "Repository");
    repoType.annotation(new JavaAnnotation("@org.springframework.stereotype.Repository"));
    repoType.superInterface("org.springframework.data.jpa.repository.JpaRepository<" +
            entityName + ", " + idTypeStr + ">");

    // Add custom query methods based on unique fields
    entity.getFields().stream()
            .filter(Property::isUnique)
            .forEach(field -> {
              String methodName = "findBy" + capitalize(field.getName());
              String returnType = entityName;
              String paramType = field.getJavaType(); // Utilisation de getJavaType()
              JavaMethod method = new JavaMethod(methodName, "Optional<" + returnType + ">", Modifier.PUBLIC)
                      .param(field.getName(), paramType);
              method.declaration(true); // Pour une interface, on veut juste la déclaration
              repoType.method(method);
            });

    return repoType;
  }

  // Generate Spring Service class
  private JavaType generateService(Entity entity) {
    String servicePackage = entity.getPackageName() + ".service";
    String entityName = entity.getName();

    JavaType serviceType = JavaType.newClass(entityName + "Service");
    serviceType.annotation(new JavaAnnotation("@org.springframework.stereotype.Service"));
    serviceType.annotation(new JavaAnnotation("@org.springframework.transaction.annotation.Transactional"));

    // Add repository dependency
    String repoField = lcFirst(entityName) + "Repository";
    JavaField repositoryField = new JavaField(repoField, entityName + "Repository", Modifier.PRIVATE | Modifier.FINAL);
    serviceType.field(repositoryField);

    // Constructor
    JavaMethod constructor = new JavaMethod(entityName + "Service", null, Modifier.PUBLIC);
    constructor.annotation(new JavaAnnotation("@org.springframework.beans.factory.annotation.Autowired"));
    constructor.param(repoField, entityName + "Repository");
    constructor.code("this." + repoField + " = " + repoField + ";");
    serviceType.method(constructor);

    // Save method
    JavaMethod saveMethod = new JavaMethod("save", entityName, Modifier.PUBLIC);
    saveMethod.param("entity", entityName);
    saveMethod.code("return " + repoField + ".save(entity);");
    serviceType.method(saveMethod);

    // FindById method
    JavaMethod findByIdMethod = new JavaMethod("findById", "Optional<" + entityName + ">", Modifier.PUBLIC);
    Property.PropertyType idType = entity.getIdType();
    findByIdMethod.param("id", idType.getJavaType());
    findByIdMethod.code("return " + repoField + ".findById(id);");
    serviceType.method(findByIdMethod);

    // FindAll method
    JavaMethod findAllMethod = new JavaMethod("findAll", "List<" + entityName + ">", Modifier.PUBLIC);
    findAllMethod.code("return " + repoField + ".findAll();");
    serviceType.method(findAllMethod);

    // Delete method
    JavaMethod deleteMethod = new JavaMethod("delete", "void", Modifier.PUBLIC);
    deleteMethod.param("entity", entityName);
    deleteMethod.code(repoField + ".delete(entity);");
    serviceType.method(deleteMethod);

    return serviceType;
  }

  // Modified render method to generate additional Spring components
  private List<File> render(Collection<Entity> items, boolean doLookup) throws IOException {
    if (items == null || items.isEmpty()) {
      return null;
    }

    final List<Entity> all = new ArrayList<>(items);
    final Entity first = all.get(0);

    final String ns = first.getPackageName();
    final String name = first.getName();

    // Handle lookup entities
    if (doLookup) {
      for (EntityGenerator gen : lookup) {
        if (gen.definedEntities.contains(name)) {
          if (gen.entities.isEmpty()) {
            gen.processAll(false);
          }
          all.addAll(0, gen.entities.get(name));
        }
      }
    }

    // Validate namespace consistency
    for (Entity it : all) {
      if (!ns.equals(it.getPackageName())) {
        throw new IllegalArgumentException(
                String.format(
                        "Invalid namespace: %s.%s != %s.%s",
                        ns, name, it.getPackageName(), name));
      }
    }

    final Entity entity = all.remove(0);
    for (Entity it : all) {
      entity.merge(it);
    }
    mergedEntities.put(entity.getName(), entity);

    // Generate files
    final List<File> rendered = new ArrayList<>();

    // Generate entity class
    final JavaType javaType = entity.toJavaClass();
    if (javaType != null) {
      // Add Spring annotations
      javaType.annotation(new JavaAnnotation("@jakarta.persistence.Entity"));
      if (entity.hasAudit()) {
        javaType.annotation(new JavaAnnotation("@org.springframework.data.jpa.domain.support.AuditingEntityListener"));
      }
      rendered.add(save(new JavaFile(entity.getPackageName(), javaType)));
    }

    // Generate repository
    if (generateRepositories) {
      final JavaType repoType = generateRepository(entity);
      rendered.add(save(new JavaFile(entity.getRepoPackage(), repoType)));
    }

    // Generate service
    if (generateServices) {
      final JavaType serviceType = generateService(entity);
      rendered.add(save(new JavaFile(entity.getPackageName() + ".service", serviceType)));
    }

    return rendered;
  }

  // Configuration getters and setters
  public void setDomainPath(File domainPath) {
    this.domainPath = domainPath;
  }

  public void setOutputPath(File outputPath) {
    this.outputPath = outputPath;
  }

  public void setGenerateRepositories(boolean generateRepositories) {
    this.generateRepositories = generateRepositories;
  }

  public void setGenerateServices(boolean generateServices) {
    this.generateServices = generateServices;
  }

  public void setBasePackage(String basePackage) {
    this.basePackage = basePackage;
  }

  // Utility methods
  private String capitalize(String str) {
    if (str == null || str.isEmpty()) return str;
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }

  private String lcFirst(String str) {
    if (str == null || str.isEmpty()) return str;
    return Character.toLowerCase(str.charAt(0)) + str.substring(1);
  }

  // The rest of your existing methods (findFrom, processAll, start, etc.) remain largely unchanged
  // Only their implementation details might need minor adjustments for Spring Boot compatibility

  protected void writeTo(File output, JavaFile content) throws IOException {
    try (Writer writer = new FileWriter(output, StandardCharsets.UTF_8)) {
      content.writeTo(writer, formatter);
    }
  }

  protected void findFrom(File input) throws IOException {
    final List<BaseType<?>> types;
    try {
      types = EntityParser.parse(input);
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }

    types.stream()
            .filter(EnumType.class::isInstance)
            .map(BaseType::getName)
            .forEach(definedEnums::add);

    types.stream()
            .filter(Entity.class::isInstance)
            .map(BaseType::getName)
            .forEach(definedEntities::add);
  }

  public void start() throws IOException {
    log.info("Generating Spring Boot classes...");
    log.info("Domain path: {}", domainPath);
    log.info("Output path: {}", outputPath);
    log.info("Base package: {}", basePackage);
    log.info("Generate repositories: {}", generateRepositories);
    log.info("Generate services: {}", generateServices);

    outputPath.mkdirs();

    final Set<File> generated = new HashSet<>();

    if (this.domainPath.exists()) {
      for (File file : domainPath.listFiles()) {
        if (file.getName().endsWith(".xml")) {
          process(file, true);
        }
      }
    }

    // Generate enums first
    for (String name : enums.keySet()) {
      final List<File> rendered = renderEnum(enums.get(name), true);
      if (rendered != null) {
        generated.addAll(rendered);
      }
    }

    // Generate entities and their corresponding Spring components
    for (String name : entities.keySet()) {
      final List<File> rendered = render(entities.get(name), true);
      if (rendered != null) {
        generated.addAll(rendered);
      }
    }

    // Clean up obsolete files
    try (Stream<Path> walk = Files.walk(outputPath.toPath())) {
      walk.map(Path::toFile)
              .filter(f -> f.getName().endsWith(".java") || f.getName().endsWith(".groovy"))
              .filter(f -> !generated.contains(f))
              .forEach(f -> {
                log.info("Deleting obsolete file: {}", f);
                f.delete();
              });
    }
  }
}