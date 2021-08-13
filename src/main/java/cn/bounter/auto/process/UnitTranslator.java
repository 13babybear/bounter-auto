package cn.bounter.auto.process;

import cn.bounter.auto.annotation.Unit;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

/**
 * 金额单位节点访问器
 */
public class UnitTranslator extends TreeTranslator {

    private TreeMaker treeMaker;
    private Names names;
    private JCTree.JCVariableDecl jcVariableDecl;
    private Unit unitAnnotation;


    public UnitTranslator(TreeMaker treeMaker, Names names, JCTree.JCVariableDecl jcVariableDecl, Unit unitAnnotation) {
        this.treeMaker = treeMaker;
        this.names = names;
        this.jcVariableDecl = jcVariableDecl;
        this.unitAnnotation = unitAnnotation;
    }


    /**
     * 访问类节点
     * @param jcClassDecl
     */
    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);

        //添加方法
        jcClassDecl.defs = jcClassDecl.defs.prepend(makeGetterWithUnitMethodDecl(jcVariableDecl));
    }


    /**
     * 生成GetXXXWithUnit方法
     * @param jcVariableDecl
     * @return
     */
    private JCTree.JCMethodDecl makeGetterWithUnitMethodDecl(JCTree.JCVariableDecl jcVariableDecl) {
        //public
        JCTree.JCModifiers jcModifiers = treeMaker.Modifiers(Flags.PUBLIC);
        //String
        JCTree.JCExpression returnType = treeMaker.Ident(names.fromString("String"));
        //方法名称：getXXX<Unit>
        String varName = jcVariableDecl.name.toString();
        Name name = names.fromString("get" + varName.substring(0, 1).toUpperCase() + varName.substring(1, varName.length()) + "WithUnit");
        //return this.xxx + "元"
        JCTree.JCStatement jcStatement = treeMaker.Return(treeMaker.Binary(JCTree.Tag.PLUS, treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.name), treeMaker.Literal(unitAnnotation.value())));

        List<JCTree.JCStatement> jcStatementList = List.nil();
        jcStatementList = jcStatementList.append(jcStatement);
        //构建代码块
        JCTree.JCBlock jcBlock = treeMaker.Block(0, jcStatementList);
        //泛型参数列表
        List<JCTree.JCTypeParameter> methodGenericParams = List.nil();
        //参数列表
        List<JCTree.JCVariableDecl> parameters = List.nil();
        //异常抛出列表
        List<JCTree.JCExpression> throwsClauses = List.nil();
        JCTree.JCExpression defaultValue = null;
        JCTree.JCMethodDecl jcMethodDecl = treeMaker.MethodDef(jcModifiers, name, returnType, methodGenericParams, parameters, throwsClauses, jcBlock, defaultValue);
        return jcMethodDecl;
    }
}
