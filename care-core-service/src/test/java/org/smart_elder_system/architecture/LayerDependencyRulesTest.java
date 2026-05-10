package org.smart_elder_system.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class LayerDependencyRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
            .importPackages("org.smart_elder_system");

    @Test
    void layersShouldBeRespected() {
        ArchRule rule = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controller").definedBy("..controller..")
                .layer("Service").definedBy("..service..")
                .layer("Repository").definedBy("..repository..")
                .layer("Po").definedBy("..po..")
                .layer("VO").definedBy("..vo..")

                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                .whereLayer("Po").mayOnlyBeAccessedByLayers("Repository", "Service", "VO");

        rule.check(classes);
    }

    @Test
    void repositoriesShouldNotBeAccessedByController() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..");

        rule.check(classes);
    }

    @Test
    void poShouldNotBeAccessedByController() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..po..");

        rule.check(classes);
    }
}
