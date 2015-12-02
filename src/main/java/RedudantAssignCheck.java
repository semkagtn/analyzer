import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by semkagtn on 01.12.15.
 */
public class RedudantAssignCheck extends Check {

    private static Map<Integer, String> symbols = new HashMap<>();

    static {
        symbols.put(TokenTypes.PLUS, "+");
        symbols.put(TokenTypes.MINUS, "-");
        symbols.put(TokenTypes.STAR, "*");
        symbols.put(TokenTypes.DIV, "/");
        symbols.put(TokenTypes.MOD, "%");
    }

    private void checkAssignment(DetailAST ast,
                                        String variable,
                                        String left,
                                        String right,
                                        int operationCode) {
        if (!variable.equals(left)) {
            return;
        }
        String operator = symbols.get(operationCode);
        String badExpr = String.format("%s = %s %s %s", variable, left, operator, right);
        String goodExpr = String.format("%s %s= %s", variable, operator, right);
        String msg = String.format("You should use '%s' instead '%s'", goodExpr, badExpr);
        log(ast, msg);
    }

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.ASSIGN};
    }

    @Override
    public void visitToken(DetailAST ast) {
        String variable = ast.getFirstChild().getText();
        DetailAST expression = ast.getLastChild();
        if (expression.getChildCount() != 2) {
            return;
        }
        String left = expression.getFirstChild().getText();
        String right = expression.getLastChild().getText();
        int operation = expression.getType();
        if (operation == TokenTypes.PLUS || operation == TokenTypes.STAR) {
            checkAssignment(ast, variable, left, right, operation);
            checkAssignment(ast, variable, right, left, operation);
        }
        if (operation == TokenTypes.DIV || operation == TokenTypes.MINUS || operation == TokenTypes.MOD) {
            checkAssignment(ast, variable, left, right, operation);
        }
    }
}
