package ru.crystals.pos.loyal.cash.service;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.crystals.discount.processing.entity.LoyTransactionEntity;

/**
 * @author Anton Martynov &lt;amartynov@crystals.ru&gt;
 */
public class LoyTxDiffGenerator {
    public class DiffInfo {
        String name;
        Object value1, value2;

        public DiffInfo(String name, Object value1, Object value2) {
            this.name = name;
            this.value1 = value1;
            this.value2 = value2;
        }

        @Override
        public String toString() {
            return "DiffInfo{" +
                    "name='" + name + '\'' +
                    ", value1=" + value1 +
                    ", value2=" + value2 +
                    '}';
        }
    }

    private static final Set<String> IGNORED = new HashSet<String>(Arrays.asList("id", "dataType", "transaction"));

    private LoyTransactionEntity tx1, tx2;

    private List<DiffInfo> result;

    public LoyTxDiffGenerator(LoyTransactionEntity tx1, LoyTransactionEntity tx2) {
        if (tx1 == null || tx2 == null) {
            throw new NullPointerException("tx1 == null || tx2 == null");
        }
        this.tx1 = tx1;
        this.tx2 = tx2;
    }

    public List<DiffInfo> compare() throws Exception {
        if (result == null) {
            result = new ArrayList<DiffInfo>();
        }

        BeanInfo beanInfo = Introspector.getBeanInfo(LoyTransactionEntity.class);
        for (PropertyDescriptor prop : beanInfo.getPropertyDescriptors()) {
            if (IGNORED.contains(prop.getName())) {
                continue;
            }
            if ("purchase".equals(prop.getName())) {
                // чек не участвует вообще в процессе, лишнее поле; не стоит его и сравнивать
                continue;
            }

            Method getter = prop.getReadMethod();
            Object v1 = getter.invoke(tx1);
            Object v2 = getter.invoke(tx2);
            if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                continue;
            }

            if (prop.getPropertyType().isAssignableFrom(List.class)) {
                // списки
                compareList(prop, (List) v1, (List) v2);
            } else {
                result.add(new DiffInfo(prop.getName(), v1, v2));
            }
        }

        return result;
    }

    private void compareList(PropertyDescriptor prop, List<?> l1, List<?> l2) throws Exception {
        if (l1.size() != l2.size()) {
            result.add(new DiffInfo(prop.getName() + ".size", l1, l2));
            return;
        }

        BeanInfo beanInfo = null;
        for (int i = 0; i < l1.size(); i++) {
            Object o1 = l1.get(i);
            Object o2 = l2.get(i);
            if (beanInfo == null) {
                beanInfo = Introspector.getBeanInfo(o1.getClass());
            }

            for (PropertyDescriptor prop1 : beanInfo.getPropertyDescriptors()) {
                if (IGNORED.contains(prop1.getName())) {
                    continue;
                }

                Method getter = prop1.getReadMethod();
                Object v1 = getter.invoke(o1);
                Object v2 = getter.invoke(o2);

                if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                    continue;
                }

                if (prop1.getPropertyType().isAssignableFrom(List.class)) {
                    // списки
                    compareList(prop1, (List) v1, (List) v2);
                } else if (v1 != null && v1.getClass().isArray()) {
                    // похоже, надо сравнивать массивы:
                    boolean different = true;
                    if (v1.getClass().equals(v2.getClass())) {
                        // Типы (массивов) совпали
                        if (v1 instanceof byte[]) {
                            different = !Arrays.equals((byte[]) v1, (byte[]) v2);
                        } else if (v1 instanceof Object[]) {
                            different = !Arrays.equals((Object[]) v1, (Object[]) v2);
                        }
                    }
                    
                    if (different) {
                        // эти поля тупо отличаются
                        result.add(new DiffInfo(prop1.getName(), v1, v2));
                    }
                } else {
                    // эти поля тупо отличаются
                    result.add(new DiffInfo(prop1.getName(), v1, v2));
                }
            }
        }
    }
}
