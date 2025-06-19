package io.github.opensabe.mapstruct.processor;

import io.github.opensabe.mapstruct.core.CommonCopyMapper;
import io.github.opensabe.mapstruct.core.FromMapMapper;
import io.github.opensabe.mapstruct.core.MapperRegister;
import org.mapstruct.Mapper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("io.github.opensabe.mapstruct.core.RegisterRepository")
public class CustomerRegisterProcessor extends FreeMarkerProcessor {

    private Types typeUtils;

    private Elements elementUtils;

    private TypeMirror commonMirror;
    private TypeMirror mapMirror;

    private boolean hasBinding = true;

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        TypeElement typeElement = elementUtils.getTypeElement(CommonCopyMapper.class.getName());
        if (typeElement == null) {
            hasBinding = false;
        }else {
            commonMirror = typeUtils.erasure(typeElement.asType());
            mapMirror = typeUtils.erasure(elementUtils.getTypeElement(FromMapMapper.class.getName()).asType());
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!hasBinding) {
            return false;
        }
        List<CustomerMapper> mappers = new ArrayList<>();
        if (!roundEnv.processingOver() && !annotations.isEmpty()) {
            for (TypeElement annotation : annotations) {
                messager.printNote("------- resolve annotation [io.github.opensabe.mapstruct.core.RegisterRepository] -------------");
                Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(annotation);
                for (Element mapper : set) {
                    if (hasMapperAnnotation(mapper)) {
                        if (mapper instanceof TypeElement typeElement) {
                            typeElement.getInterfaces().stream()
                                    .filter(this::isSubtype)
                                    .filter(m -> m instanceof DeclaredType)
                                    .findFirst()
                                    .ifPresent(m -> {
                                        List<? extends TypeMirror> arguments = ((DeclaredType) m).getTypeArguments();
                                        if (arguments.size() == 2) {
                                            mappers.add(new CustomerMapper(mapper.toString(), arguments.getFirst().toString(), arguments.get(1).toString()));
                                        }else {
                                            mappers.add(new CustomerMapper(mapper.toString(),null, arguments.getFirst().toString()));
                                        }
                                    });
                        }
                    }
                }
            }
            if (!mappers.isEmpty()) {
                mappers.forEach(mapper -> messager.printNote("[io.github.opensabe.mapstruct.core.RegisterRepository] find mapper [%s]".formatted(mapper.getMapperClass())));
                Element interfaceName = elementUtils.getTypeElement(MapperRegister.class.getName());
                String className = interfaceName.toString() + "Impl";
                writeClass(className, MapperRegister.class.getSimpleName()+".ftl", Map.of("mappers", mappers));
            }

        }
        return false;
    }

    private boolean isSubtype (TypeMirror type) {
        return typeUtils.isSubtype(type, mapMirror) || typeUtils.isSubtype(type, commonMirror);
    }

    private boolean hasMapperAnnotation (Element mapper) {
        return elementUtils.getAllAnnotationMirrors(mapper)
                .stream()
                .anyMatch(a -> typeUtils.isSameType(elementUtils.getTypeElement(Mapper.class.getName()).asType(), a.getAnnotationType()));
    }
}
