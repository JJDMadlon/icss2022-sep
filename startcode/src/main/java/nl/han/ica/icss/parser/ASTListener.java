package nl.han.ica.icss.parser;


import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}

    public AST getAST() {
		return ast;
	}

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext context) {
		currentContainer.push(new Stylesheet());
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext context) {
		ast.root = (Stylesheet) currentContainer.pop();
	}

	@Override
	public void enterStyleRule(ICSSParser.StyleRuleContext context) {
		currentContainer.push(new Stylerule());
	}

	@Override
	public void exitStyleRule(ICSSParser.StyleRuleContext context) {
		Stylerule stylerule = (Stylerule) currentContainer.pop();
		currentContainer.peek().addChild(stylerule);
	}

	@Override
	public void enterSelector(ICSSParser.SelectorContext context) {
		if(context.getText().startsWith("#"))
			currentContainer.push(new IdSelector(context.getText()));
		else if(context.getText().startsWith("."))
			currentContainer.push(new ClassSelector(context.getText()));
		else
			currentContainer.push(new TagSelector(context.getText()));
	}

	@Override
	public void exitSelector(ICSSParser.SelectorContext context) {
		Selector selector = (Selector) currentContainer.pop();
		currentContainer.peek().addChild(selector);
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext context) {
		Declaration declaration = new Declaration();
		declaration.addChild(new PropertyName(context.getChild(0).getText()));
		currentContainer.push(declaration);
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext context) {
		Declaration declaration = (Declaration) currentContainer.pop();
		currentContainer.peek().addChild(declaration);
	}

	@Override
	public void enterLiteral(ICSSParser.LiteralContext context) {
		if(context.getText().startsWith("#")){
			currentContainer.push(new ColorLiteral(context.getText()));
		}
		else if(context.getText().endsWith("px")) {
			currentContainer.push(new PixelLiteral(context.getText()));
		}
		else if(context.getText().endsWith("%")) {
			currentContainer.push(new PercentageLiteral(context.getText()));
		}
		else if(context.getText().matches("[0-9]+(\\.[0-9]+)?")) {
			currentContainer.push(new ScalarLiteral(context.getText()));
		}
		else if(context.getText().matches("TRUE") || context.getText().matches("FALSE")) {
			currentContainer.push(new BoolLiteral(context.getText()));
		}
		else {
			currentContainer.push(new VariableReference(context.getText()));
		}
	}

	@Override
	public void exitLiteral(ICSSParser.LiteralContext context) {
		//printStack(currentContainer);
		if(currentContainer.peek() instanceof VariableReference) {
			VariableReference variableReference = (VariableReference) currentContainer.pop();
			currentContainer.peek().addChild(variableReference);
		} else {
			Literal literal = (Literal) currentContainer.pop();
			currentContainer.peek().addChild(literal);
		}
	}

	@Override
	public void enterVariableReference(ICSSParser.VariableReferenceContext context) {
		currentContainer.push(new VariableReference(context.getText()));
	}

	@Override
	public void exitVariableReference(ICSSParser.VariableReferenceContext context) {
		VariableReference variableReference = (VariableReference) currentContainer.pop();
		currentContainer.peek().addChild(variableReference);
	}

	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext context) {
		currentContainer.push(new VariableAssignment());
	}

	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext context) {
		VariableAssignment variableAssignment = (VariableAssignment) currentContainer.pop();
		currentContainer.peek().addChild(variableAssignment);
	}

	@Override
	public void enterExpression(ICSSParser.ExpressionContext context) {
		if(context.getChildCount() < 3) {
			return;
		}

		if (context.getChild(1).getText().equals("*")) {
			currentContainer.push(new MultiplyOperation());
		} else if (context.getChild(1).getText().equals("+")) {
			currentContainer.push(new AddOperation());
		} else if (context.getChild(1).getText().equals("-")) {
			currentContainer.push(new SubtractOperation());
		}
	}

	@Override
	public void exitExpression(ICSSParser.ExpressionContext context) {
		if(context.getChildCount() < 3) {
			return;
		}

		Expression expression = (Expression) currentContainer.pop();
		currentContainer.peek().addChild(expression);
	}

	@Override
	public void enterIfClause(ICSSParser.IfClauseContext context) {
		currentContainer.push(new IfClause());
		//printStack(currentContainer);
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext context) {
		IfClause ifClause = (IfClause) currentContainer.pop();
		currentContainer.peek().addChild(ifClause);
	}

	@Override
	public void enterElseClause(ICSSParser.ElseClauseContext context) {
		currentContainer.push(new ElseClause());
	}

	@Override
	public void exitElseClause(ICSSParser.ElseClauseContext context) {
		ElseClause elseClause = (ElseClause) currentContainer.pop();
		currentContainer.peek().addChild(elseClause);
	}

	public static void printStack(IHANStack<ASTNode> stack) {
		IHANStack<ASTNode> tempStack = new HANStack<>();
		System.out.println("Printing stack from top to bottom:");

		while (!stack.isEmpty()) {
			ASTNode node = stack.pop();
			System.out.println(node.getClass().getSimpleName() + ": " + node);
			tempStack.push(node);
		}
		System.out.println("");

		while (!tempStack.isEmpty()) {
			stack.push(tempStack.pop());
		}
	}

}