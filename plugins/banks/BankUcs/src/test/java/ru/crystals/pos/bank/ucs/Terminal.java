package ru.crystals.pos.bank.ucs;

import org.mockito.stubbing.Answer;


public class Terminal {

    public static TerminalStubberImpl doAcceptSession() {
        return new TerminalStubberImpl().doAcceptSession();
    }

    public static TerminalStubberImpl doAnswer(Answer answer) {
        return new TerminalStubberImpl().doAnswer(answer);
    }

    public static TerminalStubberImpl doRejectSession() {
        return new TerminalStubberImpl().doRejectSession();
    }

    public static TerminalStubberImpl doRejectRequest() {
        return new TerminalStubberImpl().doRejectRequest();
    }

    public static TerminalStubberImpl doAcceptRequest() {
        return new TerminalStubberImpl().doAcceptRequest();
    }

    public static TerminalStubberImpl doEndSession() {
        return new TerminalStubberImpl().doEndSession();
    }

    public static TerminalStubberImpl doStartSession() {
        return new TerminalStubberImpl().doStartSession();
    }
}
