package idl.bdp.scrashlocator.spoon;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes the body length for each method contained in a Java project
 *
 * @author Romain Philippon
 */
public class MethodLengthProcessor extends AbstractProcessor<CtMethod<?>> {
    /**
     * Stores the body method length
     */
    private HashMap<String, Integer> listMethodLength = new HashMap<String, Integer>();

    @Override
    public void process(CtMethod<?> method) {
        List<CtStatement> allStmt = method.getElements(new TypeFilter<CtStatement>(CtStatement.class));
        int lengthMethod;

        if(!allStmt.isEmpty()) { // if statement size is 0, it means that the analyzed method belong to an interface
            CtStatement body = allStmt.get(0); //the first statement always contains the complete method body
            lengthMethod = body.toString().split("\n").length - 2;

            /* LOGGING */
            System.out.println("Length of "+ ((CtClass)method.getParent()).getQualifiedName() +"."+ method.getSimpleName() +" : "+ lengthMethod);

            // -2 to remove the opening and closing brace
            this.listMethodLength.put(((CtClass)method.getParent()).getQualifiedName() +"."+ method.getSimpleName(), lengthMethod);
        }
    }

    public Map<String, Integer> getListMethodLength() {
        return this.listMethodLength;
    }
}