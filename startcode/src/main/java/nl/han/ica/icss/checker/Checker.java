package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import static java.lang.Boolean.FALSE;

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

        Expression leftHandSide = operation.lhs;
        Expression rightHandSide = operation.rhs;
        if(leftHandSide instanceof VariableReference) {
            leftHandSide = getVariableReference((VariableReference) leftHandSide);
        }
        if(leftHandSide instanceof VariableReference) {
            rightHandSide = getVariableReference((VariableReference) rightHandSide);
        }

        checkCalculation(operation, leftHandSide, rightHandSide);

        return left;
    }

    private Expression getVariableReference(VariableReference variableReference) {
        for(HashMap<String, ExpressionType> currentScope : variableTypes) {
            if(currentScope.containsKey(variableReference.name)) {
                ExpressionType expressionType = currentScope.get(variableReference.name);
                if(expressionType == ExpressionType.PIXEL) {
                    return new PixelLiteral(variableReference.getValue());
                }
                else if(expressionType == ExpressionType.PERCENTAGE) {
                    return new PercentageLiteral(variableReference.getValue());
                }
                else if(expressionType == ExpressionType.SCALAR) {
                    return new ScalarLiteral(variableReference.getValue());
                }
                else if(expressionType == ExpressionType.BOOL) {
                    return new BoolLiteral(FALSE);
                }
                else if(expressionType == ExpressionType.COLOR) {
                    return new ColorLiteral("#000000");
                }
            }
        }
        return null;
    }

    private void checkCalculation(Operation operation, Expression left, Expression right) {
        if(left instanceof BoolLiteral || right instanceof BoolLiteral || left instanceof ColorLiteral || right instanceof ColorLiteral) {
            operation.setError("Cannot perform calculation on color or boolean");
        }

        if(operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if(!validOperationTypes(left) || !validOperationTypes(right)) {
                operation.setError("Addition and subtraction can only be performed on pixel or percentage values or other operations");
            }
            else if((left instanceof PercentageLiteral && right instanceof PixelLiteral) || (left instanceof PixelLiteral && right instanceof PercentageLiteral)) {
                operation.setError("Addition and subtraction can only be performed on the same type of literal");
            }
        }
        else if(operation instanceof MultiplyOperation) {
            if(left instanceof ScalarLiteral) {
                if(!validOperationTypes(right)) {
                    operation.setError("Multiplication must have at least one pixel or percentage value");
                }
                else if(right instanceof ScalarLiteral) {
                    operation.setError("Multiplication can only have one scalar value");
                }
            }
            if(right instanceof ScalarLiteral) {
                if(!validOperationTypes(left)) {
                    operation.setError("Multiplication must have at least one pixel or percentage value");
                }
            }
            if(validOperationTypes(left) && validOperationTypes(right)) {
                operation.setError("Multiplication must have at least one scalar value");
            }
        }
    }

    private boolean validOperationTypes(Expression expression) {
        return expression instanceof PixelLiteral || expression instanceof PercentageLiteral || expression instanceof AddOperation || expression instanceof MultiplyOperation || expression instanceof SubtractOperation;
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
