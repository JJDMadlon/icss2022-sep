package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Evaluator implements Transform {

    private final LinkedList<HashMap<String, Literal>> variableValues;
    private final LinkedList<ASTNode> NodesToRemove = new LinkedList<>();

    public Evaluator() {
        this.variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        Stylesheet stylesheet = ast.root;
        evaluateStyleSheet(stylesheet);

        for (ASTNode node : NodesToRemove) {
            stylesheet.removeChild(node);
        }
    }

    private Literal literalFromVariable(String name) {
        for (HashMap<String, Literal> scope : variableValues) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }

        return null;
    }

    private void evaluateStyleSheet(Stylesheet sheet) {
        variableValues.add(new HashMap<>());
        for(ASTNode node : sheet.getChildren()) {
            if (node instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) node);
            }
            else if (node instanceof Stylerule) {
                evaluateStylerule((Stylerule) node);
            }
        }
        variableValues.removeLast();
    }

    private LinkedList<ASTNode> evaluateVariableAssignment(VariableAssignment variable) {
        LinkedList<ASTNode> nodesToRemove = new LinkedList<>();
        if(variable.expression instanceof ColorLiteral) {
            variableValues.getLast().put(variable.name.name, (ColorLiteral) variable.expression);
        }
        else if(variable.expression instanceof PixelLiteral) {
            variableValues.getLast().put(variable.name.name, (PixelLiteral) variable.expression);
        }
        else if(variable.expression instanceof PercentageLiteral) {
            variableValues.getLast().put(variable.name.name, (PercentageLiteral) variable.expression);
        }
        else if(variable.expression instanceof ScalarLiteral) {
            variableValues.getLast().put(variable.name.name, (ScalarLiteral) variable.expression);
        }
        else if (variable.expression instanceof BoolLiteral) {
            variableValues.getLast().put(variable.name.name, (BoolLiteral) variable.expression);
        }
        else if(variable.expression instanceof VariableReference) {
            if(variableValues.getLast().containsKey(((VariableReference) variable.expression).name)) {
                variableValues.getLast().put(variable.name.name, variableValues.getLast().get(((VariableReference) variable.expression).name));
            }
            else {
                variable.setError("Variable " + ((VariableReference) variable.expression).name + " not found");
            }
        }
        else if(variable.expression instanceof Operation) {
            Literal literal = evaluateOperation((Operation) variable.expression);
            variableValues.getLast().put(variable.name.name, literal);
        }

        nodesToRemove.add(variable);
        NodesToRemove.add(variable);
        return nodesToRemove;
    }

    private LinkedList<ASTNode> evaluateStyleBody(Stylerule rule, ArrayList<ASTNode> body) {
        variableValues.addFirst(new HashMap<>());
        LinkedList<ASTNode> nodesToAdd = new LinkedList<>();
        LinkedList<ASTNode> nodesToRemove = new LinkedList<>();
        for (ASTNode node : body) {
            if (node instanceof VariableAssignment) {
                nodesToRemove.addAll(evaluateVariableAssignment((VariableAssignment) node));
            } else if (node instanceof IfClause) {
                evaluateIfClause(rule, (IfClause) node, nodesToAdd, nodesToRemove);
            } else if (node instanceof Declaration) {
                evaluateDeclaration((Declaration) node);
            }
        }
        for (ASTNode node : nodesToRemove) {
            rule.removeChild(node);
        }
        variableValues.removeFirst();
        return nodesToAdd;
    }

    private void evaluateStylerule(Stylerule rule) {
        LinkedList<ASTNode> nodesToAdd = evaluateStyleBody(rule, rule.body);
        for (ASTNode node : nodesToAdd) {
            rule.addChild(node);
        }

        boolean hasIfClause = false;
        for (ASTNode node : rule.body) {
            if (node instanceof IfClause) {
                hasIfClause = true;
                break;
            }
        }
        if (hasIfClause){
            evaluateStylerule(rule);
        } else {
            evaluateStyleBody(rule, rule.body);
        }
    }

    private void evaluateDeclaration(Declaration declaration) {
        if (declaration.expression instanceof Operation) {
            declaration.expression = evaluateOperation((Operation) declaration.expression);
        }
        else if(declaration.expression instanceof VariableReference) {
            Literal literal = literalFromVariable(((VariableReference) declaration.expression).name);
            if(literal != null) {
                declaration.expression = literal;
            }
            else {
                declaration.setError("Variable " + ((VariableReference) declaration.expression).name + " not found");
            }
        }
    }

    private Literal evaluateOperation(Operation operation) {
        evluateAllChilderenOfOperation(operation);

        return operation.calculate();
    }

    private void evluateAllChilderenOfOperation(Operation operation) {
        if(operation.lhs instanceof Operation) {
            evluateAllChilderenOfOperation((Operation) operation.lhs);
        }
        if(operation.rhs instanceof Operation) {
            evluateAllChilderenOfOperation((Operation) operation.rhs);
        }
        if(operation.lhs instanceof VariableReference){
            replaceVariableReference(operation, operation.lhs);
        }
        if(operation.rhs instanceof VariableReference) {
            replaceVariableReference(operation, operation.rhs);
        }
    }

    private void replaceVariableReference(Operation operation, Expression expression) {
        if(expression instanceof VariableReference) {
            Literal literal = literalFromVariable(((VariableReference) expression).name);
            if(operation.lhs == expression) {
                operation.lhs = literal;
            }
            else if(operation.rhs == expression) {
                operation.rhs = literal;

            }
        }
    }

    private void replaceBoolVariableReference(IfClause ifClause, Expression conditionalExpression) {
        if(conditionalExpression instanceof VariableReference) {
            ifClause.conditionalExpression = literalFromVariable(((VariableReference) conditionalExpression).name);
        }
    }

    private void evaluateIfClause(Stylerule rule, IfClause ifClause, LinkedList<ASTNode> nodesToAdd, LinkedList<ASTNode> nodesToRemove){
        replaceBoolVariableReference(ifClause, ifClause.conditionalExpression);
        LinkedList<ASTNode> tempNodesToAdd = new LinkedList<>();
        if(getBoolFromExpression(ifClause.conditionalExpression)){
            tempNodesToAdd.addAll(ifClause.body);
        }
        else if(ifClause.elseClause != null){
            tempNodesToAdd.addAll(ifClause.elseClause.body);
        }

        nodesToRemove.add(ifClause);
        if(ifClause.elseClause != null){
            nodesToRemove.add(ifClause.elseClause);
        }

        nodesToAdd.addAll(tempNodesToAdd);
    }

    private boolean getBoolFromExpression(Expression conditionalExpression) {
        if(conditionalExpression instanceof BoolLiteral){
            return ((BoolLiteral) conditionalExpression).value;
        }
        return false;
    }

}
