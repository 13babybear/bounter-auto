package cn.bounter.auto.process;

import cn.bounter.auto.annotation.Unit;
import cn.bounter.auto.annotation.Yuan;
import com.google.auto.service.AutoService;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * 金额单位注解处理器
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("cn.bounter.auto.annotation.Unit")
public class UnitProcesser extends AbstractProcessor {

    // 提供了待处理的抽象语法树
    private JavacTrees javacTrees;
    // 封装了创建AST节点的一些方法
    private TreeMaker treeMaker;
    // 提供了创建标识符的方法
    private Names names;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.javacTrees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //获取被注解的元素
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Unit.class);
        elements.forEach(element -> {
            //获取元素对应的注解值
            Unit unitAnnotation = element.getAnnotation(Unit.class);
            JCTree tree = javacTrees.getTree(element);
            JCTree parentTree = javacTrees.getTree(element.getEnclosingElement());
            if (tree.getKind().equals(Tree.Kind.VARIABLE) && parentTree.getKind().equals(Tree.Kind.CLASS)) {
                JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl)tree;
                parentTree.accept(new UnitTranslator(treeMaker, names, jcVariableDecl, unitAnnotation));
            }
        });
        return true;
    }

}
