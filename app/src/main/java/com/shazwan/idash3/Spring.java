package com.shazwan.idash3;

/**
 * Damped harmonic oscillator (unit mass) integrated with fixed sub-steps so it stays stable for
 * any frame time. Pure Java: no Android dependency, unit-tested on the JVM.
 */
final class Spring {
    static final float REST_DELTA = .001f, REST_SPEED = .01f, MAX_STEP = 1f / 240f;
    float value, velocity, target;
    private final float stiffness, damping;

    /** @param dampingRatio 1 = critically damped (no overshoot); below 1 = bouncy. */
    Spring(float stiffness, float dampingRatio, float initial) {
        this.stiffness = Math.max(1f, stiffness);
        this.damping = 2f * Math.max(0f, dampingRatio) * (float) Math.sqrt(this.stiffness);
        this.value = initial;
        this.target = initial;
    }

    static Spring snappy(float initial) { return new Spring(520f, .78f, initial); }
    static Spring soft(float initial) { return new Spring(260f, .86f, initial); }

    void jumpTo(float v) { value = v; target = v; velocity = 0f; }

    boolean isAtRest() { return Math.abs(velocity) < REST_SPEED && Math.abs(value - target) < REST_DELTA; }

    /** Advances by dt seconds (clamped to 100 ms); returns true while still moving. */
    boolean step(float dt) {
        if (!(dt > 0f)) return !isAtRest();
        float left = Math.min(dt, .1f);
        while (left > 0f) {
            float h = Math.min(MAX_STEP, left);
            float force = -stiffness * (value - target) - damping * velocity;
            velocity += force * h;
            value += velocity * h;
            left -= h;
        }
        if (isAtRest()) { value = target; velocity = 0f; return false; }
        return true;
    }
}
