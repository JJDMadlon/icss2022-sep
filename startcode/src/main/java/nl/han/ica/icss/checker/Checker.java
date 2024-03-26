package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.types.ExpressionType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Checker {

    private LinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        this.variableTypes = new LinkedList<>();

        checkStyleSheet(ast.root);
    }

    private void checkStyleSheet(Stylesheet sheet) {
        variableTypes.add(new HashMap<>());

        for(ASTNode child : sheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            }
            else if(child instanceof Stylerule) {
                variableTypes.add(new HashMap<>());
                checkStyleRule((Stylerule) child);
                variableTypes.removeLast();
            }
        }
    }

    private void checkVariableAssignment(VariableAssignment variableAssignment) {
        if(variableAssignment.expression instanceof VariableReference) {
            if(checkVariableReference((VariableReference) variableAssignment.expression) != ExpressionType.UNDEFINED) {
                for (HashMap<String, ExpressionType> currentScope : variableTypes) {
                    if (currentScope.containsKey(((VariableReference) variableAssignment.expression).name)) {
                        currentScope.put(variableAssignment.name.name, currentScope.get(((VariableReference) variableAssignment.expression).name));
                        return;
                    }
                }
            }
        }
        else if(variableAssignment.expression instanceof Operation){
            checkOperation((Operation) variableAssignment.expression);
        }
        else {
            ExpressionType expressionType = checkExpressionType(variableAssignment.expression);
            variableTypes.getLast().put(variableAssignment.name.name, expressionType);
        }
    }

    private ExpressionType checkVariableReference(VariableReference variableReference) {
        for (HashMap<String, ExpressionType> currentScope : variableTypes) {
            if (currentScope.containsKey(variableReference.name)) {
                return currentScope.get(variableReference.name);
            }
        }
        variableReference.setError(variableReference.name + " is not defined");
        return ExpressionType.UNDEFINED;
    }

    private void checkStyleRule(Stylerule styleRule) {
        checkStyleBody(styleRule.body);
    }

    private void checkStyleBody(ArrayList<ASTNode> styleRule) {
        for(ASTNode child : styleRule) {
            if(child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            }
            else if(child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            }
            else if(child instanceof IfClause) {
                checkIfClause((IfClause) child);
            }
        }
    }

    private ExpressionType checkExpressionType(Expression expression) {
        if(expression instanceof VariableReference) {
            return checkVariableReference((VariableReference) expression);
        }
        else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        }
        else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        }
        else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        }
        else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        }
        else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        }
        else {
            return ExpressionType.UNDEFINED;
        }
    }

    private void checkDeclaration(Declaration declaration) {
       ExpressionType expressionType = checkExpression(declaration.expression);

       if(expressionType == ExpressionType.UNDEFINED) {
           declaration.setError("Undefined expression type");
           return;
       }

       String propertyName = declaration.property.name;
       if(propertyName.matches(("color")) || propertyName.matches(("background-color"))) {
            if(expressionType != ExpressionType.COLOR) {
                declaration.setError("Color must be a hex code");
            }
        }
        else if(propertyName.matches("width")) {
            if(expressionType != ExpressionType.PIXEL && expressionType != ExpressionType.PERCENTAGE) {
                declaration.setError("Width must be a pixel size or a percentage");
            }
        }
        else if(propertyName.matches("height")) {
            if(expressionType != ExpressionType.PIXEL && expressionType != ExpressionType.PERCENTAGE) {
                declaration.setError("Height must be a pixel size or a percentage");
            }
       } else {
           declaration.setError("Unknown property");
       }
    }

    private ExpressionType checkExpression(Expression expression) {
        if (expression instanceof Operation) {
            return checkOperation((Operation) expression);
        } else {
            return checkExpressionType(expression);
        }
    }

    private ExpressionType checkOperation(Operation operation) {
        ExpressionType left;
        ExpressionType right;

        if(operation.lhs instanceof Operation){
            left = checkOperation((Operation) operation.lhs);
        } else {
            left = checkExpressionType(operation.lhs);
        }

        if(operation.rhs instanceof Operation){
            right = checkOperation((Operation) operation.rhs);
        } else {
            right = checkExpressionType(operation.rhs);
        }

        if(left == ExpressionType.COLOR || right == ExpressionType.COLOR) {
            operation.setError("Cannot perform operation on color");
            return ExpressionType.UNDEFINED;
        }

        return left;
    }

    private void checkIfClause(IfClause ifClause) {
        variableTypes.add(new HashMap<>());
        if(checkExpressionType(ifClause.conditionalExpression) != ExpressionType.BOOL) {
            ifClause.setError("If clause must be a boolean");
        }

        checkStyleBody(ifClause.body);

        variableTypes.removeLast();

        if(ifClause.elseClause != null) {
            variableTypes.add(new HashMap<>());
            checkStyleBody(ifClause.elseClause.body);
            variableTypes.removeLast();
        }
    }

}
