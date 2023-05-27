package com.tomwey2.kafka;

public class SshdLogStateMachine {
    /*
        (started, _) -> started
        (started, "sshd") -> sshd
        (sshd, "sshd") -> sshd
        (sshd, _) -> finished
     */
    public enum States {started, sshd, finished}

    private SshdEvent sshdEvent = null;
    private States state = States.started;

    public SshdLogStateMachine() {
        reset();
    }

    public States transform(String key, String value) {
        switch (state) {
            case started:
                if (value.contains("sshd") && value.contains("authentication failure")) {
                    sshdEvent = SshdEventFactory.create(value);
                    state = States.sshd;
                }
                break;
            case sshd:
                if (value.contains("sshd") && value.contains("authentication failure")) {
                    sshdEvent = SshdEventFactory.create(
                            sshdEvent.timestamp(),
                            sshdEvent.message(),
                            sshdEvent.host(),
                            sshdEvent.user(),
                            sshdEvent.failureCount() + 1);
                    state = States.sshd;
                } else if (value.contains("sshd")) {
                    // do nothing
                } else {
                    state = States.finished;
                }
                break;
            case finished:
                // do nothing
                break;
        }

        return state;
    }

    public SshdEvent getSshdEvent() {
        return sshdEvent;
    }

    public void reset() {
        sshdEvent = null;
        state = States.started;
    }
}
