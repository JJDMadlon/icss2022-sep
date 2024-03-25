package nl.han.ica.icss.ast.operations;

import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Literal;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;

public class SubtractOperation extends Operation {

    @Override
    public String getNodeLabel() {
        return "Subtract";
    }

    @Override
    public Literal calculate() {
        Expression CalculatedLhs = lhs.calculate();
        Expression CalculatedRhs = rhs.calculate();

        if(CalculatedLhs instanceof ScalarLiteral && CalculatedRhs instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) CalculatedLhs).value - ((ScalarLiteral) CalculatedRhs).value);
        }
        if(CalculatedLhs instanceof PercentageLiteral && CalculatedRhs instanceof PercentageLiteral) {
            return new PercentageLiteral(((PercentageLiteral) CalculatedLhs).value - ((PercentageLiteral) CalculatedRhs).value);
        }
        if(CalculatedLhs instanceof PixelLiteral && CalculatedRhs instanceof PixelLiteral) {
            return new PixelLiteral(((PixelLiteral) CalculatedLhs).value - ((PixelLiteral) CalculatedRhs).value);
        }

        return null;
    }
}
