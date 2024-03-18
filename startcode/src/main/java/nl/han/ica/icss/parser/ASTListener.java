package nl.han.ica.icss.parser;


import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

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
	public void enterVariableDeclaration(ICSSParser.VariableDeclarationContext context) {
		currentContainer.push(new VariableAssignment());
	}

	@Override
	public void exitVariableDeclaration(ICSSParser.VariableDeclarationContext context) {
		VariableAssignment variableAssignment = (VariableAssignment) currentContainer.pop();
		currentContainer.peek().addChild(variableAssignment);
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
		else if(context.getText().endsWith("em")) {
			currentContainer.push(new ScalarLiteral(context.getText()));
		}
	}

	@Override
	public void exitLiteral(ICSSParser.LiteralContext context) {
		Literal literal = (Literal) currentContainer.pop();
		currentContainer.peek().addChild(literal);
	}

}