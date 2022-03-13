package ru.crystals.pos.scale.massak.protocol100;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class Protocol100Test {

    protected byte[] EMPTY_ANSWER = new byte[]{0, 0, 0, 0, 0, 0};

    protected void assertEqualsWithoutCrc(byte[] request, byte[] response) {
        byte[] clearReq = removeCrc(request);
        byte[] clearResp = removeCrc(response);
        Assert.assertArrayEquals(clearReq, clearResp);
    }

    private byte[] removeCrc(byte[] array) {
        byte[] clear = new byte[array.length - 2];
        System.arraycopy(array, 0, clear, 0, array.length - 2);
        return clear;
    }
}
