package ru.crystals.pos.fiscalprinter.az.airconn.audit;

import java.util.Objects;

public class ProtectedFile implements Comparable<ProtectedFile> {
    private final String name;
    private final int size;
    private final String md5;

    public ProtectedFile(String name, int size, String md5) {
        this.name = name;
        this.size = size;
        this.md5 = md5;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public String getMd5() {
        return md5;
    }

    @Override
    public int compareTo(ProtectedFile o) {
        return name.compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProtectedFile that = (ProtectedFile) o;
        return size == that.size &&
                name.equals(that.name) &&
                md5.equals(that.md5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, size, md5);
    }
}
