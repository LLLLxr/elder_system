package org.smart_elder_system.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ModuleDependencyRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
            .importPackages("org.smart_elder_system");

    @Test
    void modulesShouldNotDependOnOtherModulesPo() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("org.smart_elder_system.admission..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.smart_elder_system.health..po..",
                        "org.smart_elder_system.contract..po..",
                        "org.smart_elder_system.caredelivery..po.."
                );

        rule.check(classes);
    }

    @Test
    void modulesShouldNotDependOnOtherModulesRepository() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("org.smart_elder_system.admission..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.smart_elder_system.health..repository..",
                        "org.smart_elder_system.contract..repository..",
                        "org.smart_elder_system.caredelivery..repository.."
                );

        rule.check(classes);
    }
}
