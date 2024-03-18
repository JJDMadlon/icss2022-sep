package nl.han.ica.datastructures;

import nl.han.ica.icss.ast.ASTNode;

public class HANStack<T> implements IHANStack<ASTNode> {
    private HANLinkedList<ASTNode> stackList;

    public HANStack() {
        stackList = new HANLinkedList<>();
    }

    @Override
    public void push(ASTNode value) {
        stackList.addFirst(value);
    }

    @Override
    public ASTNode pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        ASTNode value = stackList.getFirst();
        stackList.removeFirst();
        return value;
    }

    @Override
    public ASTNode peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        return stackList.getFirst();
    }

    @Override
    public boolean isEmpty() {
        return stackList.getSize() == 0;
    }
}
