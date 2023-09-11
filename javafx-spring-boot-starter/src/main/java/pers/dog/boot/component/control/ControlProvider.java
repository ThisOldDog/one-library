package pers.dog.boot.component.control;

import java.util.function.Consumer;

import pers.dog.boot.infra.util.ReflectUtils;

/**
 * @author 废柴
 */
public class ControlProvider<T> {
    private T control;
    private Consumer<T> afterAssignmentConsumer;
    private Class<T> type;

    public ControlProvider() {
        this(null);
    }
    public ControlProvider(T control) {
        this.control = control;
    }

    public T get() {
        return control;
    }
    public void set(Object control) {
        this.control = (T) control;
        if (afterAssignmentConsumer != null) {
            afterAssignmentConsumer.accept(this.control);
        }
    }

    @SuppressWarnings("unchecked")
    public Class<?> getType() {
        if (type == null) {
            type = (Class<T>) ReflectUtils.getClassInterfaceGenericType(this.getClass(), 0);
        }
        return type;
    }

    public void afterAssignment(Consumer<T> consumer) {
        this.afterAssignmentConsumer = consumer;
    }
}
