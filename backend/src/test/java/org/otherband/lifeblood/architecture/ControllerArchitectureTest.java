package org.otherband.lifeblood.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.elements.GivenMethodsConjunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.assertj.core.api.Assertions.assertThat;

public class ControllerArchitectureTest {

    public static final JavaClasses LIFE_BLOOD_PACKAGES = new ClassFileImporter()
            .importPackages("org.otherband.lifeblood");

    @BeforeAll
    static void setup() {
        assertThat(LIFE_BLOOD_PACKAGES).hasSizeGreaterThan(10); // check imported correctly
    }

    @Test
    void testOnlyOpenApiObjectsUsed() {
        ArchRule rule = apiMethods()
                .should(useGeneratedModelsForRequestBodyParameters())
                .because("@RequestBody parameters in controller methods must use OpenAPI-generated models");
        rule.check(LIFE_BLOOD_PACKAGES);
    }

    @Test
    void allControllersMethodsHaveSecurityAnnotation() {
        ArchRule rule = apiMethods().should()
                .beAnnotatedWith(PreAuthorize.class)
                .orShould()
                .beAnnotatedWith(PostAuthorize.class)
                .because("We use method-level security");
        rule.check(LIFE_BLOOD_PACKAGES);
    }


    private static GivenMethodsConjunction apiMethods() {
        return methods()
                .that().areAnnotatedWith(GetMapping.class)
                .or()
                .areAnnotatedWith(PostMapping.class)
                .or()
                .areAnnotatedWith(PutMapping.class)
                .or()
                .areAnnotatedWith(DeleteMapping.class)
                .or()
                .areAnnotatedWith(PatchMapping.class)
                .or()
                .areAnnotatedWith(RequestMapping.class);
    }

    private static ArchCondition<JavaMethod> useGeneratedModelsForRequestBodyParameters() {
        return new ArchCondition<>("use generated models for @RequestBody parameters") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                for (JavaParameter parameter : method.getParameters()) {
                    if (hasRequestBodyAnnotation(parameter)) {
                        JavaClass parameterType = parameter.getRawType();
                        String parameterTypeName = parameterType.getName();

                        boolean isValid = isString(parameterTypeName) ||
                                isGeneratedModel(parameterType);

                        if (!isValid) {
                            String message = String.format(
                                    "Method %s.%s has @RequestBody parameter '%s' of type %s, " +
                                            "which is not a String or in package org.otherband.lifeblood.generated",
                                    method.getOwner().getSimpleName(),
                                    method.getName(),
                                    parameter.getIndex(),
                                    parameterTypeName
                            );
                            events.add(SimpleConditionEvent.violated(method, message));
                        }
                    }
                }
            }

            private boolean hasRequestBodyAnnotation(JavaParameter parameter) {
                return parameter.getAnnotations().stream()
                        .anyMatch(annotation ->
                                annotation.getRawType().isEquivalentTo(RequestBody.class));
            }

            private boolean isString(String typeName) {
                return "java.lang.String".equals(typeName);
            }

            private boolean isGeneratedModel(JavaClass type) {
                return type.getPackageName().startsWith("org.otherband.lifeblood.generated");
            }
        };
    }
}