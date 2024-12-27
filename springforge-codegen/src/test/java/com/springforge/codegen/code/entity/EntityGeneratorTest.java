package com.springforge.codegen.code.entity;

import com.springforge.codegen.code.entity.EntityGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EntityGeneratorTest {
    
    @TempDir
    Path tempDir;
    
    private EntityGenerator generator;
    private File domainPath;
    private File outputPath;

    @BeforeEach
    void setUp() throws IOException {
        domainPath = new File(tempDir.toFile(), "domain");
        outputPath = new File(tempDir.toFile(), "generated");
        domainPath.mkdirs();
        outputPath.mkdirs();

        generator = new EntityGenerator(domainPath, outputPath);
        generator.setBasePackage("com.example");
        
        // Créer un fichier XML de test
        createTestEntityXml();
    }

    private void createTestEntityXml() throws IOException {
        String xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <domain-models xmlns="http://springforge.com/xml/ns/domain-models">
            <module name="test" package="com.example">
                <entity name="User">
                    <string name="username" required="true" unique="true"/>
                    <string name="email" required="true"/>
                    <string name="password" required="true"/>
                    <boolean name="active" default="true"/>
                    <datetime name="lastLoginDate"/>
                </entity>
            </module>
        </domain-models>
        """;
        
        File xmlFile = new File(domainPath, "user.xml");
        Files.writeString(xmlFile.toPath(), xml);
    }

    @Test
    void testEntityGeneration() throws IOException {
        generator.start();

        // Vérifier la création des fichiers
        assertTrue(new File(outputPath, "com/example/User.java").exists());
        assertTrue(new File(outputPath, "com/example/repo/UserRepository.java").exists());
        assertTrue(new File(outputPath, "com/example/service/UserService.java").exists());

        // Vérifier le contenu du fichier entité
        String entityContent = Files.readString(new File(outputPath, "com/example/User.java").toPath());
        assertTrue(entityContent.contains("@Entity"));
        assertTrue(entityContent.contains("private String username"));
        assertTrue(entityContent.contains("private String email"));
        assertTrue(entityContent.contains("private String password"));
        assertTrue(entityContent.contains("private Boolean active"));
        assertTrue(entityContent.contains("private LocalDateTime lastLoginDate"));

        // Vérifier le contenu du repository
        String repoContent = Files.readString(new File(outputPath, "com/example/repo/UserRepository.java").toPath());
        assertTrue(repoContent.contains("JpaRepository<User, Long>"));
        assertTrue(repoContent.contains("Optional<User> findByUsername"));

        // Vérifier le contenu du service
        String serviceContent = Files.readString(new File(outputPath, "com/example/service/UserService.java").toPath());
        assertTrue(serviceContent.contains("@Service"));
        assertTrue(serviceContent.contains("@Transactional"));
        assertTrue(serviceContent.contains("UserRepository userRepository"));
    }
}