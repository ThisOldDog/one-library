package pers.dog.boot.infra.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import pers.dog.boot.component.cache.status.StatusStore;

/**
 * 反射工具类
 *
 * @author 废柴 2023/2/20 20:01
 */
public class ReflectUtils {

    /**
     * 获取 Class 的反省类
     *
     * @param target 目标类
     * @param index  第几个反省
     * @return 返回目标类中指定下标的反省类型
     */
    public static Class<?> getClassInterfaceGenericType(final Class<?> target, final int index) {
        Type[] genericInterfaces = target.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (!(genericInterface instanceof ParameterizedType)) {
                return null;
            }
            Type rawType = ((ParameterizedType) genericInterface).getRawType();
            if (rawType instanceof Class) {
                Type[] actualTypeArguments = ((ParameterizedType) genericInterface).getActualTypeArguments();
                if (actualTypeArguments.length > 0 && actualTypeArguments.length > index) {
                    Type actualTypeArgument = actualTypeArguments[index];
                    if (actualTypeArgument instanceof Class) {
                        return (Class<?>) actualTypeArgument;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取 Class 的反省类
     *
     * @param target 目标类
     * @param index  第几个反省
     * @return 返回目标类中指定下标的反省类型
     */
    public static Class<?> getClassExtendGenericType(final Class<?> target, final int index) {
        Type genericSuperclass = target.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType)) {
            return null;
        }
        Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
        if (rawType instanceof Class) {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
            if (actualTypeArguments.length > 0 && actualTypeArguments.length > index) {
                Type actualTypeArgument = actualTypeArguments[index];
                if (actualTypeArgument instanceof Class) {
                    return (Class<?>) actualTypeArgument;
                }
            }
        }
        return null;
    }
}
