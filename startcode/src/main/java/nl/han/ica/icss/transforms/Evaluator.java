package nl.han.ica.icss.transforms;

import jdk.jshell.spi.ExecutionControl;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.HashMap;
import java.util.LinkedList;

public class Evaluator implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        this.variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        evaluateStyleSheet(ast.root);
    }

    private void evaluateStyleSheet(Stylesheet root) {
        variableValues.add(new HashMap<>());

        for (ASTNode child : root.getChildren()) {
            if (child instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) child);
            } else if (child instanceof Stylerule) {
                variableValues.add(new HashMap<>());
                evaluateStyleRule((Stylerule) child);
                variableValues.removeLast();
            }
        }
    }

    private void evaluateVariableAssignment(VariableAssignment child) {
        if (child.expression instanceof Operation) {
            child.expression = evaluateOperation((Operation) child.expression);
        }
        variableValues.getLast().put(child.name.name, (Literal) child.expression);
    }

    private void evaluateStyleRule(Stylerule child) {
        for (ASTNode node : child.getChildren()) {
            if (node instanceof Declaration) {
                evaluateDeclaration((Declaration) node);
            }
            else if (node instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) node);
            }
            else if (node instanceof IfClause) {
                evaluateIfClause((IfClause) node);
            }
        }
    }

    private void evaluateDeclaration(Declaration node) {
        if (node.expression instanceof Operation) {
            node.expression = evaluateOperation((Operation) node.expression);
        }
    }

    private Expression evaluateOperation(Operation expression) {
        if (expression instanceof AddOperation) {
            return evaluateAddOperation((AddOperation) expression);
        }
        else if (expression instanceof SubtractOperation) {
            return evaluateSubtractOperation((SubtractOperation) expression);
        }
        else if (expression instanceof MultiplyOperation) {
            return evaluateMultiplyOperation((MultiplyOperation) expression);
        } else {
            return null;
        }
    }

    private Expression evaluateMultiplyOperation(MultiplyOperation expression) {
        //TODO: Implement this method
        return null;
    }

    private Expression evaluateSubtractOperation(SubtractOperation expression) {
        //TODO: Implement this method
        return null;
    }

    private Expression evaluateAddOperation(AddOperation expression) {
        //TODO: Implement this method
        return null;
    }

    private void evaluateIfClause(IfClause node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Declaration) {
                evaluateDeclaration((Declaration) child);
            }
            else if (child instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) child);
            }
        }
    }

}
