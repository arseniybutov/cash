package ru.crystals.pos.bank.ucs;

import org.mockito.internal.stubbing.StubberImpl;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

public class TerminalStubberImpl extends StubberImpl {

    private static final int STX = 0x02;
    private static final int ETX = 0x03;
    private static final int EOT = 0x04;
    private static final int ACK = 0x06;
    private static final int ENQ = 0x05;
    private static final int DLE = 0x10;
    private static final int NAK = 0x15;

    private Stubber root;

    public TerminalStubberImpl() {
        root = new StubberImpl();
    }
    public TerminalStubberImpl(Stubber doReturn) {
        root = doReturn;
    }

    public TerminalStubberImpl doAcceptSession() {
        return doReturn(ACK);
    }

    public TerminalStubberImpl doRejectSession() {
        return doReturn(NAK);
    }

    public TerminalStubberImpl doRejectRequest() {
        return doReturn(NAK);
    }

    public TerminalStubberImpl doAcceptRequest() {
        return doReturn(ACK);
    }

    public TerminalStubberImpl doEndSession() {
        return doReturn(EOT);
    }
    public TerminalStubberImpl doStartSession() {
        return doReturn(ENQ);
    }


    @Override
    public <T> T when(T mock) {
        return root.when(mock);
    }

    @Override
    public TerminalStubberImpl doThrow(Throwable toBeThrown) {
        root.doThrow(toBeThrown);
        return this;
    }

    @Override
    public TerminalStubberImpl doThrow(Class<? extends Throwable> toBeThrown) {
        root.doThrow(toBeThrown);
        return this;
    }

    @Override
    public TerminalStubberImpl doAnswer(Answer answer) {
        root.doAnswer(answer);
        return this;
    }

    @Override
    public TerminalStubberImpl doNothing() {
        root.doNothing();
        return this;
    }

    @Override
    public TerminalStubberImpl doReturn(Object toBeReturned) {
        root.doReturn(toBeReturned);
        return this;
    }

    @Override
    public TerminalStubberImpl doCallRealMethod() {
        root.doCallRealMethod();
        return this;
    }

}
