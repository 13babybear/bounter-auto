package cn.bounter.auto.process;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

/**
 * 金额'元'节点访问器
 */
public class YuanTranslator extends TreeTranslator {

    private TreeMaker treeMaker;
    private Names names;
    private JCTree.JCVariableDecl jcVariableDecl;


    public YuanTranslator(TreeMaker treeMaker, Names names, JCTree.JCVariableDecl jcVariableDecl) {
        this.treeMaker = treeMaker;
        this.names = names;
        this.jcVariableDecl = jcVariableDecl;
    }


    /**
     * 访问类节点
     * @param jcClassDecl
     */
    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);

        //添加方法
        jcClassDecl.defs = jcClassDecl.defs.prepend(makeGetYuanMethodDecl(jcVariableDecl));
    }


    private JCTree.JCMethodDecl makeGetYuanMethodDecl(JCTree.JCVariableDecl jcVariableDecl) {
        //public
        JCTree.JCModifiers jcModifiers = treeMaker.Modifiers(Flags.PUBLIC);
        JCTree.JCExpression returnType = jcVariableDecl.vartype;
        //方法名称：getXXXYuan
        String varName = jcVariableDecl.name.toString();
        Name name = names.fromString("get" + varName.substring(0, 1).toUpperCase() + varName.substring(1, varName.length()) + "Yuan");
        // new BigDecimal(100);
        JCTree.JCNewClass BigDecimalNew = treeMaker.NewClass(
                null,
                com.sun.tools.javac.util.List.nil(),    // 泛型参数列表
                treeMaker.Ident(names.fromString("BigDecimal")),    // 类名
                List.of(treeMaker.Literal(100)),    //参数列表
                null
        );

        //return this.xxx.divide(new BigDecimal(100))
        JCTree.JCStatement jcStatement = treeMaker.Return(
            treeMaker.Apply(
                    List.of(memberAccess("java.math.BigDecimal")),  //参数类型
                    memberAccess("this." + varName + ".divide"),
                    List.of(BigDecimalNew)      //参数值
            )
        );

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

    /**
     * 变量拼接，如：java.lang.String
     * @param components
     * @return
     */
    private JCTree.JCExpression memberAccess(String components) {
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = treeMaker.Ident(names.fromString(componentArray[0]));
        for (int i = 1; i < componentArray.length; i++) {
            expr = treeMaker.Select(expr, names.fromString(componentArray[i]));
        }
        return expr;
    }
}
