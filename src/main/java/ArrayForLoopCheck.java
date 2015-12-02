import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Created by semkagtn on 02.12.15.
 */
public class ArrayForLoopCheck extends Check {

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.LITERAL_FOR};
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST init = ast.getFirstChild().getNextSibling();
        String var = getVarAssignedWithZero(init);
        if (var == null) {
            return;
        }
        DetailAST condition = init.getNextSibling().getNextSibling();
        String array = getArrayName(condition);
        if (array == null) {
            return;
        }
        DetailAST postExpr = condition.getNextSibling().getNextSibling();
        if (!isSingleIncrement(postExpr, var)) {
            return;
        }
        DetailAST loopBody = postExpr.getNextSibling().getNextSibling();
        if (loopBody.getType() == TokenTypes.EXPR) {
            loopBody = loopBody.getParent();
        }
        if (!loopBodyContainsDifficultExpr(loopBody, array, var)) {
            log(ast, "You should use 'foreach loop' instead 'for'");
        }
    }

    private String getVarAssignedWithZero(DetailAST init) {
        if (init.getChildCount(TokenTypes.VARIABLE_DEF) != 1) {
            return null;
        }
        DetailAST variableDef = init.getFirstChild();
        if (variableDef.getChildCount(TokenTypes.TYPE) == 0) {
            return null;
        }
        DetailAST varDefExpr = variableDef.getLastChild().getFirstChild().getFirstChild();
        if (varDefExpr.getType() != TokenTypes.NUM_INT && varDefExpr.getType() != TokenTypes.NUM_LONG) {
            return null;
        }
        long exprValue = Long.parseLong(varDefExpr.getText());
        if (exprValue != 0L) {
            return null;
        }
        return variableDef.getFirstChild().getNextSibling().getNextSibling().getText();
    }

    private String getArrayName(DetailAST condition) {
        if (condition.getChildCount() != 1) {
            return null;
        }
        DetailAST expression = condition.getFirstChild().getFirstChild();
        if (expression.getType() != TokenTypes.LT) {
            return null;
        }
        DetailAST exprRight = expression.getLastChild();
        if (exprRight.getType() != TokenTypes.DOT) {
            return null;
        }
        DetailAST dotRight = exprRight.getLastChild();
        if (!dotRight.getText().equals("length")) {
            return null;
        }
        return exprRight.getFirstChild().getText();
    }

    private boolean isSingleIncrement(DetailAST postExpr, String var) {
        if (postExpr.getChildCount() != 1) {
            return false;
        }
        DetailAST expr = postExpr.getFirstChild().getFirstChild().getFirstChild();
        return (expr.getType() == TokenTypes.INC || expr.getType() == TokenTypes.POST_INC) &&
                expr.getFirstChild().getText().equals(var);
    }

    private boolean loopBodyContainsDifficultExpr(DetailAST expr, String array, String var) {
        if (expr.getType() == TokenTypes.IDENT && expr.getText().equals(var)) {
            return isDifficultIdent(expr, array);
        }
        for (DetailAST child = expr.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (loopBodyContainsDifficultExpr(child, array, var)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDifficultIdent(DetailAST ident, String array) {
        DetailAST parent = ident.getParent();
        if (parent.getType() == TokenTypes.DOT || parent.getType() == TokenTypes.METHOD_CALL) {
            return false;
        }
        DetailAST grandparent = parent.getParent();
        return grandparent.getType() != TokenTypes.INDEX_OP ||
                !parent.getPreviousSibling().getText().equals(array) ||
                grandparent.getParent().getType() == TokenTypes.ASSIGN;
    }
}
