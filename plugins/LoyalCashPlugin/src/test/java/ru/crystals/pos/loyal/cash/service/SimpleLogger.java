package ru.crystals.pos.loyal.cash.service;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Anton Martynov &lt;amartynov@crystals.ru&gt;
 */
public class SimpleLogger implements Logger {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");

    @Override
    public String getName() {
        return "SimpleLogger";
    }

    private void log(String level, String s) {
        System.out.printf("%s [%s] - %s", sdf.format(new Date()), level, s);
        System.out.println();
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(String s) {
        log("trace", s);
    }

    @Override
    public void trace(String s, Object o) {
        trace(MessageFormatter.arrayFormat(s, new Object[]{o}).getMessage());
    }

    @Override
    public void trace(String s, Object o, Object o2) {
        trace(MessageFormatter.arrayFormat(s, new Object[]{o, o2}).getMessage());
    }

    @Override
    public void trace(String s, Object[] objects) {
        trace(MessageFormatter.arrayFormat(s, objects).getMessage());
    }

    @Override
    public void trace(String s, Throwable throwable) {
        trace(s);
        throwable.printStackTrace(System.out);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Marker marker, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Marker marker, String s, Object[] objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDebugEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(String s, Object[] objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Marker marker, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Marker marker, String s, Object[] objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInfoEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(String s, Object[] objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(Marker marker, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(Marker marker, String s, Object[] objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isWarnEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(String s, Object[] objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Marker marker, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Marker marker, String s, Object[] objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isErrorEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(String s, Object[] objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Marker marker, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Marker marker, String s, Object[] objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
