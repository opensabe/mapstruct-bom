package io.github.opensabe.mapstruct.processor;

import io.github.opensabe.mapstruct.core.Binding;
import io.github.opensabe.mapstruct.core.MapperRepository;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 根据{@link Binding}注解，自动生成相应的Mapper
 * @author heng.ma
 */
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("io.github.opensabe.mapstruct.core.Binding")
public class MapperGeneratorProcessor extends FreeMarkerProcessor {

    private Elements elementUtils;

    private Types typeUtils;

    private TypeMirror bindingMirror;

    private boolean hasBinding = true;

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        TypeElement typeElement = elementUtils.getTypeElement(Binding.class.getName());
        if (typeElement == null) {
            hasBinding = false;
        }else {
            typeUtils = processingEnv.getTypeUtils();
            bindingMirror = typeElement.asType();
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //待创建的mapper，最后Processor结束以后清空
        if (!hasBinding) {
            return false;
        }
        MapperRep mappers = new MapperRep();
        if (!roundEnv.processingOver() && !annotations.isEmpty()) {
            messager.printNote("---- resolve annotation [io.github.opensabe.mapstruct.core.Binding] ----------");
            Set<AbstractMapper> maps = new HashSet<>();
            for (TypeElement annotation : annotations) {
                Set<? extends Element> beans = roundEnv.getElementsAnnotatedWith(annotation);
                for (Element bean : beans) {
                    List<? extends AnnotationMirror> bindings = bean.getAnnotationMirrors()
                            .stream().filter(m -> typeUtils.isSameType(m.getAnnotationType(), bindingMirror))
                            .collect(Collectors.toList());
                    maps.addAll(MetaDataFactory.create(elementUtils, bean, bindings));
                }
            }


            for (AbstractMapper mapper : maps) {
                String className = mapper.getPackageName() + "." + mapper.getMapperName();
                messager.printNote("[io.github.opensabe.mapstruct.core.Binding] mapper: [%s], sourceClass : [%s], targetClass : [%s] ".formatted(mapper.getMapperName(), mapper.getSourceClass(), mapper.getTargetClass()));
                writeClass(className, mapper.template(), mapper);
                mappers.add(mapper);
            }

            if (!mappers.isEmpty()) {
                writeClass(MapperRepository.class.getName()+"Impl", MapperRepository.class.getSimpleName()+".ftl", mappers);
            }
        }
        return false;
    }
}
