package io.improbable.keanu.tensor;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;


public class TensorMatchers {
    private TensorMatchers() {}

    public static <T> Matcher<Tensor<T>> isScalarWithValue(Matcher<T> value) {
        return new TypeSafeDiagnosingMatcher<Tensor<T>>() {
            @Override
            protected boolean matchesSafely(Tensor<T> item, Description mismatchDescription) {
                mismatchDescription.appendValue(item);
                return item.isScalar() && value.matches(item.getValue(0));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Scalar with value ").appendValue(value);
            }
        };
    }
}
