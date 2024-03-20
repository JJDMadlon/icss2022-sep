package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.types.ExpressionType;

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
            checkVariableReference(variableAssignment.name);
        }
        else {
            ExpressionType expressionType = checkExpressionType(variableAssignment.expression);
            variableTypes.getLast().put(variableAssignment.name.name, expressionType);
        }
    }

    private ExpressionType checkExpressionType(Expression expression) {
        if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else {
            return ExpressionType.UNDEFINED;
        }
    }

    private void checkStyleRule(Stylerule styleRule) {
        for(ASTNode child : styleRule.getChildren()) {
            if(child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            }
        }
    }

    private void checkDeclaration(Declaration declaration) {
        if(declaration.expression instanceof VariableReference) {
            checkVariableReference((VariableReference) declaration.expression);
        }
        else if(declaration.property.name.endsWith(("color"))) {
            if(!(declaration.expression instanceof ColorLiteral)) {
                declaration.setError("Color must be a hex code ie: #ffffff");
            }
        }
        else if(declaration.property.name.equals("width")) {
            if(!(declaration.expression instanceof PixelLiteral)) {
                declaration.setError("Width must be a pixel size ie: 100px");
            }
        }
    }

    private void checkVariableReference(VariableReference variableReference) {
        for (HashMap<String, ExpressionType> variableType : variableTypes) {
            if (variableType.containsKey(variableReference.name)) {
                return;
            } else {
                variableReference.setError(variableReference.name + " is not defined");
            }
        }
    }


}
