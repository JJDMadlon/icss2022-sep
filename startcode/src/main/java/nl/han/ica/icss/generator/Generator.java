package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Stylerule;

public class Generator {

	public String generate(AST ast) {
		return generateStylesheet(ast);

	}

	private String generateStylesheet(AST ast) {
		StringBuilder StyleSheetString = new StringBuilder();
		for (ASTNode node : ast.root.getChildren()) {
			if (node instanceof Stylerule) {
				StyleSheetString.append(generateStylerule((Stylerule) node));
			}
		}
		return StyleSheetString.toString();
	}

	private String generateStylerule(Stylerule rule) {
		StringBuilder StyleRuleString = new StringBuilder();
		StyleRuleString.append(rule.selectors.get(0).toString()).append(" {\n");
		for(int i = 0; i < rule.body.size(); i++) {
			StyleRuleString.append("  ").append(rule.body.get(i).toString()).append("\n");
		}
		StyleRuleString.append("}\n");

		return StyleRuleString.toString();
	}

}
