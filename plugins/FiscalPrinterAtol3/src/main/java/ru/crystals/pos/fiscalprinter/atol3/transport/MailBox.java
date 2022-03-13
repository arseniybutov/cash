package ru.crystals.pos.fiscalprinter.atol3.transport;

import java.util.NoSuchElementException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

class MailBox {
    private final Lock lock = new ReentrantLock();
    private final Condition responseAvailable = lock.newCondition();

    private ThreadLocal<Pointer> pointer = new ThreadLocal<Pointer>() {
        @Override
        protected Pointer initialValue() {
            return new Pointer(head);
        }
    };

    private class Pointer {
        private Node node;

        Pointer(Node node) {
            node.refCounter += 1;
            this.node = node;
        }

        Response next() {
            node.refCounter -= 1;

            if (node.refCounter <= 0) {
                remove(node);
            }

            Response result = peekNext();

            node = node.next;
            node.refCounter += 1;

            return result;
        }

        Response peekNext() {
            if (node.next == null) {
                throw new NoSuchElementException();
            }

            return node.next.response;
        }

        boolean hasNext() {
            return node.next != null;
        }
    }

    private static class Node {
        private int refCounter = 0;
        private final Response response;
        private Node next;
        private Node prev;

        Node(Response response) {
            this.response = response;
        }
    }

    private Node tail = new Node(null);
    private Node head = tail;

    private Pointer pointer() {
        return pointer.get();
    }

    public void add(Response response) {
        lock.lock();

        try {
            Node node = new Node(response);
            node.prev = tail;
            tail.next = node;
            tail = node;

            responseAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public Response get(Predicate<Response> predicate, long timeout) throws InterruptedException {
        lock.lock();

        try {
            Pointer pointer = pointer();

            while (true) {
                if (pointer.hasNext()) {
                    Response response = pointer.peekNext();
                    if (response.isAsyncError()) {
                        return response;
                    }
                    if (predicate.test(response)) {
                        return response;
                    }
                    pointer.next();
                } else {
                    timeout = responseAvailable.awaitNanos(timeout);
                    if (timeout <= 0) {
                        return null;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void clearPendingAsyncError() {
        lock.lock();

        try {
            Response response = pointer().next();
            if (!response.isAsyncError()) {
                throw new IllegalStateException("no pending errors");
            }
        } finally {
            lock.unlock();
        }
    }

    private void remove(Node node) {
        Node prev = node.prev;
        Node next = node.next;

        next.prev = prev;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }
    }
}
