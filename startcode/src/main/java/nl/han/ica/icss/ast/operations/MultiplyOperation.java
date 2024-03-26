package nl.han.ica.icss.ast.operations;

import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Literal;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;

public class MultiplyOperation extends Operation {

    @Override
    public String getNodeLabel() {
        return "Multiply";
    }

    @Override
    public Literal calculate() {
        Expression CalculatedLhs = lhs.calculate();
        Expression CalculatedRhs = rhs.calculate();


        if ((CalculatedLhs instanceof PixelLiteral || CalculatedLhs instanceof ScalarLiteral) && (CalculatedRhs instanceof PixelLiteral || CalculatedRhs instanceof ScalarLiteral)) {
            return new PixelLiteral(CalculatedLhs.getValue() * CalculatedRhs.getValue());
        }
        if ((CalculatedLhs instanceof PercentageLiteral || CalculatedLhs instanceof ScalarLiteral) && (CalculatedRhs instanceof PercentageLiteral || CalculatedRhs instanceof ScalarLiteral)) {
            return new PercentageLiteral(CalculatedLhs.getValue() * CalculatedRhs.calculate().getValue());
        }

        return null;
    }
}
