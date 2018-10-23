package io.improbable.keanu.algorithms.variational.optimizer.gradient;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.optim.nonlinear.scalar.LineSearch;

import java.lang.reflect.Field;

public class TestOptimiser {

    public TestOptimiser(int x) {

        try {
            double ABS_TOL_UNUSED = Double.MIN_VALUE;
            double REL_TOL_UNUSED = 1e-15;

            Double abs = Double.MIN_VALUE;

            System.out.println("Abs is zero: " + (abs == 0.0d));
            System.out.println("MIN_VALUE is zero: " +  (Double.MIN_VALUE == 0.0d));
            System.out.println("abs IS MIN_VALUE: " + (abs == Double.MIN_VALUE));
            System.out.println("abs: " + abs);
            System.out.println("MIN_VALUE: " + Double.MIN_VALUE);
            System.out.println("abs bits: " + Double.doubleToRawLongBits(abs));
            System.out.println("MIN_VALUE bits: " + Double.doubleToRawLongBits(Double.MIN_VALUE));
            System.out.println("zero bits: " + Double.doubleToRawLongBits(0.0d));

            // Fails
            if (abs <= 0) {
                throw new NotStrictlyPositiveException(abs);
            }

            // Doesn't fail
//            Double randomValue = 5.0;
//            if (abs <= 0) {
//                throw new NotStrictlyPositiveException(randomValue);
//            }
        }
        catch (NotStrictlyPositiveException e) {
            translateNSPExceptionToIllegalArgException(e);
            throw e;
        }

        // Doesn't fail if you include the following line
//        throw new NullPointerException();

        // Also doesn't fail if you include the following
//        if (abs <= 0) {
//            throw new NullPointerException();
//        }


//        UnivariateOptimizer lineOptimizer = new DavidBrentOptimizer(REL_TOL_UNUSED,
//            Double.MIN_VALUE,
//            new SimpleUnivariateValueChecker(1e-8,
//                1e-8));
    }


    private static void translateNSPExceptionToIllegalArgException(NotStrictlyPositiveException e) {
        long bitwiseMinValue = Double.doubleToLongBits(e.getMin().doubleValue());
        long bitwiseArgumentValue = Double.doubleToLongBits(e.getArgument().doubleValue());

        try {
            Field absTol = LineSearch.class.getDeclaredField("ABS_TOL_UNUSED");
            absTol.setAccessible(true);
            double min = absTol.getDouble(LineSearch.class);

            boolean isLessOrEqual = min <= 0;
            boolean isZero = min == 0;

            throw new IllegalArgumentException("Hit a NSP Error.  Min: "
                + Long.toHexString(bitwiseMinValue)
                + " Arg: " + Long.toHexString(bitwiseArgumentValue) + " min is <=  0 " + isLessOrEqual + " min is zero: " + isZero + " min=" + min, e);
        }catch(NoSuchFieldException | IllegalAccessException e1){
            e.printStackTrace();
        }
    }

}
