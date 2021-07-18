package com.roster123.employeescheduler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * class which uses reflection to expose private fields/methods for testing.
 * 
 * This is valuable for testing fields that are tagged for use by Optaplanner -
 * although they are private, they are the interface Optaplanner is relying upon,
 * so the design ideal that they would change seamlessly as part of a black box is less critical
 */
public class ClassMemberExposer {
    /**
     * get a field value by name
     * @param e
     * @param fieldName
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static Object getFieldValueByName(Object obj, String fieldName) throws IllegalArgumentException, IllegalAccessException{
        // https://en.wikibooks.org/wiki/Java_Programming/Reflection/Accessing_Private_Features_with_Reflection
        Field fields[] = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(fieldName)){
                field.setAccessible(true);
                return field.get(obj);
            }
        }
        return null;
    }

    /**
     * set the value of a non-visible field by name of the field
     * @param obj object we want to change value of field for
     * @param fieldName the name of the field we wish to change the value of
     * @param value the value we wish to put in the field
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void setFieldValueByName(Object obj, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException{
        // https://en.wikibooks.org/wiki/Java_Programming/Reflection/Accessing_Private_Features_with_Reflection
        Field fields[] = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(fieldName)){
                field.setAccessible(true);
                field.set(obj, value);
                return;
            }
        }
    }

    /**
     * generic way to invoke a non-visible method, and return it's returned value (if the method returns void, this will return null)
     * @param obj object we want to run the method for
     * @param methodName the name of the method to run
     * @param params the arguments to the method we wish to run
     * @return the value returned from the method, or null if the return type is void
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static Object genericInvokeMethod(Object obj, String methodName, Object... params) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // from https://stackoverflow.com/a/18909973
        int paramCount = params.length;
        Method method;
        Class<?>[] classArray = new Class<?>[paramCount];
        for (int i = 0; i < paramCount; i++) {
            classArray[i] = params[i].getClass();
        }
        method = obj.getClass().getDeclaredMethod(methodName, classArray);
        method.setAccessible(true);
        return method.invoke(obj, params);
    }
}
