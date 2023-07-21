package cn.bounter.auto.process;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

/**
 * Mybatis-Plus 增删改查注解处理器
 * 需要@MapperScan扫码Bean所在包
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("cn.bounter.auto.annotation.Crud")
public class CrudProcesser extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof TypeElement) {
                    TypeElement typeElement = (TypeElement) element;
                    String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).toString();
                    String className = typeElement.getSimpleName().toString();

                    // 生成 XXXMapper interface
                    TypeSpec mapperInterface = TypeSpec.interfaceBuilder(className + "Mapper")
                                                       .addModifiers(Modifier.PUBLIC)
                                                       .addSuperinterface(ParameterizedTypeName.get(ClassName.get("com.baomidou.mybatisplus.core.mapper", "BaseMapper"), ClassName.get(packageName, className)))
                                                       .build();

                    try {
                        JavaFile.builder(packageName, mapperInterface)
                                .build()
                                .writeTo(processingEnv.getFiler());
                    } catch (IOException e) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "生成XXXMapper失败，错误信息: " + e.getMessage());
                    }

                    // 生成 XXXServiceImpl class
                    ClassName baseMapper = ClassName.get(packageName, className + "Mapper");
                    ClassName serviceImpl = ClassName.get("com.baomidou.mybatisplus.extension.service.impl", "ServiceImpl");
                    ClassName service = ClassName.get("com.baomidou.mybatisplus.extension.service", "IService");

                    TypeSpec serviceImplClass = TypeSpec.classBuilder(className + "ServiceImpl")
                                                        .addModifiers(Modifier.PUBLIC)
                                                        .superclass(ParameterizedTypeName.get(serviceImpl, baseMapper, ClassName.get(packageName, className)))
                                                        .addSuperinterface(ParameterizedTypeName.get(service, ClassName.get(packageName, className)))
                                                        .addAnnotation(ClassName.get("org.springframework.stereotype", "Service"))
                                                        .build();

                    try {
                        JavaFile.builder(packageName, serviceImplClass)
                                .build()
                                .writeTo(processingEnv.getFiler());
                    } catch (IOException e) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "生成XXXServiceImpl失败，错误信息: " + e.getMessage());
                    }
                }
            }
        }
        return true;
    }

}
