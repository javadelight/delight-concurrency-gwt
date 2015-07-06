package delight.concurrency.gwt;

import delight.concurrency.Concurrency;
import delight.concurrency.gwt.internal.GwtConcurrencyFactory;
import delight.factories.Factory;

public class ConcurrencyGwt {

    public static Concurrency create() {
        return new GwtConcurrencyImpl();
    }

    public static Factory<?, ?, ?> createFactory() {
        return new GwtConcurrencyFactory();
    }

}
